package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY;
import hapax.TemplateDataDictionary;
import hapax.TemplateDictionary;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
import org.springframework.roo.addon.requestfactory.BaseTemplateServiceImpl;
import org.springframework.roo.addon.requestfactory.RequestFactoryPath;
import org.springframework.roo.addon.requestfactory.RequestFactoryProxyProperty;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateDataHolder;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateService;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateServiceImpl;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeService;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.entity.RooRequestFactory;
import org.springframework.roo.addon.requestfactory.scaffold.RequestFactoryScaffoldMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeParsingService;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.layers.LayerService;
import org.springframework.roo.classpath.persistence.PersistenceMemberLocator;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
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
public class GwtBootstrapTemplateServiceImpl extends BaseTemplateServiceImpl
        implements GwtBootstrapTemplateService {

    private static final String TEMPLATE_DIR = "org/springframework/roo/addon/requestfactory/gwt/bootstrap/scaffold/templates/";

    protected TemplateDataDictionary buildDictionary(final RequestFactoryType type,
            final String moduleName) {
        final Set<ClassOrInterfaceTypeDetails> proxies = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY);
        final TemplateDataDictionary dataDictionary = buildStandardDataDictionary(
                type, moduleName);
        if (type == GwtBootstrapType.MASTER_ACTIVITIES) {
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
                            GwtBootstrapType.LIST_ACTIVITY, moduleName);
                    addImport(dataDictionary, proxy.getName()
                            .getFullyQualifiedTypeName());
                    addImport(dataDictionary, entitySimpleName,
                            GwtBootstrapType.DESKTOP_LIST_VIEW, moduleName);
                    addImport(dataDictionary, entitySimpleName,
                            GwtBootstrapType.MOBILE_LIST_VIEW, moduleName);
                }
            }
        } else if (type == GwtBootstrapType.LIST_PLACE_RENDERER) {
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
        } else if (type == GwtBootstrapType.DETAILS_ACTIVITIES) {
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
                            GwtBootstrapType.ACTIVITIES_MAPPER.getPath().packageName(
                                    projectOperations
                                            .getTopLevelPackage(moduleName))
                                    + "."
                                    + entitySimpleName
                                    + GwtBootstrapType.ACTIVITIES_MAPPER.getSuffix());
                }
            }
        } else if (type == GwtBootstrapType.MOBILE_ACTIVITIES) {
            // do nothing
        } else if (type == GwtBootstrapType.PROXY_NODE_PROCESSOR
                || type == GwtBootstrapType.PROXY_LIST_NODE_PROCESSOR
                || type == GwtBootstrapType.IS_LEAF_PROCESSOR) {
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
                            + GwtBootstrapType.DATA_PROVIDER.getSuffix();
                    final String providerFullName = GwtBootstrapType.DATA_PROVIDER.
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
                    GwtBootstrapPaths.SCAFFOLD_UI.packageName(projectOperations
                            .getTopLevelPackage(moduleName)));
        }

        return dataDictionary;
    }

    @Override
    protected TemplateDataDictionary buildMirrorDataDictionary(
            final RequestFactoryType type, final ClassOrInterfaceTypeDetails mirroredType,
            final ClassOrInterfaceTypeDetails proxy,
            final Map<RequestFactoryType, JavaType> mirrorTypeMap,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final String moduleName) {
        final JavaType proxyType = proxy.getName();

        final TemplateDataDictionary dataDictionary = super.buildMirrorDataDictionary(
                type, mirroredType, proxy, mirrorTypeMap, clientSideTypeMap, moduleName);

        // Get my locator and
        final JavaType entity = mirroredType.getName();
        final String entityName = entity.getFullyQualifiedTypeName();
        final JavaType idType = persistenceMemberLocator
                .getIdentifierType(entity);
        Validate.notNull(idType,
                "Identifier type is not available for entity '" + entityName
                        + "'");

        final String simpleTypeName = mirroredType.getName()
                .getSimpleTypeName();
        final JavaPackage topLevelPackage = projectOperations
                .getTopLevelPackage(moduleName);

        dataDictionary.setVariable("placePackage",
                GwtBootstrapPaths.SCAFFOLD_PLACE.packageName(topLevelPackage));
        dataDictionary.setVariable("scaffoldUiPackage",
                GwtBootstrapPaths.SCAFFOLD_UI.packageName(topLevelPackage));
        dataDictionary.setVariable("uiPackage",
                GwtBootstrapPaths.MANAGED_UI.packageName(topLevelPackage));
        dataDictionary.setVariable("uiEditorPackage",
                GwtBootstrapPaths.MANAGED_UI_EDITOR.packageName(topLevelPackage));

        dataDictionary.setVariable("proxyRenderer", GwtBootstrapProxyProperty
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
        if (type == GwtBootstrapType.EDIT_ACTIVITY_WRAPPER
                || type == GwtBootstrapType.MOBILE_EDIT_VIEW
                || type == GwtBootstrapType.DESKTOP_EDIT_VIEW) {
            List<String> existingDesktopFields = new ArrayList<String>();
            List<String> existingMobileFields = new ArrayList<String>();

            try {
                String className = GwtBootstrapPaths.MANAGED_UI_DESKTOP
                        .packageName(topLevelPackage)
                        + "."
                        + simpleTypeName
                        + GwtBootstrapType.DESKTOP_EDIT_VIEW.getTemplate();

                ClassOrInterfaceTypeDetails details = typeLocationService
                        .getTypeDetails(new JavaType(className));

                if (details != null) {
                    for (FieldMetadata field : details.getDeclaredFields()) {
                        JavaSymbolName fieldName = field.getFieldName();
                        String name = fieldName.toString();
                        existingDesktopFields.add(name);
                    }
                }

                className = GwtBootstrapPaths.MANAGED_UI_MOBILE
                        .packageName(topLevelPackage)
                        + "."
                        + simpleTypeName
                        + GwtBootstrapType.MOBILE_EDIT_VIEW.getTemplate();

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
                if (type == GwtBootstrapType.MOBILE_EDIT_VIEW)
                    existingEditViewFields = existingMobileFields;

                // Adds names of fields in DesktopEditView to existingFields
                // list
                if (type == GwtBootstrapType.DESKTOP_EDIT_VIEW)
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

        if (type == GwtBootstrapType.MOBILE_DETAILS_VIEW
                || type == GwtBootstrapType.DESKTOP_DETAILS_VIEW) {
            List<String> existingDesktopFields = new ArrayList<String>();
            List<String> existingMobileFields = new ArrayList<String>();

            try {
                String className = GwtBootstrapPaths.MANAGED_UI_DESKTOP
                        .packageName(topLevelPackage)
                        + "."
                        + simpleTypeName
                        + GwtBootstrapType.DESKTOP_DETAILS_VIEW.getTemplate();

                ClassOrInterfaceTypeDetails details = typeLocationService
                        .getTypeDetails(new JavaType(className));

                if (details != null) {
                    for (FieldMetadata field : details.getDeclaredFields()) {
                        JavaSymbolName fieldName = field.getFieldName();
                        String name = fieldName.toString();
                        existingDesktopFields.add(name);
                    }
                }

                className = GwtBootstrapPaths.MANAGED_UI_MOBILE
                        .packageName(topLevelPackage)
                        + "."
                        + simpleTypeName
                        + GwtBootstrapType.MOBILE_DETAILS_VIEW.getTemplate();

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
                if (type == GwtBootstrapType.MOBILE_DETAILS_VIEW)
                    existingDetailsViewFields = existingMobileFields;

                // Adds names of fields in DesktopDetailsView to existingFields
                // list
                if (type == GwtBootstrapType.DESKTOP_DETAILS_VIEW)
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

    @Override
    protected TemplateDataDictionary buildStandardDataDictionary(
            final RequestFactoryType type, final String moduleName) {
        final TemplateDataDictionary dataDictionary = super
                .buildStandardDataDictionary(type, moduleName);
        dataDictionary.setVariable("placePackage", GwtBootstrapPaths.SCAFFOLD_PLACE
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
                                RequestFactoryScaffoldMetadata.class,
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

    @Override
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
        for (final GwtBootstrapType gwtBootstrapType : GwtBootstrapType.getGwtBootstrapMirrorTypes()) {
            if (gwtBootstrapType.getTemplate() == null) {
                continue;
            }
            TemplateDataDictionary dataDictionary = buildMirrorDataDictionary(
                    gwtBootstrapType, mirroredType, proxy, mirrorTypeMap,
                    clientSideTypeMap, moduleName);
            gwtBootstrapType.dynamicallyResolveFieldsToWatch(clientSideTypeMap);
            gwtBootstrapType.dynamicallyResolveMethodsToWatch(mirroredType.getName(),
                    clientSideTypeMap, topLevelPackage);
            templateTypeDetailsMap.put(
                    gwtBootstrapType,
                    getTemplateDetails(dataDictionary, gwtBootstrapType.getTemplate(),
                            mirrorTypeMap.get(gwtBootstrapType), moduleName, TEMPLATE_DIR));

            if (gwtBootstrapType.isCreateUiXml()) {
                dataDictionary = buildMirrorDataDictionary(gwtBootstrapType,
                        mirroredType, proxy, mirrorTypeMap, clientSideTypeMap,
                        moduleName);
                final String contents = getTemplateContents(
                        gwtBootstrapType.getTemplate() + "UiXml", dataDictionary,
                        TEMPLATE_DIR);
                xmlTemplates.put(gwtBootstrapType, contents);
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
                    GwtBootstrapPaths.MANAGED_UI_EDITOR.packageName(topLevelPackage));
            dataDictionary.setVariable("scaffoldUiPackage",
                    GwtBootstrapPaths.SCAFFOLD_UI.packageName(topLevelPackage));
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
                    GwtBootstrapPaths.MANAGED_UI_EDITOR.packageName(topLevelPackage) + "."
                            + boundCollectionType + collectionType + "Editor");
            typeDetails.add(getTemplateDetails(dataDictionary,
                    "CollectionEditor", collectionEditorType, moduleName,
                    TEMPLATE_DIR));

            dataDictionary = TemplateDictionary.create();
            dataDictionary.setVariable("packageName",
                    GwtBootstrapPaths.MANAGED_UI_EDITOR.packageName(topLevelPackage));
            dataDictionary.setVariable("scaffoldUiPackage",
                    GwtBootstrapPaths.SCAFFOLD_UI.packageName(topLevelPackage));
            dataDictionary.setVariable("collectionType", collectionType);
            dataDictionary.setVariable("collectionTypeImpl",
                    collectionTypeImpl.getSimpleTypeName());
            dataDictionary.setVariable("boundCollectionType",
                    boundCollectionType);
            addImport(dataDictionary, proxyProperty.getPropertyType());

            final String contents = getTemplateContents("CollectionEditor"
                    + "UiXml", dataDictionary, TEMPLATE_DIR);
            final String packagePath = projectOperations.getPathResolver()
                    .getFocusedIdentifier(Path.SRC_MAIN_JAVA,
                            GwtBootstrapPaths.MANAGED_UI_EDITOR.getPackagePath(topLevelPackage));
            xmlMap.put(packagePath + "/" + boundCollectionType + collectionType
                    + "Editor.ui.xml", contents);
        }

        return new RequestFactoryTemplateDataHolder(templateTypeDetailsMap, xmlTemplates,
                typeDetails, xmlMap);
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
