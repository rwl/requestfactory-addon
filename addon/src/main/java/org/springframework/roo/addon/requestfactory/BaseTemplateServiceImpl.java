package org.springframework.roo.addon.requestfactory;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.INSTANCE_REQUEST;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
import static org.springframework.roo.addon.requestfactory.account.AccountJavaType.ROO_ACCOUNT;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.HELP_TEXT;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.INVISIBLE;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.KEY;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.PASSWORD;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY_ENTITY;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.TEXT_AREA;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.UNEDITABLE;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.UNITS;
import static org.springframework.roo.model.JavaType.INT_PRIMITIVE;
import static org.springframework.roo.model.JavaType.STRING;
import static org.springframework.roo.model.JdkJavaType.ARRAY_LIST;
import static org.springframework.roo.model.JdkJavaType.HASH_SET;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.JdkJavaType.SET;
import static org.springframework.roo.model.Jsr303JavaType.NOT_NULL;
import hapax.Template;
import hapax.TemplateDataDictionary;
import hapax.TemplateDictionary;
import hapax.TemplateException;
import hapax.TemplateLoader;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooRequestFactoryEntity;
import org.springframework.roo.addon.requestfactory.entity.EntityDataKeys;
import org.springframework.roo.addon.requestfactory.entity.EntityMetadata;
import org.springframework.roo.addon.requestfactory.entity.TextType;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeParsingService;
import org.springframework.roo.classpath.customdata.CustomDataKeys;
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
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;


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
public class BaseTemplateServiceImpl {

    private static final int LAYER_POSITION = LayerType.HIGHEST.getPosition();

    private static final String TEMPLATE_DIR = "org/springframework/roo/addon/requestfactory/scaffold/templates/";

    @Reference protected RequestFactoryTypeService requestFactoryTypeService;
    @Reference protected LayerService layerService;
    @Reference protected MetadataService metadataService;
    @Reference protected PersistenceMemberLocator persistenceMemberLocator;
    @Reference protected ProjectOperations projectOperations;
    @Reference protected TypeLocationService typeLocationService;
    @Reference protected TypeParsingService typeParsingService;

    protected void addImport(final TemplateDataDictionary dataDictionary,
            final JavaType type) {
        dataDictionary.addSection("imports").setVariable("import",
                type.getFullyQualifiedTypeName());
        for (final JavaType param : type.getParameters()) {
            addImport(dataDictionary, param.getFullyQualifiedTypeName());
        }
    }

    protected void addImport(final TemplateDataDictionary dataDictionary,
            final String importDeclaration) {
        dataDictionary.addSection("imports").setVariable("import",
                importDeclaration);
    }

    protected void addImport(final TemplateDataDictionary dataDictionary,
            final String simpleName, final RequestFactoryType requestFactoryType,
            final String moduleName) {
        addImport(
                dataDictionary,
                requestFactoryType.getPath().packageName(
                        projectOperations.getTopLevelPackage(moduleName))
                        + "." + simpleName + requestFactoryType.getSuffix());
    }

    protected void addReference(final TemplateDataDictionary dataDictionary,
            final RequestFactoryType type, final Map<RequestFactoryType,
            JavaType> mirrorTypeMap) {
        JavaType javaType = mirrorTypeMap.get(type);
        String fullyQualifiedTypeName = null;
        try {
            fullyQualifiedTypeName = javaType.getFullyQualifiedTypeName();
        } catch (NullPointerException e) {
            throw e;
        }
        addImport(dataDictionary, fullyQualifiedTypeName);
        dataDictionary.setVariable(type.getName(), javaType
                .getSimpleTypeName());
    }

    protected void addReference(final TemplateDataDictionary dataDictionary,
            final RequestFactoryType type, final String moduleName) {
        addImport(dataDictionary, getDestinationJavaType(type, moduleName)
                .getFullyQualifiedTypeName());
        dataDictionary.setVariable(type.getName(),
                getDestinationJavaType(type, moduleName).getSimpleTypeName());
    }

    protected TemplateDataDictionary buildDictionary(final RequestFactoryType type,
            final String moduleName) {
        final Set<ClassOrInterfaceTypeDetails> proxies = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY);
        final TemplateDataDictionary dataDictionary = buildStandardDataDictionary(
                type, moduleName);
        if (type == RequestFactoryType.APP_ENTITY_TYPES_PROCESSOR) {
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


                    AnnotationMetadata annotation = entity.getAnnotation(ROO_REQUEST_FACTORY_ENTITY);
                    if (annotation != null) {
                        AnnotationAttributeValue<String> attribute = annotation
                                .getAttribute(RooRequestFactoryEntity.PARENT_PROPERTY_ATTRIBUTE);
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
        } else if (type == RequestFactoryType.APP_REQUEST_FACTORY) {
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
            }
            if (typeLocationService.findTypesWithAnnotation(ROO_ACCOUNT).size() != 0) {
                dataDictionary.showSection("hasAccount");
            }
        }

