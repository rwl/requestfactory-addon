package org.springframework.roo.addon.requestfactory;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.INSTANCE_REQUEST;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
import static org.springframework.roo.addon.requestfactory.account.AccountJavaType.ROO_ACCOUNT;
import static org.springframework.roo.addon.requestfactory.scaffold.ScaffoldJavaType.KEY;
import static org.springframework.roo.addon.requestfactory.scaffold.ScaffoldJavaType.ROO_REQUEST_FACTORY;
import static org.springframework.roo.model.JavaType.INT_PRIMITIVE;
import static org.springframework.roo.model.JavaType.STRING;
import static org.springframework.roo.model.JdkJavaType.ARRAY_LIST;
import static org.springframework.roo.model.JdkJavaType.HASH_SET;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.JdkJavaType.SET;
import hapax.Template;
import hapax.TemplateDataDictionary;
import hapax.TemplateDictionary;
import hapax.TemplateException;
import hapax.TemplateLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.addon.requestfactory.gwt.bootstrap.scaffold.GwtBootstrapScaffoldMetadata;
import org.springframework.roo.addon.requestfactory.scaffold.RooRequestFactory;
import org.springframework.roo.addon.requestfactory.scaffold.ScaffoldDataKeys;
import org.springframework.roo.addon.requestfactory.scaffold.ScaffoldJavaType;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeParsingService;
import org.springframework.roo.classpath.customdata.CustomDataKeys;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.layers.LayerService;
import org.springframework.roo.classpath.layers.LayerType;
import org.springframework.roo.classpath.layers.MemberTypeAdditions;
import org.springframework.roo.classpath.layers.MethodParameter;
import org.springframework.roo.classpath.persistence.PersistenceMemberLocator;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.RooJavaType;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Provides a basic implementation of {@link RequestFactoryTemplateService} which is used
 * to create {@link ClassOrInterfaceTypeDetails} objects from source files
 * created from templates. This class keeps all templating concerns in one
 * place.
 *
 * @author James Tyrrell
 * @since 1.1.2
 */
@Component
@Service
public class RequestFactoryTemplateServiceImpl implements RequestFactoryTemplateService {

    private static final int LAYER_POSITION = LayerType.HIGHEST.getPosition();

    @Reference RequestFactoryTypeService requestFactoryTypeService;
    @Reference LayerService layerService;
    @Reference MetadataService metadataService;
    @Reference PersistenceMemberLocator persistenceMemberLocator;
    @Reference ProjectOperations projectOperations;
    @Reference TypeLocationService typeLocationService;
    @Reference TypeParsingService typeParsingService;

    private void addImport(final TemplateDataDictionary dataDictionary,
            final JavaType type) {
        dataDictionary.addSection("imports").setVariable("import",
                type.getFullyQualifiedTypeName());
        for (final JavaType param : type.getParameters()) {
            addImport(dataDictionary, param.getFullyQualifiedTypeName());
        }
    }

    private void addImport(final TemplateDataDictionary dataDictionary,
            final String importDeclaration) {
        dataDictionary.addSection("imports").setVariable("import",
                importDeclaration);
    }

    private void addImport(final TemplateDataDictionary dataDictionary,
            final String simpleName, final RequestFactoryType requestFactoryType,
            final String moduleName) {
        addImport(
                dataDictionary,
                requestFactoryType.getPath().packageName(
                        projectOperations.getTopLevelPackage(moduleName))
                        + "." + simpleName + requestFactoryType.getSuffix());
    }

    private void addReference(final TemplateDataDictionary dataDictionary,
            final RequestFactoryType type, final Map<RequestFactoryType, JavaType> mirrorTypeMap) {
        addImport(dataDictionary, mirrorTypeMap.get(type)
                .getFullyQualifiedTypeName());
        dataDictionary.setVariable(type.getName(), mirrorTypeMap.get(type)
                .getSimpleTypeName());
    }

    private void addReference(final TemplateDataDictionary dataDictionary,
            final RequestFactoryType type, final String moduleName) {
        addImport(dataDictionary, getDestinationJavaType(type, moduleName)
                .getFullyQualifiedTypeName());
        dataDictionary.setVariable(type.getName(),
                getDestinationJavaType(type, moduleName).getSimpleTypeName());
    }

    private TemplateDataDictionary buildDictionary(final RequestFactoryType type,
            final String moduleName) {
        final Set<ClassOrInterfaceTypeDetails> proxies = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY);
        final TemplateDataDictionary dataDictionary = buildStandardDataDictionary(
                type, moduleName);
        switch (type) {
        case APP_ENTITY_TYPES_PROCESSOR:
            for (final ClassOrInterfaceTypeDetails proxy : proxies) {
                if (!RequestFactoryUtils.scaffoldProxy(proxy)) {
                    continue;
                }
                final String proxySimpleName = proxy.getName()
                        .getSimpleTypeName();
                final ClassOrInterfaceTypeDetails entity = requestFactoryTypeService
                        .lookupEntityFromProxy(proxy);
                if (entity != null) {
                    final String entitySimpleName = entity.getName()
                            .getSimpleTypeName();

                    dataDictionary.addSection("proxys").setVariable("proxy",
                            proxySimpleName);


                    AnnotationMetadata annotation = entity.getAnnotation(ROO_REQUEST_FACTORY);
                    if (annotation != null) {
                        AnnotationAttributeValue<String> attribute = annotation
                                .getAttribute(RooRequestFactory.PARENT_PROPERTY_ATTRIBUTE);
                        if (attribute == null || attribute.getValue().isEmpty()) {
                            dataDictionary.addSection("roots").setVariable("root",
                                    proxySimpleName);
                        }
                    } else {
                        dataDictionary.addSection("roots").setVariable("root",
                                proxySimpleName);
                    }


                    final String entity1 = new StringBuilder("\t\tif (")
                            .append(proxySimpleName)
                            .append(".class.equals(clazz)) {\n\t\t\tprocessor.handle")
                            .append(entitySimpleName).append("((")
                            .append(proxySimpleName)
                            .append(") null);\n\t\t\treturn;\n\t\t}")
                            .toString();
                    dataDictionary.addSection("entities1").setVariable(
                            "entity", entity1);

                    final String entity2 = new StringBuilder(
                            "\t\tif (proxy instanceof ")
                            .append(proxySimpleName)
                            .append(") {\n\t\t\tprocessor.handle")
                            .append(entitySimpleName).append("((")
                            .append(proxySimpleName)
                            .append(") proxy);\n\t\t\treturn;\n\t\t}")
                            .toString();
                    dataDictionary.addSection("entities2").setVariable(
                            "entity", entity2);

                    final String entity3 = new StringBuilder(
                            "\tpublic abstract void handle")
                            .append(entitySimpleName).append("(")
                            .append(proxySimpleName).append(" proxy);")
                            .toString();
                    dataDictionary.addSection("entities3").setVariable(
                            "entity", entity3);
                    addImport(dataDictionary, proxy.getName()
                            .getFullyQualifiedTypeName());
                }
            }
            break;
        case MASTER_ACTIVITIES:
            for (final ClassOrInterfaceTypeDetails proxy : proxies) {
                if (!RequestFactoryUtils.scaffoldProxy(proxy)) {
                    continue;
                }
                final String proxySimpleName = proxy.getName()
                        .getSimpleTypeName();
                final ClassOrInterfaceTypeDetails entity = requestFactoryTypeService
                        .lookupEntityFromProxy(proxy);
                if (entity != null
                        && !Modifier.isAbstract(entity.getModifier())) {
                    final String entitySimpleName = entity.getName()
                            .getSimpleTypeName();
                    final TemplateDataDictionary section = dataDictionary
                            .addSection("entities");
                    section.setVariable("entitySimpleName", entitySimpleName);
                    section.setVariable("entityFullPath", proxySimpleName);
                    addImport(dataDictionary, entitySimpleName,
                            RequestFactoryType.LIST_ACTIVITY, moduleName);
                    addImport(dataDictionary, proxy.getName()
                            .getFullyQualifiedTypeName());
                    addImport(dataDictionary, entitySimpleName,
                            RequestFactoryType.DESKTOP_LIST_VIEW, moduleName);
                    addImport(dataDictionary, entitySimpleName,
                            RequestFactoryType.MOBILE_LIST_VIEW, moduleName);
                }
            }
            break;
        case APP_REQUEST_FACTORY:
            for (final ClassOrInterfaceTypeDetails proxy : proxies) {
                if (!RequestFactoryUtils.scaffoldProxy(proxy)) {
                    continue;
                }
                final ClassOrInterfaceTypeDetails entity = requestFactoryTypeService
                        .lookupEntityFromProxy(proxy);
                if (entity != null
                        && !Modifier.isAbstract(entity.getModifier())) {
                    final String entitySimpleName = entity.getName()
                            .getSimpleTypeName();
                    ClassOrInterfaceTypeDetails request = requestFactoryTypeService
                            .lookupUnmanagedRequestFromProxy(proxy);
                    if (request == null)
                        request = requestFactoryTypeService.lookupRequestFromProxy(proxy);
                    if (request != null) {
                        final String requestExpression = new StringBuilder("\t")
                                .append(request.getName().getSimpleTypeName())
                                .append(" ")
                                .append(StringUtils
                                        .uncapitalize(entitySimpleName))
                                .append("Request();").toString();
                        dataDictionary.addSection("entities").setVariable(
                                "entity", requestExpression);
                        addImport(dataDictionary, request.getName()
                                .getFullyQualifiedTypeName());
                    }
                }
                dataDictionary.setVariable("sharedScaffoldPackage",
                        RequestFactoryPath.SHARED_SCAFFOLD.packageName(projectOperations
                                .getTopLevelPackage(moduleName)));
            }

            if (typeLocationService.findTypesWithAnnotation(ROO_ACCOUNT).size() != 0) {
                dataDictionary.showSection("account");
            }
            break;
        case LIST_PLACE_RENDERER:
            for (final ClassOrInterfaceTypeDetails proxy : proxies) {
                if (!RequestFactoryUtils.scaffoldProxy(proxy)) {
                    continue;
                }
                final ClassOrInterfaceTypeDetails entity = requestFactoryTypeService
                        .lookupEntityFromProxy(proxy);
                if (entity != null) {
                    final String entitySimpleName = entity.getName()
                            .getSimpleTypeName();
                    final String proxySimpleName = proxy.getName()
                            .getSimpleTypeName();
                    final TemplateDataDictionary section = dataDictionary
                            .addSection("entities");
                    section.setVariable("entitySimpleName", entitySimpleName);
                    section.setVariable("entityFullPath", proxySimpleName);
                    addImport(dataDictionary, proxy.getName()
                            .getFullyQualifiedTypeName());
                }
            }
            break;
        case DETAILS_ACTIVITIES:
            for (final ClassOrInterfaceTypeDetails proxy : proxies) {
                if (!RequestFactoryUtils.scaffoldProxy(proxy)) {
                    continue;
                }
                final ClassOrInterfaceTypeDetails entity = requestFactoryTypeService
                        .lookupEntityFromProxy(proxy);
                if (entity != null) {
                    final String proxySimpleName = proxy.getName()
                            .getSimpleTypeName();
                    final String entitySimpleName = entity.getName()
                            .getSimpleTypeName();
                    final String entityExpression = new StringBuilder(
                            "\t\t\tpublic void handle")
                            .append(entitySimpleName)
                            .append("(")
                            .append(proxySimpleName)
                            .append(" proxy) {\n")
                            .append("\t\t\t\tsetResult(new ")
                            .append(entitySimpleName)
                            .append("ActivitiesMapper(requests, placeController).getActivity(proxyPlace, parentId));\n\t\t\t}")
                            .toString();
                    dataDictionary.addSection("entities").setVariable("entity",
                            entityExpression);
                    addImport(dataDictionary, proxy.getName()
                            .getFullyQualifiedTypeName());
                    addImport(
                            dataDictionary,
                            RequestFactoryType.ACTIVITIES_MAPPER.getPath().packageName(
                                    projectOperations
                                            .getTopLevelPackage(moduleName))
                                    + "."
                                    + entitySimpleName
                                    + RequestFactoryType.ACTIVITIES_MAPPER.getSuffix());
                }
            }
            break;
        case MOBILE_ACTIVITIES:
            // Do nothing
            break;
        case PROXY_NODE_PROCESSOR:
        case PROXY_LIST_NODE_PROCESSOR:
        case IS_LEAF_PROCESSOR:
            for (final ClassOrInterfaceTypeDetails proxy : proxies) {
                if (!RequestFactoryUtils.scaffoldProxy(proxy)) {
                    continue;
                }
                final ClassOrInterfaceTypeDetails entity = requestFactoryTypeService
                        .lookupEntityFromProxy(proxy);
                if (entity != null) {
                    final String entitySimpleName = entity.getName()
                            .getSimpleTypeName();
                    final String proxySimpleName = proxy.getName()
                            .getSimpleTypeName();
                    final JavaPackage topLevelPackage = projectOperations
                            .getTopLevelPackage(moduleName);
                    final String providerSimpleName = entitySimpleName
                            + RequestFactoryType.DATA_PROVIDER.getSuffix();
                    final String providerFullName = RequestFactoryType.DATA_PROVIDER.
                            getPath().packageName(topLevelPackage)
                            + "." + providerSimpleName;
                    final TemplateDataDictionary section = dataDictionary
                            .addSection("entities");
                    section.setVariable("entitySimpleName", entitySimpleName);
                    section.setVariable("entityFullPath", proxySimpleName);
                    addImport(dataDictionary, proxy.getName()
                            .getFullyQualifiedTypeName());
                    section.setVariable("providerSimpleName", providerSimpleName);
                    addImport(dataDictionary, providerFullName);

                    Boolean isLeaf = true;

                    for (final ClassOrInterfaceTypeDetails p : proxies) {
                        if (!RequestFactoryUtils.scaffoldProxy(p)) {
                            continue;
                        }
                        final ClassOrInterfaceTypeDetails ety = requestFactoryTypeService
                                .lookupEntityFromProxy(p);
                        if (ety != null) {

//                    for (final ClassOrInterfaceTypeDetails ety : typeLocationService
//                            .findClassesOrInterfaceDetailsWithAnnotation(ROO_GWT_BOOTSTRAP)) {
                        AnnotationMetadata annotation = ety.getAnnotation(ROO_REQUEST_FACTORY);
                        if (annotation == null) continue;
                        AnnotationAttributeValue<String> annotationAttributeValue = annotation
                                .getAttribute(RooRequestFactory.PARENT_PROPERTY_ATTRIBUTE);
                        if (annotationAttributeValue == null) continue;
                        String parentPropertyName = annotationAttributeValue.getValue();
                        if (parentPropertyName.isEmpty()) continue;
                        FieldMetadata parentProperty = ety
                                .getField(new JavaSymbolName(parentPropertyName));
                        Validate.notNull(parentProperty, "Parent property not found");

                        if (parentProperty.getFieldType().equals(entity.getType())) {
                            isLeaf = false;

                            ClassOrInterfaceTypeDetails proxyForEntity = requestFactoryTypeService
                                    .lookupProxyFromEntity(ety);
                            section.addSection("children").setVariable("child",
                                    proxyForEntity.getName().getSimpleTypeName());
                            addImport(dataDictionary, proxyForEntity.getName()
                                    .getFullyQualifiedTypeName());
                        }
                        }
                    }
                    section.setVariable("isLeaf", isLeaf.toString());
                }
            }
            dataDictionary.setVariable("scaffoldUiPackage",
                    RequestFactoryPath.SCAFFOLD_UI.packageName(projectOperations
                            .getTopLevelPackage(moduleName)));
            break;
        }