        return dataDictionary;
    }

    protected TemplateDataDictionary buildMirrorDataDictionary(
            final RequestFactoryType type, final ClassOrInterfaceTypeDetails mirroredType,
            final ClassOrInterfaceTypeDetails proxy,
            final Map<RequestFactoryType, JavaType> mirrorTypeMap,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final String moduleName) {
        final JavaType proxyType = proxy.getName();
        final JavaType javaType = mirrorTypeMap.get(type);
        Validate.notNull(javaType);

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
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_ENTITY)) {
            AnnotationMetadata annotation = gwtBootstrapEntity
                    .getAnnotation(ROO_REQUEST_FACTORY_ENTITY);
            if (annotation == null) {
                continue;
            }
            AnnotationAttributeValue<String> annotationAttributeValue = annotation
                    .getAttribute(RooRequestFactoryEntity.PARENT_PROPERTY_ATTRIBUTE);
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


        final String parentPropertyName = RequestFactoryUtils
                .getStringAnnotationValue(mirroredType, ROO_REQUEST_FACTORY_ENTITY,
                        RooRequestFactoryEntity.PARENT_PROPERTY_ATTRIBUTE, "");

        final JavaType nonKeyIdType = idType.equals(KEY) ? STRING : idType;
        final String getId = idType.equals(KEY) ? "getStringId" : "getId";
        dataDictionary.setVariable("getId", getId);

        final String countMethodId, findMethodId, countCall, findCall;
        final List<MethodParameter> findParameters, countParamemters;
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
            dataDictionary.showSection("hasParent");

            countMethodId = EntityDataKeys.COUNT_BY_PARENT_METHOD.name();
            findMethodId = EntityDataKeys.FIND_ENTRIES_BY_PARENT_METHOD.name();
            final String parentId = nonKeyIdType.getSimpleTypeName() + ".valueOf(parentId)";
            countCall = "(" + parentId + ")";
            findCall = "(" + parentId + ", range.getStart(), range.getLength())";
            countParamemters = Arrays.asList(new MethodParameter(
                    nonKeyIdType, parentPropertyName + "Id"));
            findParameters = Arrays.asList(new MethodParameter(
                    nonKeyIdType, parentPropertyName + "Id"), new MethodParameter(
                    INT_PRIMITIVE, "firstResult"), new MethodParameter(
                    INT_PRIMITIVE, "maxResults"));

            final FieldMetadata parentProperty = mirroredType
                    .getField(new JavaSymbolName(parentPropertyName));
            Validate.notNull(parentProperty, "Parent property not found");
            final JavaType parentType = parentProperty.getFieldType();
            final String parentTypeName = parentType.getSimpleTypeName();
            final String parentProxyName = parentTypeName + "Proxy";

            final String setProxyParentStmt = "if (proxy.get"
                    + StringUtils.capitalize(parentPropertyName) + "() == null) {\n"
                    + "factory." + StringUtils.uncapitalize(parentTypeName)
                    + "Request().find" + parentTypeName + (KEY.equals(idType) ? "ByStringId" : "")
                    + "(" + parentId + ").fire(new Receiver<" + parentProxyName + ">() {\n"
                    + "@Override\n"
                    + "public void onSuccess(" + parentProxyName + " response) {\n"
                    + "proxy.set" + StringUtils.capitalize(parentPropertyName) + "(response);\n"
                    + "}\n"
                    + "});\n"
                    + "}\n";
            dataDictionary.setVariable("setProxyParentStmt", setProxyParentStmt);

            final ClassOrInterfaceTypeDetails parentDetails = typeLocationService
                    .getTypeDetails(parentType);
            final String grandParentPropertyName = RequestFactoryUtils
                    .getStringAnnotationValue(parentDetails, ROO_REQUEST_FACTORY_ENTITY,
                            RooRequestFactoryEntity.PARENT_PROPERTY_ATTRIBUTE, "");

            final String gotoParentPlaceStmt = "requests." + StringUtils.uncapitalize(parentTypeName)
                    + "Request().find" + parentTypeName + (KEY.equals(idType) ? "ByStringId" : "")
                    + "(" + parentId + ")"
                    + (grandParentPropertyName.isEmpty() ? "" : ".with(\"" + grandParentPropertyName + "\")")
                    + ".fire(new Receiver<" + parentProxyName + ">() {\n"
                    + "@Override\n"
                    + "public void onSuccess(" + parentProxyName + " response) {\n"
                    + "placeController.goTo(new ProxyPlace(response.stableId(), Operation.DETAILS, "
                    + (grandParentPropertyName.isEmpty() ? "null" : "String.valueOf(response.get" + StringUtils.capitalize(grandParentPropertyName) + "()." + getId + "())")
                    + "));\n"
                    + "}\n"
                    + "});";
            dataDictionary.setVariable("gotoParentPlaceStmt", gotoParentPlaceStmt);
        }

        final MemberTypeAdditions findMethodAdditions = layerService
                .getMemberTypeAdditions(metadataIdentificationString,
                        findMethodId, entity, nonKeyIdType, LAYER_POSITION,
                        findParameters);
        Validate.notNull(findMethodAdditions,
                "Find entries method is not available for entity '" + entityName + "'");
        dataDictionary.setVariable("findEntitiesMethod",
                findMethodAdditions.getMethodName() + findCall);

        final MemberTypeAdditions countMethodAdditions = layerService
                .getMemberTypeAdditions(metadataIdentificationString,
                        countMethodId, entity, nonKeyIdType, LAYER_POSITION,
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
        dataDictionary.setVariable("topLevelPackage", projectOperations
                .getTopLevelPackage(moduleName).getFullyQualifiedPackageName());

        dataDictionary.setVariable("requestPackage",
                RequestFactoryPath.SHARED_MANAGED_REQUEST.packageName(topLevelPackage));
        dataDictionary.setVariable("sharedScaffoldPackage",
                RequestFactoryPath.SHARED_SCAFFOLD.packageName(topLevelPackage));

        dataDictionary.setVariable("name", simpleTypeName);
        dataDictionary.setVariable("pluralName", plural);
        dataDictionary.setVariable("nameUncapitalized",
                StringUtils.uncapitalize(simpleTypeName));
        dataDictionary.setVariable("proxy", proxyType.getSimpleTypeName());

        return dataDictionary;
    }

    protected boolean isPrimaryProp(final RequestFactoryProxyProperty prop,
            final ClassOrInterfaceTypeDetails entity) {
        return isRenderProp(prop, entity, RooRequestFactoryEntity.
                PRIMARY_PROPERTY_ATTRIBUTE);
    }

    protected boolean isSecondaryProp(final RequestFactoryProxyProperty prop,
            final ClassOrInterfaceTypeDetails entity) {
        return isRenderProp(prop, entity, RooRequestFactoryEntity.
                SECONDARY_PROPERTY_ATTRIBUTE);
    }

    private boolean isRenderProp(final RequestFactoryProxyProperty prop,
            final ClassOrInterfaceTypeDetails entity,
            final String propertyAttribute) {
        AnnotationMetadata annotation = entity.getAnnotation(ROO_REQUEST_FACTORY_ENTITY);
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

    protected TemplateDataDictionary buildStandardDataDictionary(
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
        dataDictionary.setVariable("topLevelPackage", projectOperations
                .getTopLevelPackage(moduleName).getFullyQualifiedPackageName());
        dataDictionary.setVariable("sharedScaffoldPackage",
                RequestFactoryPath.SHARED_SCAFFOLD.packageName(projectOperations
                        .getTopLevelPackage(moduleName)));
        dataDictionary.setVariable("sharedAccountPackage", RequestFactoryPath.SHARED_ACCOUNT
                .packageName(projectOperations.getTopLevelPackage(moduleName)));
        return dataDictionary;
    }

    protected JavaType getCollectionImplementation(final JavaType javaType) {
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

    protected JavaType getDestinationJavaType(final RequestFactoryType destType,
            final String moduleName) {
        return new JavaType(getFullyQualifiedTypeName(destType, moduleName));
    }

    protected String getFullyQualifiedTypeName(final RequestFactoryType requestFactoryType,
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
        for (final RequestFactoryType requestFactoryType : RequestFactoryType.getRequestFactoryMirrorTypes()) {
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
                            mirrorTypeMap.get(requestFactoryType), moduleName, TEMPLATE_DIR));
        }

        final Map<String, String> xmlMap = new LinkedHashMap<String, String>();
        final List<ClassOrInterfaceTypeDetails> typeDetails = new ArrayList<ClassOrInterfaceTypeDetails>();

        return new RequestFactoryTemplateDataHolder(templateTypeDetailsMap, xmlTemplates,
                typeDetails, xmlMap);
    }

    protected String getRequestMethodCall(
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
                moduleName, TEMPLATE_DIR));
        return templateTypeDetails;
    }

    protected String getTemplateContents(final String templateName,
            final TemplateDataDictionary dataDictionary,
            final String templateDirectory) {
        try {
            final TemplateLoader templateLoader = TemplateResourceLoader
                    .create(templateDirectory);
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
            final String moduleName, final String templateDirectory) {
        try {
            final TemplateLoader templateLoader = TemplateResourceLoader
                    .create(templateDirectory);
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

    protected boolean isInvisible(final RequestFactoryProxyProperty property,
            final ClassOrInterfaceTypeDetails governorTypeDetails) {
        final JavaSymbolName field = property.getSymbolName();
        if (governorTypeDetails.getField(field) != null
                && governorTypeDetails.getField(field)
                .getAnnotation(INVISIBLE) != null) {
            return true;
        }

        final JavaSymbolName getter = property.getGetterSymbolName();
        if (governorTypeDetails.getMethod(getter) != null
                && governorTypeDetails.getMethod(getter)
                .getAnnotation(INVISIBLE) != null) {
            return true;
        }

        final JavaType entity = governorTypeDetails.getType();
        if (persistenceMemberLocator.getVersionAccessor(entity) != null
                && persistenceMemberLocator.getVersionAccessor(entity)
                .getMethodName().equals(getter)) {
            return true;
        }
        if (persistenceMemberLocator.getIdentifierAccessor(entity) != null
                && persistenceMemberLocator.getIdentifierAccessor(entity)
                .getMethodName().equals(getter)) {
            return true;
        }

        if (KEY.equals(persistenceMemberLocator.getIdentifierType(entity))
                && getter.equals(EntityMetadata.STRING_ID_GETTER)) {
            return true;
        }

        return false;
    }

    protected boolean isUneditable(final RequestFactoryProxyProperty property,
            final ClassOrInterfaceTypeDetails governorTypeDetails) {
        if (governorTypeDetails.getField(property.getSymbolName()) != null
                && governorTypeDetails.getField(property.getSymbolName())
                .getAnnotation(UNEDITABLE) != null) {
            return true;
        }
        if (governorTypeDetails.getMethod(property.getGetterSymbolName()) != null
                && governorTypeDetails.getMethod(property.getGetterSymbolName())
                .getAnnotation(UNEDITABLE) != null) {
            return true;
        }
        return false;
    }

    protected boolean isNotNull(final RequestFactoryProxyProperty property,
            final ClassOrInterfaceTypeDetails governorTypeDetails) {
        if (governorTypeDetails.getField(property.getSymbolName()) != null
                && governorTypeDetails.getField(property.getSymbolName())
                .getAnnotation(NOT_NULL) != null) {
            return true;
        }
        if (governorTypeDetails.getMethod(property.getGetterSymbolName()) != null
                && governorTypeDetails.getMethod(property.getGetterSymbolName())
                .getAnnotation(NOT_NULL) != null) {
            return true;
        }
        return false;
    }

    protected boolean isReadOnly(final String name,
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

    protected TextType getTextType(final JavaSymbolName fieldName,
            final ClassOrInterfaceTypeDetails governorTypeDetails) {
        FieldMetadata fieldMetadata = governorTypeDetails.getField(fieldName);
        if (fieldMetadata != null) {
            if (fieldMetadata.getAnnotation(TEXT_AREA) != null) {
                return TextType.MULTILINE;
            } else if (fieldMetadata.getAnnotation(PASSWORD) != null) {
                return TextType.PASSWORD;
            }
        }
        return TextType.NORMAL;
    }

    protected String getHelpText(final JavaSymbolName fieldName,
            final ClassOrInterfaceTypeDetails governorTypeDetails) {
        FieldMetadata fieldMetadata = governorTypeDetails.getField(fieldName);
        if (fieldMetadata != null) {
            AnnotationMetadata annotationMetadata = fieldMetadata
                    .getAnnotation(HELP_TEXT);
            if (annotationMetadata != null) {
                AnnotationAttributeValue<String> aav = annotationMetadata
                        .getAttribute("value");
                if (aav != null) {
                    return "<b:HelpBlock>" + aav.getValue() + "</b:HelpBlock>";
                }
            }
        }
        return "";
    }

    protected String getUnits(final JavaSymbolName fieldName,
            final ClassOrInterfaceTypeDetails governorTypeDetails) {
        FieldMetadata fieldMetadata = governorTypeDetails.getField(fieldName);
        if (fieldMetadata != null) {
            AnnotationMetadata annotationMetadata = fieldMetadata
                    .getAnnotation(UNITS);
            if (annotationMetadata != null) {
                AnnotationAttributeValue<String> aav = annotationMetadata
                        .getAttribute("value");
                if (aav != null) {
                    return "<b:HelpInline>" + aav.getValue() + "</b:HelpInline>";
                }
            }
        }
        return "";
    }

    protected boolean isSameBaseType(final JavaType type1, final JavaType type2) {
        return type1.getFullyQualifiedTypeName().equals(
                type2.getFullyQualifiedTypeName());
    }

    protected void maybeAddImport(final TemplateDataDictionary dataDictionary,
            final Set<String> importSet, final JavaType type) {
        if (!importSet.contains(type.getFullyQualifiedTypeName())) {
            addImport(dataDictionary, type.getFullyQualifiedTypeName());
            importSet.add(type.getFullyQualifiedTypeName());
        }
    }
}