        return dataDictionary;
    }

    private TemplateDataDictionary buildMirrorDataDictionary(
            final RequestFactoryType type, final ClassOrInterfaceTypeDetails mirroredType,
            final ClassOrInterfaceTypeDetails proxy,
            final Map<RequestFactoryType, JavaType> mirrorTypeMap,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final String moduleName) {
        final JavaType proxyType = proxy.getName();
        final JavaType javaType = mirrorTypeMap.get(type);

        final TemplateDataDictionary dataDictionary = TemplateDictionary
                .create();

        // Get my locator and
        final JavaType entity = mirroredType.getName();
        final String entityName = entity.getFullyQualifiedTypeName();
        final String metadataIdentificationString = mirroredType
                .getDeclaredByMetadataId();
        final JavaType idType = persistenceMemberLocator
                .getIdentifierType(entity);
        Validate.notNull(idType,
                "Identifier type is not available for entity '" + entityName
                        + "'");

        for (final ClassOrInterfaceTypeDetails gwtBootstrapEntity : typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY)) {
            AnnotationMetadata annotation = gwtBootstrapEntity
                    .getAnnotation(ROO_REQUEST_FACTORY);
            if (annotation == null) {
                continue;
            }
            AnnotationAttributeValue<String> annotationAttributeValue = annotation
                    .getAttribute(RooRequestFactory.PARENT_PROPERTY_ATTRIBUTE);
            if (annotationAttributeValue == null) {
                continue;
            }
            String parentPropertyName = annotationAttributeValue.getValue();
            if (parentPropertyName.isEmpty()) {
                continue;
            }
            FieldMetadata parentProperty = gwtBootstrapEntity
                    .getField(new JavaSymbolName(parentPropertyName));
            Validate.notNull(parentProperty, "Parent field not found");

            if (parentProperty.getFieldType().equals(entity)) {
                ClassOrInterfaceTypeDetails proxyForEntity = requestFactoryTypeService
                        .lookupProxyFromEntity(gwtBootstrapEntity);
                dataDictionary.addSection("children").setVariable("child",
                        proxyForEntity.getName().getSimpleTypeName());
                addImport(dataDictionary,
                        proxyForEntity.getName().getFullyQualifiedTypeName());
            }
        }

        final MethodParameter entityParameter = new MethodParameter(entity,
                "proxy");
        final ClassOrInterfaceTypeDetails request = requestFactoryTypeService
                .lookupRequestFromProxy(proxy);

        final MemberTypeAdditions persistMethodAdditions = layerService
                .getMemberTypeAdditions(metadataIdentificationString,
                        CustomDataKeys.PERSIST_METHOD.name(), entity, idType,
                        LAYER_POSITION, entityParameter);
        Validate.notNull(persistMethodAdditions,
                "Persist method is not available for entity '" + entityName
                        + "'");
        final String persistMethodSignature = getRequestMethodCall(request,
                persistMethodAdditions);
        dataDictionary.setVariable("persistMethodSignature",
                persistMethodSignature);

        final MemberTypeAdditions removeMethodAdditions = layerService
                .getMemberTypeAdditions(metadataIdentificationString,
                        CustomDataKeys.REMOVE_METHOD.name(), entity, idType,
                        LAYER_POSITION, entityParameter);
        Validate.notNull(removeMethodAdditions,
                "Remove method is not available for entity '" + entityName
                        + "'");
        final String removeMethodSignature = getRequestMethodCall(request,
                removeMethodAdditions);
        dataDictionary.setVariable("removeMethodSignature",
                removeMethodSignature);


        AnnotationMetadata annotation = mirroredType.getAnnotation(ROO_REQUEST_FACTORY);
        AnnotationAttributeValue<String> annotationAttributeValue = annotation
                .getAttribute(RooRequestFactory.PARENT_PROPERTY_ATTRIBUTE);
        String parentPropertyName = "";
        if (annotationAttributeValue != null) {
            parentPropertyName = annotationAttributeValue.getValue();
        }

        JavaType altIdType = idType.equals(KEY) ? STRING : idType;

        String countMethodId, findMethodId, countCall, findCall;
        List<MethodParameter> findParameters, countParamemters;
        if (parentPropertyName.isEmpty()) {
            countMethodId = CustomDataKeys.COUNT_ALL_METHOD.name();
            findMethodId = CustomDataKeys.FIND_ENTRIES_METHOD.name();
            countCall = "()";
            findCall = "(range.getStart(), range.getLength())";
            countParamemters = Arrays.asList();
            findParameters = Arrays.asList(new MethodParameter(
                    INT_PRIMITIVE, "firstResult"), new MethodParameter(
                    INT_PRIMITIVE, "maxResults"));
        } else {
            countMethodId = ScaffoldDataKeys.COUNT_BY_PARENT_METHOD.name();
            findMethodId = ScaffoldDataKeys.FIND_ENTRIES_BY_PARENT_METHOD.name();
            countCall = "(parentId)";
            findCall = "(parentId, range.getStart(), range.getLength())";
            countParamemters = Arrays.asList(new MethodParameter(
                    altIdType, parentPropertyName + "Id"));
            findParameters = Arrays.asList(new MethodParameter(
                    altIdType, parentPropertyName + "Id"), new MethodParameter(
                    INT_PRIMITIVE, "firstResult"), new MethodParameter(
                    INT_PRIMITIVE, "maxResults"));

            FieldMetadata parentProperty = mirroredType
                    .getField(new JavaSymbolName(parentPropertyName));
            Validate.notNull(parentProperty, "Parent property not found");
            String parentTypeName = parentProperty.getFieldType().getSimpleTypeName();
            String parentProxyName = parentTypeName + "Proxy";

            String setProxyParentStmt = "if (proxy.get"
                    + StringUtils.capitalize(parentPropertyName) + "() == null) {\n"
                    + "factory." + StringUtils.uncapitalize(parentTypeName)
                    + "Request().find" + parentTypeName + "ByStringId"
                    + "(parentId).fire(new Receiver<" + parentProxyName + ">() {\n"
                    + "@Override\n"
                    + "public void onSuccess(" + parentProxyName + " response) {\n"
                    + "proxy.set" + StringUtils.capitalize(parentPropertyName) + "(response);\n"
                    + "}\n"
                    + "});\n"
                    + "}\n";
            dataDictionary.setVariable("setProxyParentStmt", setProxyParentStmt);
        }

        final MemberTypeAdditions findMethodAdditions = layerService
                .getMemberTypeAdditions(metadataIdentificationString,
                        findMethodId, entity, altIdType, LAYER_POSITION,
                        findParameters);
        Validate.notNull(findMethodAdditions,
                "Find entries method is not available for entity '" + entityName + "'");
        dataDictionary.setVariable("findEntitiesMethod",
                findMethodAdditions.getMethodName() + findCall);

        final MemberTypeAdditions countMethodAdditions = layerService
                .getMemberTypeAdditions(metadataIdentificationString,
                        countMethodId, entity, altIdType, LAYER_POSITION,
                        countParamemters);
        Validate.notNull(countMethodAdditions,
                "Count method is not available for entity '" + entityName + "'");
        dataDictionary.setVariable("countEntitiesMethod",
                countMethodAdditions.getMethodName() + countCall);


        for (final RequestFactoryType reference : type.getReferences()) {
            addReference(dataDictionary, reference, mirrorTypeMap);
        }

        addImport(dataDictionary, proxyType.getFullyQualifiedTypeName());

        final String pluralMetadataKey = PluralMetadata.createIdentifier(
                mirroredType.getName(), PhysicalTypeIdentifier
                        .getPath(mirroredType.getDeclaredByMetadataId()));
        final PluralMetadata pluralMetadata = (PluralMetadata) metadataService
                .get(pluralMetadataKey);
        final String plural = pluralMetadata.getPlural();

        final String simpleTypeName = mirroredType.getName()
                .getSimpleTypeName();
        final JavaPackage topLevelPackage = projectOperations
                .getTopLevelPackage(moduleName);
        dataDictionary.setVariable("className", javaType.getSimpleTypeName());
        dataDictionary.setVariable("packageName", javaType.getPackage()
                .getFullyQualifiedPackageName());
        dataDictionary.setVariable("placePackage",
                RequestFactoryPath.SCAFFOLD_PLACE.packageName(topLevelPackage));
        dataDictionary.setVariable("scaffoldUiPackage",
                RequestFactoryPath.SCAFFOLD_UI.packageName(topLevelPackage));
        dataDictionary.setVariable("sharedScaffoldPackage",
                RequestFactoryPath.SHARED_SCAFFOLD.packageName(topLevelPackage));
        dataDictionary.setVariable("uiPackage",
                RequestFactoryPath.MANAGED_UI.packageName(topLevelPackage));
        dataDictionary.setVariable("requestPackage",
                RequestFactoryPath.MANAGED_REQUEST.packageName(topLevelPackage));
        dataDictionary.setVariable("uiEditorPackage",
                RequestFactoryPath.MANAGED_UI_EDITOR.packageName(topLevelPackage));
        dataDictionary.setVariable("name", simpleTypeName);
        dataDictionary.setVariable("pluralName", plural);
        dataDictionary.setVariable("nameUncapitalized",
                StringUtils.uncapitalize(simpleTypeName));
        dataDictionary.setVariable("proxy", proxyType.getSimpleTypeName());
        dataDictionary.setVariable("pluralName", plural);
        dataDictionary.setVariable("proxyRenderer", RequestFactoryProxyProperty
                .getProxyRendererType(topLevelPackage, proxyType));

        String proxyFields = null;
        RequestFactoryProxyProperty primaryProperty = null;
        RequestFactoryProxyProperty secondaryProperty = null;
        RequestFactoryProxyProperty dateProperty = null;
        final Set<String> importSet = new HashSet<String>();

        List<String> existingEditViewFields = new ArrayList<String>();

        List<String> fieldsInBothViewAndMobileEditView = new ArrayList<String>();

        List<String> existingDetailsViewFields = new ArrayList<String>();

        // Adds names of fields the are found in both the unmanaged EditView and
        // MobileEditView to fieldsInBothViewAndMobileView list
        if (type == RequestFactoryType.EDIT_ACTIVITY_WRAPPER
                || type == RequestFactoryType.MOBILE_EDIT_VIEW
                || type == RequestFactoryType.DESKTOP_EDIT_VIEW) {
            List<String> existingDesktopFields = new ArrayList<String>();
            List<String> existingMobileFields = new ArrayList<String>();

            try {
                String className = RequestFactoryPath.MANAGED_UI_DESKTOP
                        .packageName(topLevelPackage)
                        + "."
                        + simpleTypeName
                        + RequestFactoryType.DESKTOP_EDIT_VIEW.getTemplate();

                ClassOrInterfaceTypeDetails details = typeLocationService
                        .getTypeDetails(new JavaType(className));

                if (details != null) {
                    for (FieldMetadata field : details.getDeclaredFields()) {
                        JavaSymbolName fieldName = field.getFieldName();
                        String name = fieldName.toString();
                        existingDesktopFields.add(name);
                    }
                }

                className = RequestFactoryPath.MANAGED_UI_MOBILE
                        .packageName(topLevelPackage)
                        + "."
                        + simpleTypeName
                        + RequestFactoryType.MOBILE_EDIT_VIEW.getTemplate();

                details = typeLocationService.getTypeDetails(new JavaType(
                        className));

                if (details != null) {
                    for (FieldMetadata field : details.getDeclaredFields()) {
                        JavaSymbolName fieldName = field.getFieldName();
                        String name = fieldName.toString();
                        existingMobileFields.add(name);
                    }
                }

                // Adds names of fields in MobileEditView to existingFields list
                if (type == RequestFactoryType.MOBILE_EDIT_VIEW)
                    existingEditViewFields = existingMobileFields;

                // Adds names of fields in DesktopEditView to existingFields
                // list
                if (type == RequestFactoryType.DESKTOP_EDIT_VIEW)
                    existingEditViewFields = existingDesktopFields;

            }
            catch (Exception e) {
                throw new IllegalArgumentException(e);
            }

            for (String mobileViewField : existingMobileFields) {
                for (String viewField : existingDesktopFields) {
                    if (viewField.equals(mobileViewField)) {
                        fieldsInBothViewAndMobileEditView.add(viewField);
                        break;
                    }
                }
            }
        }

        if (type == RequestFactoryType.MOBILE_DETAILS_VIEW
                || type == RequestFactoryType.DESKTOP_DETAILS_VIEW) {
            List<String> existingDesktopFields = new ArrayList<String>();
            List<String> existingMobileFields = new ArrayList<String>();

            try {
                String className = RequestFactoryPath.MANAGED_UI_DESKTOP
                        .packageName(topLevelPackage)
                        + "."
                        + simpleTypeName
                        + RequestFactoryType.DESKTOP_DETAILS_VIEW.getTemplate();

                ClassOrInterfaceTypeDetails details = typeLocationService
                        .getTypeDetails(new JavaType(className));

                if (details != null) {
                    for (FieldMetadata field : details.getDeclaredFields()) {
                        JavaSymbolName fieldName = field.getFieldName();
                        String name = fieldName.toString();
                        existingDesktopFields.add(name);
                    }
                }

                className = RequestFactoryPath.MANAGED_UI_MOBILE
                        .packageName(topLevelPackage)
                        + "."
                        + simpleTypeName
                        + RequestFactoryType.MOBILE_DETAILS_VIEW.getTemplate();

                details = typeLocationService.getTypeDetails(new JavaType(
                        className));

                if (details != null) {
                    for (FieldMetadata field : details.getDeclaredFields()) {
                        JavaSymbolName fieldName = field.getFieldName();
                        String name = fieldName.toString();
                        existingMobileFields.add(name);
                    }
                }

                // Adds names of fields in MobileDetailsView to existingFields
                // list
                if (type == RequestFactoryType.MOBILE_DETAILS_VIEW)
                    existingDetailsViewFields = existingMobileFields;

                // Adds names of fields in DesktopDetailsView to existingFields
                // list
                if (type == RequestFactoryType.DESKTOP_DETAILS_VIEW)
                    existingDetailsViewFields = existingDesktopFields;
            }
            catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        for (final RequestFactoryProxyProperty requestFactoryProxyProperty : clientSideTypeMap
                .values()) {
            // Determine if this is the primary property.
            if (primaryProperty == null) {
                // Choose the first available field.
                primaryProperty = requestFactoryProxyProperty;
            }
            else if (requestFactoryProxyProperty.isString()
                    && !primaryProperty.isString()
                    && !isPrimaryProp(primaryProperty, mirroredType)) {
                // Favor String properties over other types.
                //secondaryProperty = primaryProperty;
                primaryProperty = requestFactoryProxyProperty;
            }
            /*else if (secondaryProperty == null) {
                // Choose the next available property.
                secondaryProperty = gwtProxyProperty;
            }
            else if (gwtProxyProperty.isString()
                    && !secondaryProperty.isString()) {
                // Favor String properties over other types.
                secondaryProperty = gwtProxyProperty;
            }*/
            if (isPrimaryProp(requestFactoryProxyProperty, mirroredType)) {
                primaryProperty = requestFactoryProxyProperty;
            }
            if (isSecondaryProp(requestFactoryProxyProperty, mirroredType)) {
                secondaryProperty = requestFactoryProxyProperty;
            }

            // Determine if this is the first date property.
            if (dateProperty == null && requestFactoryProxyProperty.isDate()) {
                dateProperty = requestFactoryProxyProperty;
            }

            if (requestFactoryProxyProperty.isProxy()
                    || requestFactoryProxyProperty.isCollectionOfProxy()) {
                if (proxyFields != null) {
                    proxyFields += ", ";
                }
                else {
                    proxyFields = "";
                }
                proxyFields += "\"" + requestFactoryProxyProperty.getName() + "\"";
            }

            // if the property is in the existingFields list, do not add it
            if (!existingDetailsViewFields.contains(requestFactoryProxyProperty.getName())) {
                dataDictionary.addSection("fields").setVariable("field",
                        requestFactoryProxyProperty.getName());

                final TemplateDataDictionary managedPropertiesSection = dataDictionary
                        .addSection("managedProperties");
                managedPropertiesSection.setVariable("prop",
                        requestFactoryProxyProperty.getName());
                managedPropertiesSection.setVariable(
                        "propId",
                        proxyType.getSimpleTypeName() + "_"
                                + requestFactoryProxyProperty.getName());
                managedPropertiesSection.setVariable("propGetter",
                        requestFactoryProxyProperty.getGetter());
                managedPropertiesSection.setVariable("propType",
                        requestFactoryProxyProperty.getType());
                managedPropertiesSection.setVariable("propFormatter",
                        requestFactoryProxyProperty.getFormatter());
                managedPropertiesSection.setVariable("propRenderer",
                        requestFactoryProxyProperty.getRenderer());
                managedPropertiesSection.setVariable("propReadable",
                        requestFactoryProxyProperty.getReadableName());
            }

            final TemplateDataDictionary propertiesSection = dataDictionary
                    .addSection("properties");
            propertiesSection.setVariable("prop", requestFactoryProxyProperty.getName());
            propertiesSection.setVariable(
                    "propId",
                    proxyType.getSimpleTypeName() + "_"
                            + requestFactoryProxyProperty.getName());
            propertiesSection.setVariable("propGetter",
                    requestFactoryProxyProperty.getGetter());
            propertiesSection.setVariable("propType",
                    requestFactoryProxyProperty.getType());
            propertiesSection.setVariable("propFormatter",
                    requestFactoryProxyProperty.getFormatter());
            propertiesSection.setVariable("propRenderer",
                    requestFactoryProxyProperty.getRenderer());
            propertiesSection.setVariable("propReadable",
                    requestFactoryProxyProperty.getReadableName());

            if (!isReadOnly(requestFactoryProxyProperty.getName(), mirroredType)) {
                // if the property is in the existingFields list, do not add it
                if (!existingEditViewFields
                        .contains(requestFactoryProxyProperty.getName()))
                    dataDictionary.addSection("editViewProps").setVariable(
                            "prop", requestFactoryProxyProperty.forEditView());

                final TemplateDataDictionary editableSection = dataDictionary
                        .addSection("editableProperties");
                editableSection.setVariable("prop", requestFactoryProxyProperty.getName());
                editableSection.setVariable(
                        "propId",
                        proxyType.getSimpleTypeName() + "_"
                                + requestFactoryProxyProperty.getName());
                editableSection.setVariable("propGetter",
                        requestFactoryProxyProperty.getGetter());
                editableSection.setVariable("propType",
                        requestFactoryProxyProperty.getType());
                editableSection.setVariable("propFormatter",
                        requestFactoryProxyProperty.getFormatter());
                editableSection.setVariable("propRenderer",
                        requestFactoryProxyProperty.getRenderer());
                editableSection.setVariable("propBinder",
                        requestFactoryProxyProperty.getBinder());
                editableSection.setVariable("propReadable",
                        requestFactoryProxyProperty.getReadableName());
            }

            dataDictionary.setVariable("proxyRendererType",
                    proxyType.getSimpleTypeName() + "Renderer");

            // If the field is not added to the managed MobileEditView and the
            // managed EditView then it there is no reason to add it to the
            // interface nor the start method in the EditActivityWrapper
            if (!fieldsInBothViewAndMobileEditView.contains(requestFactoryProxyProperty
                    .getName())) {
                if (requestFactoryProxyProperty.isProxy() || requestFactoryProxyProperty.isEnum()
                        || requestFactoryProxyProperty.isCollectionOfProxy()) {
                    final TemplateDataDictionary section = dataDictionary
                            .addSection(requestFactoryProxyProperty.isEnum() ? "setEnumValuePickers"
                                    : "setProxyValuePickers");
                    // The methods is required to satisfy the interface.
                    // However, if the field is in the existingFields lists, the
                    // method must be empty because the field will not be added
                    // to the managed view.
                    section.setVariable(
                            "setValuePicker",
                            existingEditViewFields.contains(requestFactoryProxyProperty
                                    .getName()) ? requestFactoryProxyProperty
                                    .getSetEmptyValuePickerMethod()
                                    : requestFactoryProxyProperty
                                            .getSetValuePickerMethod());
                    section.setVariable("setValuePickerName",
                            requestFactoryProxyProperty.getSetValuePickerMethodName());
                    section.setVariable("valueType", requestFactoryProxyProperty
                            .getValueType().getSimpleTypeName());
                    section.setVariable("rendererType",
                            requestFactoryProxyProperty.getProxyRendererType());
                    if (requestFactoryProxyProperty.isProxy()
                            || requestFactoryProxyProperty.isCollectionOfProxy()) {
                        String propTypeName = StringUtils
                                .uncapitalize(requestFactoryProxyProperty
                                        .isCollectionOfProxy() ? requestFactoryProxyProperty
                                        .getPropertyType().getParameters()
                                        .get(0).getSimpleTypeName()
                                        : requestFactoryProxyProperty.getPropertyType()
                                                .getSimpleTypeName());
                        propTypeName = propTypeName.substring(0,
                                propTypeName.indexOf("Proxy"));
                        section.setVariable("requestInterface", propTypeName
                                + "Request");
                        section.setVariable("findMethod",
                                "find" + StringUtils.capitalize(propTypeName)
                                        + "Entries(0, 50)");
                    }
                    maybeAddImport(dataDictionary, importSet,
                            requestFactoryProxyProperty.getPropertyType());
                    maybeAddImport(dataDictionary, importSet,
                            requestFactoryProxyProperty.getValueType());
                    if (requestFactoryProxyProperty.isCollection/*OfProxy*/()) {
                        maybeAddImport(dataDictionary, importSet,
                                requestFactoryProxyProperty.getPropertyType()
                                        .getParameters().get(0));
                        maybeAddImport(dataDictionary, importSet,
                                requestFactoryProxyProperty.getSetEditorType());
                    }
                }
            }

        }

        dataDictionary.setVariable("proxyFields", proxyFields);

        // Add a section for the mobile properties.
        if (primaryProperty != null) {
            dataDictionary
                    .setVariable("primaryProp", primaryProperty.getName());
            dataDictionary.setVariable("primaryPropGetter",
                    primaryProperty.getGetter());
            String primaryPropBuilder = new StringBuilder(
                    "if (value != null) {\n\t\t\t\tsb.appendEscaped(")
                    .append("primaryRenderer")
                    .append(".render(value));\n\t\t\t}").toString();
            dataDictionary
                    .setVariable("primaryPropBuilder", primaryPropBuilder);
        }
        else {
            dataDictionary.setVariable("primaryProp", "id");
            dataDictionary.setVariable("primaryPropGetter", "getId");
            dataDictionary.setVariable("primaryPropBuilder", "");
        }
        if (secondaryProperty != null) {
            dataDictionary.showSection("hasSecondaryProp");
            dataDictionary.setVariable("secondaryPropGetter",
                    secondaryProperty.getGetter());
            dataDictionary.setVariable("secondaryPropBuilder",
                    secondaryProperty.forMobileListView("secondaryRenderer"));
            final TemplateDataDictionary section = dataDictionary
                    .addSection("mobileProperties");
            section.setVariable("prop", secondaryProperty.getName());
            section.setVariable("propGetter", secondaryProperty.getGetter());
            section.setVariable("propType", secondaryProperty.getType());
            section.setVariable("propRenderer", secondaryProperty.getRenderer());
            section.setVariable("propRendererName", "secondaryRenderer");
        }
        else {
            dataDictionary.setVariable("secondaryPropBuilder", "");
        }
        if (dateProperty != null) {
            dataDictionary.setVariable("datePropBuilder",
                    dateProperty.forMobileListView("dateRenderer"));
            final TemplateDataDictionary section = dataDictionary
                    .addSection("mobileProperties");
            section.setVariable("prop", dateProperty.getName());
            section.setVariable("propGetter", dateProperty.getGetter());
            section.setVariable("propType", dateProperty.getType());
            section.setVariable("propRenderer", dateProperty.getRenderer());
            section.setVariable("propRendererName", "dateRenderer");
        }
        else {
            dataDictionary.setVariable("datePropBuilder", "");
        }
        return dataDictionary;
    }

    private boolean isPrimaryProp(final RequestFactoryProxyProperty prop,
            final ClassOrInterfaceTypeDetails entity) {
        return isRenderProp(prop, entity, RooRequestFactory.
                PRIMARY_PROPERTY_ATTRIBUTE);
    }

    private boolean isSecondaryProp(final RequestFactoryProxyProperty prop,
            final ClassOrInterfaceTypeDetails entity) {
        return isRenderProp(prop, entity, RooRequestFactory.
                SECONDARY_PROPERTY_ATTRIBUTE);
    }

    private boolean isRenderProp(final RequestFactoryProxyProperty prop,
            final ClassOrInterfaceTypeDetails entity,
            final String propertyAttribute) {
        AnnotationMetadata annotation = entity.getAnnotation(ROO_REQUEST_FACTORY);
        if (annotation == null) {
            return false;
        }
        AnnotationAttributeValue<String> primaryProperty = annotation
                .getAttribute(propertyAttribute);
        if (primaryProperty == null || !primaryProperty.getValue()
                .equals(prop.getName())) {
            return false;
        }
        return true;
    }

    private TemplateDataDictionary buildStandardDataDictionary(
            final RequestFactoryType type, final String moduleName) {
        final JavaType javaType = new JavaType(getFullyQualifiedTypeName(type,
                moduleName));
        final TemplateDataDictionary dataDictionary = TemplateDictionary
                .create();
        for (final RequestFactoryType reference : type.getReferences()) {
            addReference(dataDictionary, reference, moduleName);
        }
        dataDictionary.setVariable("className", javaType.getSimpleTypeName());
        dataDictionary.setVariable("packageName", javaType.getPackage()
                .getFullyQualifiedPackageName());
        dataDictionary.setVariable("placePackage", RequestFactoryPath.SCAFFOLD_PLACE
                .packageName(projectOperations.getTopLevelPackage(moduleName)));
        dataDictionary.setVariable("sharedScaffoldPackage",
                RequestFactoryPath.SHARED_SCAFFOLD.packageName(projectOperations
                        .getTopLevelPackage(moduleName)));
        dataDictionary.setVariable("sharedAccountPackage", RequestFactoryPath.SHARED_ACCOUNT
                .packageName(projectOperations.getTopLevelPackage(moduleName)));
        return dataDictionary;
    }

    public String buildUiXml(final String templateContents,
            final String destFile, final List<MethodMetadata> proxyMethods) {
        FileReader fileReader = null;
        try {
            final DocumentBuilder builder = XmlUtils.getDocumentBuilder();
            builder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(final String publicId,
                        final String systemId) throws SAXException, IOException {
                    if (systemId
                            .equals("http://dl.google.com/gwt/DTD/xhtml.ent")) {
                        return new InputSource(FileUtils.getInputStream(
                                GwtBootstrapScaffoldMetadata.class,
                                "templates/xhtml.ent"));
                    }

                    // Use the default behaviour
                    return null;
                }
            });

            InputSource source = new InputSource();
            source.setCharacterStream(new StringReader(templateContents));

            final Document templateDocument = builder.parse(source);

            if (!new File(destFile).exists()) {
                return transformXml(templateDocument);
            }

            source = new InputSource();
            fileReader = new FileReader(destFile);
            source.setCharacterStream(fileReader);
            final Document existingDocument = builder.parse(source);

            // Look for the element holder denoted by the 'debugId' attribute
            // first
            Element existingHoldingElement = XmlUtils.findFirstElement(
                    "//*[@debugId='" + "boundElementHolder" + "']",
                    existingDocument.getDocumentElement());
            Element templateHoldingElement = XmlUtils.findFirstElement(
                    "//*[@debugId='" + "boundElementHolder" + "']",
                    templateDocument.getDocumentElement());

            // If holding element isn't found then the holding element is either
            // not widget based or using the old convention of 'id' so look for
            // the element holder with an 'id' attribute
            if (existingHoldingElement == null) {
                existingHoldingElement = XmlUtils.findFirstElement("//*[@id='"
                        + "boundElementHolder" + "']",
                        existingDocument.getDocumentElement());
            }
            if (templateHoldingElement == null) {
                templateHoldingElement = XmlUtils.findFirstElement("//*[@id='"
                        + "boundElementHolder" + "']",
                        templateDocument.getDocumentElement());
            }

            if (existingHoldingElement != null) {
                final Map<String, Element> templateElementMap = new LinkedHashMap<String, Element>();
                for (final Element element : XmlUtils.findElements("//*[@id]",
                        templateHoldingElement)) {
                    templateElementMap.put(element.getAttribute("id"), element);
                }

                final Map<String, Element> existingElementMap = new LinkedHashMap<String, Element>();
                for (final Element element : XmlUtils.findElements("//*[@id]",
                        existingHoldingElement)) {
                    existingElementMap.put(element.getAttribute("id"), element);
                }

                if (existingElementMap.keySet().containsAll(
                        templateElementMap.values())) {
                    return transformXml(existingDocument);
                }

                final List<Element> elementsToAdd = new ArrayList<Element>();
                for (final Map.Entry<String, Element> entry : templateElementMap
                        .entrySet()) {
                    if (!existingElementMap.keySet().contains(entry.getKey())) {
                        elementsToAdd.add(entry.getValue());
                    }
                }

                final List<Element> elementsToRemove = new ArrayList<Element>();
                for (final Map.Entry<String, Element> entry : existingElementMap
                        .entrySet()) {
                    if (!templateElementMap.keySet().contains(entry.getKey())) {
                        elementsToRemove.add(entry.getValue());
                    }
                }

                for (final Element element : elementsToAdd) {
                    final Node importedNode = existingDocument.importNode(
                            element, true);
                    existingHoldingElement.appendChild(importedNode);
                }

                for (final Element element : elementsToRemove) {
                    existingHoldingElement.removeChild(element);
                }

                if (elementsToAdd.size() > 0) {
                    final List<Element> sortedElements = new ArrayList<Element>();
                    for (final MethodMetadata method : proxyMethods) {
                        final String propertyName = StringUtils
                                .uncapitalize(BeanInfoUtils
                                        .getPropertyNameForJavaBeanMethod(
                                                method).getSymbolName());
                        final Element element = XmlUtils.findFirstElement(
                                "//*[@id='" + propertyName + "']",
                                existingHoldingElement);
                        if (element != null) {
                            sortedElements.add(element);
                        }
                    }
                    for (final Element el : sortedElements) {
                        if (el.getParentNode() != null
                                && el.getParentNode().equals(
                                        existingHoldingElement)) {
                            existingHoldingElement.removeChild(el);
                        }
                    }
                    for (final Element el : sortedElements) {
                        existingHoldingElement.appendChild(el);
                    }
                }

                return transformXml(existingDocument);
            }

            return transformXml(templateDocument);
        }
        catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        finally {
            IOUtils.closeQuietly(fileReader);
        }
    }

    private JavaType getCollectionImplementation(final JavaType javaType) {
        if (isSameBaseType(javaType, SET)) {
            return new JavaType(HASH_SET.getFullyQualifiedTypeName(),
                    javaType.getArray(), javaType.getDataType(),
                    javaType.getArgName(), javaType.getParameters());
        }
        if (isSameBaseType(javaType, LIST)) {
            return new JavaType(ARRAY_LIST.getFullyQualifiedTypeName(),
                    javaType.getArray(), javaType.getDataType(),
                    javaType.getArgName(), javaType.getParameters());
        }
        return javaType;
    }

    private JavaType getDestinationJavaType(final RequestFactoryType destType,
            final String moduleName) {
        return new JavaType(getFullyQualifiedTypeName(destType, moduleName));
    }

    private String getFullyQualifiedTypeName(final RequestFactoryType requestFactoryType,
            final String moduleName) {
        return requestFactoryType.getPath().packageName(
                projectOperations.getTopLevelPackage(moduleName))
                + "." + requestFactoryType.getTemplate();
    }

    public RequestFactoryTemplateDataHolder getMirrorTemplateTypeDetails(
            final ClassOrInterfaceTypeDetails mirroredType,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final String moduleName) {
        final ClassOrInterfaceTypeDetails proxy = requestFactoryTypeService
                .lookupProxyFromEntity(mirroredType);
        final ClassOrInterfaceTypeDetails request = requestFactoryTypeService
                .lookupUnmanagedRequestFromEntity(mirroredType);
        final JavaPackage topLevelPackage = projectOperations
                .getTopLevelPackage(moduleName);
        final Map<RequestFactoryType, JavaType> mirrorTypeMap = RequestFactoryUtils.getMirrorTypeMap(
                mirroredType.getName(), topLevelPackage);
        mirrorTypeMap.put(RequestFactoryType.PROXY, proxy.getName());
        mirrorTypeMap.put(RequestFactoryType.REQUEST, request.getName());

        final Map<RequestFactoryType, ClassOrInterfaceTypeDetails> templateTypeDetailsMap = new LinkedHashMap<RequestFactoryType, ClassOrInterfaceTypeDetails>();
        final Map<RequestFactoryType, String> xmlTemplates = new LinkedHashMap<RequestFactoryType, String>();
        for (final RequestFactoryType requestFactoryType : RequestFactoryType.getMirrorTypes()) {
            if (requestFactoryType.getTemplate() == null) {
                continue;
            }
            TemplateDataDictionary dataDictionary = buildMirrorDataDictionary(
                    requestFactoryType, mirroredType, proxy, mirrorTypeMap,
                    clientSideTypeMap, moduleName);
            requestFactoryType.dynamicallyResolveFieldsToWatch(clientSideTypeMap);
            requestFactoryType.dynamicallyResolveMethodsToWatch(mirroredType.getName(),
                    clientSideTypeMap, topLevelPackage);
            templateTypeDetailsMap.put(
                    requestFactoryType,
                    getTemplateDetails(dataDictionary, requestFactoryType.getTemplate(),
                            mirrorTypeMap.get(requestFactoryType), moduleName));

            if (requestFactoryType.isCreateUiXml()) {
                dataDictionary = buildMirrorDataDictionary(requestFactoryType,
                        mirroredType, proxy, mirrorTypeMap, clientSideTypeMap,
                        moduleName);
                final String contents = getTemplateContents(
                        requestFactoryType.getTemplate() + "UiXml", dataDictionary);
                xmlTemplates.put(requestFactoryType, contents);
            }
        }

        final Map<String, String> xmlMap = new LinkedHashMap<String, String>();
        final List<ClassOrInterfaceTypeDetails> typeDetails = new ArrayList<ClassOrInterfaceTypeDetails>();
        for (final RequestFactoryProxyProperty proxyProperty : clientSideTypeMap.values()) {
            if (!proxyProperty.isCollection()
                    || proxyProperty.isCollectionOfProxy()) {
                continue;
            }

            TemplateDataDictionary dataDictionary = TemplateDictionary.create();
            dataDictionary.setVariable("packageName",
                    RequestFactoryPath.MANAGED_UI_EDITOR.packageName(topLevelPackage));
            dataDictionary.setVariable("scaffoldUiPackage",
                    RequestFactoryPath.SCAFFOLD_UI.packageName(topLevelPackage));
            final JavaType collectionTypeImpl = getCollectionImplementation(proxyProperty
                    .getPropertyType());
            addImport(dataDictionary, collectionTypeImpl);
            addImport(dataDictionary, proxyProperty.getPropertyType());

            final String collectionType = proxyProperty.getPropertyType()
                    .getSimpleTypeName();
            final String boundCollectionType = proxyProperty.getPropertyType()
                    .getParameters().get(0).getSimpleTypeName();

            dataDictionary.setVariable("collectionType", collectionType);
            dataDictionary.setVariable("collectionTypeImpl",
                    collectionTypeImpl.getSimpleTypeName());
            dataDictionary.setVariable("boundCollectionType",
                    boundCollectionType);

            final JavaType collectionEditorType = new JavaType(
                    RequestFactoryPath.MANAGED_UI_EDITOR.packageName(topLevelPackage) + "."
                            + boundCollectionType + collectionType + "Editor");
            typeDetails.add(getTemplateDetails(dataDictionary,
                    "CollectionEditor", collectionEditorType, moduleName));

            dataDictionary = TemplateDictionary.create();
            dataDictionary.setVariable("packageName",
                    RequestFactoryPath.MANAGED_UI_EDITOR.packageName(topLevelPackage));
            dataDictionary.setVariable("scaffoldUiPackage",
                    RequestFactoryPath.SCAFFOLD_UI.packageName(topLevelPackage));
            dataDictionary.setVariable("collectionType", collectionType);
            dataDictionary.setVariable("collectionTypeImpl",
                    collectionTypeImpl.getSimpleTypeName());
            dataDictionary.setVariable("boundCollectionType",
                    boundCollectionType);
            addImport(dataDictionary, proxyProperty.getPropertyType());

            final String contents = getTemplateContents("CollectionEditor"
                    + "UiXml", dataDictionary);
            final String packagePath = projectOperations.getPathResolver()
                    .getFocusedIdentifier(Path.SRC_MAIN_JAVA,
                            RequestFactoryPath.MANAGED_UI_EDITOR.getPackagePath(topLevelPackage));
            xmlMap.put(packagePath + "/" + boundCollectionType + collectionType
                    + "Editor.ui.xml", contents);
        }

        return new RequestFactoryTemplateDataHolder(templateTypeDetailsMap, xmlTemplates,
                typeDetails, xmlMap);
    }

    private String getRequestMethodCall(
            final ClassOrInterfaceTypeDetails request,
            final MemberTypeAdditions memberTypeAdditions) {
        final String methodName = memberTypeAdditions.getMethodName();
        final MethodMetadata requestMethod = MemberFindingUtils.getMethod(
                request, methodName);
        String requestMethodCall = memberTypeAdditions.getMethodName();
        if (requestMethod != null) {
            if (INSTANCE_REQUEST.getFullyQualifiedTypeName().equals(
                    requestMethod.getReturnType().getFullyQualifiedTypeName())) {
                requestMethodCall = requestMethodCall + "().using";
            }
        }
        return requestMethodCall;
    }

    public List<ClassOrInterfaceTypeDetails> getStaticTemplateTypeDetails(
            final RequestFactoryType type, final String moduleName) {
        final List<ClassOrInterfaceTypeDetails> templateTypeDetails = new ArrayList<ClassOrInterfaceTypeDetails>();
        final TemplateDataDictionary dataDictionary = buildDictionary(type,
                moduleName);
        templateTypeDetails.add(getTemplateDetails(dataDictionary,
                type.getTemplate(), getDestinationJavaType(type, moduleName),
                moduleName));
        return templateTypeDetails;
    }

    private String getTemplateContents(final String templateName,
            final TemplateDataDictionary dataDictionary) {
        try {
            final TemplateLoader templateLoader = TemplateResourceLoader
                    .create();
            final Template template = templateLoader.getTemplate(templateName);
            return template.renderToString(dataDictionary);
        }
        catch (final TemplateException e) {
            throw new IllegalStateException(e);
        }
    }

    public ClassOrInterfaceTypeDetails getTemplateDetails(
            final TemplateDataDictionary dataDictionary,
            final String templateFile, final JavaType templateType,
            final String moduleName) {
        try {
            final TemplateLoader templateLoader = TemplateResourceLoader
                    .create();
            final Template template = templateLoader.getTemplate(templateFile);
            Validate.notNull(template, "Template required for '" + templateFile
                    + "'");
            final String templateContents = template
                    .renderToString(dataDictionary);
            final String templateId = PhysicalTypeIdentifier.createIdentifier(
                    templateType,
                    LogicalPath.getInstance(Path.SRC_MAIN_JAVA, moduleName));
            return typeParsingService.getTypeFromString(templateContents,
                    templateId, templateType);
        }
        catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isReadOnly(final String name,
            final ClassOrInterfaceTypeDetails governorTypeDetails) {
        final List<String> readOnly = new ArrayList<String>();
        final ClassOrInterfaceTypeDetails proxy = requestFactoryTypeService
                .lookupProxyFromEntity(governorTypeDetails);
        if (proxy != null) {
            readOnly.addAll(RequestFactoryUtils.getAnnotationValues(proxy,
                    ROO_REQUEST_FACTORY_PROXY, "readOnly"));
        }

        return readOnly.contains(name);
    }

    private boolean isSameBaseType(final JavaType type1, final JavaType type2) {
        return type1.getFullyQualifiedTypeName().equals(
                type2.getFullyQualifiedTypeName());
    }

    private void maybeAddImport(final TemplateDataDictionary dataDictionary,
            final Set<String> importSet, final JavaType type) {
        if (!importSet.contains(type.getFullyQualifiedTypeName())) {
            addImport(dataDictionary, type.getFullyQualifiedTypeName());
            importSet.add(type.getFullyQualifiedTypeName());
        }
    }

    private String transformXml(final Document document)
            throws TransformerException {
        final Transformer transformer = XmlUtils.createIndentingTransformer();
        final DOMSource source = new DOMSource(document);
        final StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(source, result);
        return result.getWriter().toString();
    }
}
