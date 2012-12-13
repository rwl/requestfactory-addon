package org.springframework.roo.addon.requestfactory.gwt;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
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
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.addon.requestfactory.RequestFactoryProxyProperty;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateDataHolder;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateService;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeService;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.entity.TextType;
import org.springframework.roo.addon.requestfactory.gwt.scaffold.GwtScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
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
public class GwtTemplateServiceImpl implements GwtTemplateService {

    private static final String TEMPLATE_DIR = "org/springframework/roo/addon/requestfactory/gwt/scaffold/templates/";

    @Reference RequestFactoryTypeService requestFactoryTypeService;
    @Reference RequestFactoryTemplateService templateService;
    @Reference ProjectOperations projectOperations;
    @Reference TypeLocationService typeLocationService;
    @Reference MetadataService metadataService;
    @Reference PersistenceMemberLocator persistenceMemberLocator;
    
    @Override
    public TemplateDataDictionary buildDictionary(final RequestFactoryType type,
            final String moduleName) {
        final Set<ClassOrInterfaceTypeDetails> proxies = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY);
        final String proxyModuleName = PhysicalTypeIdentifier.getPath(
                proxies.iterator().next().getDeclaredByMetadataId()).getModule();
        final TemplateDataDictionary dataDictionary = buildStandardDataDictionary(
                type, moduleName, proxyModuleName);
        if (type == GwtType.MASTER_ACTIVITIES) {
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
                    section.setVariable("entitySimpleNameUncapitalize",
                            StringUtils.uncapitalize(entitySimpleName));
                    section.setVariable("entityFullPath", proxySimpleName);
                    section.setVariable("proxySimpleName", proxySimpleName);
                    section.setVariable("proxySimpleNameUncapitalize",
                            StringUtils.uncapitalize(proxySimpleName));
                    templateService.addImport(dataDictionary, entitySimpleName,
                            GwtType.LIST_ACTIVITY, moduleName);
                    templateService.addImport(dataDictionary, proxy.getName()
                            .getFullyQualifiedTypeName());
                    templateService.addImport(
                            dataDictionary,
                            GwtType.ACTIVITIES_MAPPER.getPath().packageName(
                                    projectOperations
                                            .getTopLevelPackage(moduleName))
                                    + "."
                                    + entitySimpleName
                                    + GwtType.ACTIVITIES_MAPPER.getSuffix());
                    templateService.addImport(
                            dataDictionary,
                            GwtType.LIST_ACTIVITIES_MAPPER.getPath().packageName(
                                    projectOperations
                                            .getTopLevelPackage(moduleName))
                                    + "."
                                    + entitySimpleName
                                    + GwtType.LIST_ACTIVITIES_MAPPER.getSuffix());
                }
            }
        } else if (type == GwtType.LIST_PLACE_RENDERER
                || type == GwtType.PROXY_PLACE_RENDERER) {
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

                    final String pluralMetadataKey = PluralMetadata.createIdentifier(
                            entity.getName(), PhysicalTypeIdentifier
                                    .getPath(entity.getDeclaredByMetadataId()));
                    final PluralMetadata pluralMetadata = (PluralMetadata) metadataService
                            .get(pluralMetadataKey);
                    final String plural = pluralMetadata.getPlural();

                    final TemplateDataDictionary section = dataDictionary
                            .addSection("entities");
                    section.setVariable("entitySimpleName", entitySimpleName);
                    section.setVariable("entitySimpleNameUncapitalised",
                            StringUtils.uncapitalize(entitySimpleName));
                    section.setVariable("entityPluralName", plural);
                    section.setVariable("entityFullPath", proxySimpleName);
                    templateService.addImport(dataDictionary, proxy.getName()
                            .getFullyQualifiedTypeName());
                }
            }
        } else if (type == GwtType.MOBILE_ACTIVITY_MAPPER) {
            // do nothing
        } else if (type == GwtType.PROXY_NODE_PROCESSOR
                || type == GwtType.PROXY_LIST_NODE_PROCESSOR
                || type == GwtType.IS_LEAF_PROCESSOR) {
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
                            + GwtType.NODE_DATA_PROVIDER.getSuffix();
                    final String providerFullName = GwtType.NODE_DATA_PROVIDER.
                            getPath().packageName(topLevelPackage)
                            + "." + providerSimpleName;
                    final TemplateDataDictionary section = dataDictionary
                            .addSection("entities");
                    section.setVariable("entitySimpleName", entitySimpleName);
                    section.setVariable("entityFullPath", proxySimpleName);
                    templateService.addImport(dataDictionary, proxy.getName()
                            .getFullyQualifiedTypeName());
                    section.setVariable("providerSimpleName", providerSimpleName);
                    templateService.addImport(dataDictionary, providerFullName);

                    Boolean isLeaf = true;

                    for (final ClassOrInterfaceTypeDetails p : proxies) {
                        if (!RequestFactoryUtils.scaffoldProxy(p)) {
                            continue;
                        }
                        final ClassOrInterfaceTypeDetails ety = requestFactoryTypeService
                                .lookupEntityFromProxy(p);
                        if (ety != null) {

                        final FieldMetadata parentProperty = requestFactoryTypeService.getParentField(ety);
                        if (parentProperty == null) {
                            continue;
                        }
                        Validate.notNull(parentProperty, "Parent property not found");

                        if (parentProperty.getFieldType().equals(entity.getType())) {
                            isLeaf = false;

                            final ClassOrInterfaceTypeDetails proxyForEntity = requestFactoryTypeService
                                    .lookupProxyFromEntity(ety);
                            section.addSection("children").setVariable("child",
                                    proxyForEntity.getName().getSimpleTypeName());
                            templateService.addImport(dataDictionary, proxyForEntity.getName()
                                    .getFullyQualifiedTypeName());
                        }
                        }
                    }
                    section.setVariable("isLeaf", isLeaf.toString());
                }
            }
            dataDictionary.setVariable("scaffoldUiPackage",
                    GwtPaths.SCAFFOLD_UI.packageName(projectOperations
                            .getTopLevelPackage(moduleName)));
        }

        return dataDictionary;
    }

    @Override
    public TemplateDataDictionary buildMirrorDataDictionary(
            final RequestFactoryType type, final ClassOrInterfaceTypeDetails mirroredType,
            final ClassOrInterfaceTypeDetails proxy,
            final Map<RequestFactoryType, JavaType> mirrorTypeMap,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final String moduleName) {
        final JavaType proxyType = proxy.getName();

        final TemplateDataDictionary dataDictionary = templateService.buildMirrorDataDictionary(
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
                GwtPaths.SCAFFOLD_PLACE.packageName(topLevelPackage));
        dataDictionary.setVariable("scaffoldUiPackage",
                GwtPaths.SCAFFOLD_UI.packageName(topLevelPackage));
        dataDictionary.setVariable("uiPackage",
                GwtPaths.MANAGED_UI.packageName(topLevelPackage));
        dataDictionary.setVariable("desktopUiPackage",
                GwtPaths.MANAGED_UI_DESKTOP.packageName(topLevelPackage));
        dataDictionary.setVariable("mobileUiPackage",
                GwtPaths.MANAGED_UI_MOBILE.packageName(topLevelPackage));
        dataDictionary.setVariable("uiEditorPackage",
                GwtPaths.MANAGED_UI_EDITOR.packageName(topLevelPackage));

        dataDictionary.setVariable("proxyRenderer", GwtProxyProperty
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
        if (type == GwtType.EDIT_ACTIVITY_WRAPPER
                || type == GwtType.MOBILE_EDIT_VIEW
                || type == GwtType.DESKTOP_EDIT_VIEW) {
            List<String> existingDesktopFields = new ArrayList<String>();
            List<String> existingMobileFields = new ArrayList<String>();

            try {
                String className = GwtPaths.MANAGED_UI_DESKTOP
                        .packageName(topLevelPackage)
                        + "."
                        + simpleTypeName
                        + GwtType.DESKTOP_EDIT_VIEW.getTemplate();

                ClassOrInterfaceTypeDetails details = typeLocationService
                        .getTypeDetails(new JavaType(className));

                if (details != null) {
                    for (FieldMetadata field : details.getDeclaredFields()) {
                        JavaSymbolName fieldName = field.getFieldName();
                        String name = fieldName.toString();
                        existingDesktopFields.add(name);
                    }
                }

                className = GwtPaths.MANAGED_UI_MOBILE
                        .packageName(topLevelPackage)
                        + "."
                        + simpleTypeName
                        + GwtType.MOBILE_EDIT_VIEW.getTemplate();

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
                if (type == GwtType.MOBILE_EDIT_VIEW)
                    existingEditViewFields = existingMobileFields;

                // Adds names of fields in DesktopEditView to existingFields
                // list
                if (type == GwtType.DESKTOP_EDIT_VIEW)
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

        if (type == GwtType.MOBILE_DETAILS_VIEW
                || type == GwtType.DESKTOP_DETAILS_VIEW) {
            List<String> existingDesktopFields = new ArrayList<String>();
            List<String> existingMobileFields = new ArrayList<String>();

            try {
                String className = GwtPaths.MANAGED_UI_DESKTOP
                        .packageName(topLevelPackage)
                        + "."
                        + simpleTypeName
                        + GwtType.DESKTOP_DETAILS_VIEW.getTemplate();

                ClassOrInterfaceTypeDetails details = typeLocationService
                        .getTypeDetails(new JavaType(className));

                if (details != null) {
                    for (FieldMetadata field : details.getDeclaredFields()) {
                        JavaSymbolName fieldName = field.getFieldName();
                        String name = fieldName.toString();
                        existingDesktopFields.add(name);
                    }
                }

                className = GwtPaths.MANAGED_UI_MOBILE
                        .packageName(topLevelPackage)
                        + "."
                        + simpleTypeName
                        + GwtType.MOBILE_DETAILS_VIEW.getTemplate();

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
                if (type == GwtType.MOBILE_DETAILS_VIEW)
                    existingDetailsViewFields = existingMobileFields;

                // Adds names of fields in DesktopDetailsView to existingFields
                // list
                if (type == GwtType.DESKTOP_DETAILS_VIEW)
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
                    && !templateService.isPrimaryProp(primaryProperty, mirroredType)) {
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
            if (templateService.isPrimaryProp(requestFactoryProxyProperty, mirroredType)) {
                primaryProperty = requestFactoryProxyProperty;
            }
            if (templateService.isSecondaryProp(requestFactoryProxyProperty, mirroredType)) {
                secondaryProperty = requestFactoryProxyProperty;
            }

            // Determine if this is the first date property.
            if (dateProperty == null && requestFactoryProxyProperty.isDate()) {
                dateProperty = requestFactoryProxyProperty;
            }

            final String helpText = templateService.getHelpText(requestFactoryProxyProperty
                    .getSymbolName(), mirroredType);
            final TextType textType = templateService.getTextType(requestFactoryProxyProperty
                    .getSymbolName(), mirroredType);
            final String units = templateService.getUnits(requestFactoryProxyProperty
                    .getSymbolName(), mirroredType);

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
            if (!existingDetailsViewFields.contains(requestFactoryProxyProperty.getName())
                    && !templateService.isInvisible(requestFactoryProxyProperty, mirroredType)) {
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

            if (!templateService.isInvisible(requestFactoryProxyProperty, mirroredType)) {
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
                propertiesSection.setVariable("helpText", helpText);
                propertiesSection.setVariable("units", units);
            }

            if (!templateService.isReadOnly(requestFactoryProxyProperty.getName(), mirroredType)
                    && !templateService.isUneditable(requestFactoryProxyProperty, mirroredType)) {
                // if the property is in the existingFields list, do not add it
                if (!existingEditViewFields
                        .contains(requestFactoryProxyProperty.getName())) {
                    dataDictionary.addSection("editViewProps").setVariable(
                            "prop", requestFactoryProxyProperty.forEditView(textType));
                    dataDictionary.addSection("mobileEditViewProps").setVariable(
                            "prop", requestFactoryProxyProperty.forMobileEditView(textType));
                }

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
                        requestFactoryProxyProperty.getBinder(textType));
                editableSection.setVariable("mobilePropBinder",
                        requestFactoryProxyProperty.getMobileBinder(textType));
                editableSection.setVariable("propReadable",
                        requestFactoryProxyProperty.getReadableName());
                editableSection.setVariable("helpText", helpText);
                editableSection.setVariable("units", units);
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
                    if (!templateService.isUneditable(requestFactoryProxyProperty, mirroredType)) {
                        final TemplateDataDictionary section = dataDictionary
                                .addSection(requestFactoryProxyProperty.isEnum() ? "setEnumValuePickers"
                                        : "setProxyProviders");
                        final boolean nullable = !templateService.isNotNull(
                                requestFactoryProxyProperty, mirroredType);
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
                                                .getSetValuePickerMethod(nullable));
                        section.setVariable("setValuePickerName",
                                requestFactoryProxyProperty.getSetValuePickerMethodName());
                        
                        section.setVariable(
                                "setProvider",
                                existingEditViewFields.contains(requestFactoryProxyProperty
                                        .getName()) ? requestFactoryProxyProperty
                                        .getSetEmptyProviderMethod()
                                        : requestFactoryProxyProperty
                                                .getSetProviderMethod(nullable));
                        section.setVariable("setProviderName",
                                requestFactoryProxyProperty.getSetProviderMethodName());
                        section.setVariable("setValuePickerName",
                                requestFactoryProxyProperty.getSetValuePickerMethodName());
                        section.setVariable("valueType", requestFactoryProxyProperty
                                .getValueType().getSimpleTypeName());
                        section.setVariable("proxyDataProviderName", requestFactoryProxyProperty
                                .getValueType().getSimpleTypeName() + "DataProvider");
                        if (nullable) {
                            section.showSection("nullable");
                        }
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
                    }
                    templateService.maybeAddImport(dataDictionary, importSet,
                            requestFactoryProxyProperty.getPropertyType());
                    templateService.maybeAddImport(dataDictionary, importSet,
                            requestFactoryProxyProperty.getValueType());
                    if (requestFactoryProxyProperty.isProxy()) {
                        templateService.maybeAddImport(dataDictionary, importSet,
                                requestFactoryProxyProperty.getInstanceEditorType());
                    }
                    if (requestFactoryProxyProperty.isCollection/*OfProxy*/()) {
                        templateService.maybeAddImport(dataDictionary, importSet,
                                requestFactoryProxyProperty.getPropertyType()
                                        .getParameters().get(0));
                        templateService.maybeAddImport(dataDictionary, importSet,
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
    public TemplateDataDictionary buildStandardDataDictionary(
            final RequestFactoryType type, final String moduleName,
            final String proxyModuleName) {
        final TemplateDataDictionary dataDictionary = templateService
                .buildStandardDataDictionary(type, moduleName, proxyModuleName);
        dataDictionary.setVariable("placePackage", GwtPaths.SCAFFOLD_PLACE
                .packageName(projectOperations.getTopLevelPackage(moduleName)));
        return dataDictionary;
    }

    @Override
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
                                GwtScaffoldMetadata.class,
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
        final JavaPackage proxyTopLevelPackage = projectOperations
                .getTopLevelPackage(PhysicalTypeIdentifier.getPath(
                proxy.getDeclaredByMetadataId()).getModule());
        final Map<RequestFactoryType, JavaType> mirrorTypeMap = RequestFactoryUtils
                .getMirrorTypeMap(mirroredType.getName(), proxyTopLevelPackage);
        mirrorTypeMap.putAll(GwtUtils.getMirrorTypeMap(
                mirroredType.getName(), topLevelPackage));
        mirrorTypeMap.put(RequestFactoryType.PROXY, proxy.getName());
        mirrorTypeMap.put(RequestFactoryType.REQUEST, request.getName());

        final Map<RequestFactoryType, ClassOrInterfaceTypeDetails> templateTypeDetailsMap = new LinkedHashMap<RequestFactoryType, ClassOrInterfaceTypeDetails>();
        final Map<RequestFactoryType, String[]> xmlTemplates = new LinkedHashMap<RequestFactoryType, String[]>();
        for (final GwtType gwtType : GwtType.getGwtMirrorTypes()) {
            if (gwtType.getTemplate() == null) {
                continue;
            }
            TemplateDataDictionary dataDictionary = buildMirrorDataDictionary(
                    gwtType, mirroredType, proxy, mirrorTypeMap,
                    clientSideTypeMap, moduleName);
            gwtType.dynamicallyResolveFieldsToWatch(clientSideTypeMap);
            gwtType.dynamicallyResolveMethodsToWatch(mirroredType.getName(),
                    clientSideTypeMap, topLevelPackage);
            templateTypeDetailsMap.put(
                    gwtType, templateService.
                    getTemplateDetails(dataDictionary, gwtType.getTemplate(),
                            mirrorTypeMap.get(gwtType), moduleName, TEMPLATE_DIR));

            if (gwtType.isCreateUiXml()) {
                dataDictionary = buildMirrorDataDictionary(gwtType,
                        mirroredType, proxy, mirrorTypeMap, clientSideTypeMap,
                        moduleName);
                final String[] contents = {templateService.getTemplateContents(
                        gwtType.getTemplate() + "UiXml", dataDictionary,
                        TEMPLATE_DIR)};
                xmlTemplates.put(gwtType, contents);
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
                    GwtPaths.MANAGED_UI_EDITOR.packageName(topLevelPackage));
            dataDictionary.setVariable("scaffoldUiPackage",
                    GwtPaths.SCAFFOLD_UI.packageName(topLevelPackage));
            final JavaType collectionTypeImpl = templateService
                    .getCollectionImplementation(proxyProperty
                    .getPropertyType());
            templateService.addImport(dataDictionary, collectionTypeImpl);
            templateService.addImport(dataDictionary, proxyProperty.getPropertyType());

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
                    GwtPaths.MANAGED_UI_EDITOR.packageName(topLevelPackage) + "."
                            + boundCollectionType + collectionType + "Editor");
            typeDetails.add(templateService.getTemplateDetails(dataDictionary,
                    "CollectionEditor", collectionEditorType, moduleName,
                    TEMPLATE_DIR));

            dataDictionary = TemplateDictionary.create();
            dataDictionary.setVariable("packageName",
                    GwtPaths.MANAGED_UI_EDITOR.packageName(topLevelPackage));
            dataDictionary.setVariable("scaffoldUiPackage",
                    GwtPaths.SCAFFOLD_UI.packageName(topLevelPackage));
            dataDictionary.setVariable("collectionType", collectionType);
            dataDictionary.setVariable("collectionTypeImpl",
                    collectionTypeImpl.getSimpleTypeName());
            dataDictionary.setVariable("boundCollectionType",
                    boundCollectionType);
            templateService.addImport(dataDictionary, proxyProperty.getPropertyType());

            final String contents = templateService.getTemplateContents("CollectionEditor"
                    + "UiXml", dataDictionary, TEMPLATE_DIR);
            final String packagePath = projectOperations.getPathResolver()
                    .getFocusedIdentifier(Path.SRC_MAIN_JAVA,
                            GwtPaths.MANAGED_UI_EDITOR.getPackagePath(topLevelPackage));
            xmlMap.put(packagePath + "/" + boundCollectionType + collectionType
                    + "Editor.ui.xml", contents);
        }

        return new RequestFactoryTemplateDataHolder(templateTypeDetailsMap, xmlTemplates,
                typeDetails, xmlMap);
    }

    @Override
    public String transformXml(final Document document)
            throws TransformerException {
        final Transformer transformer = XmlUtils.createIndentingTransformer();
        final DOMSource source = new DOMSource(document);
        final StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(source, result);
        return result.getWriter().toString();
    }

    @Override
    public List<ClassOrInterfaceTypeDetails> getStaticTemplateTypeDetails(
            final RequestFactoryType type, final String moduleName) {
        final List<ClassOrInterfaceTypeDetails> templateTypeDetails = new ArrayList<ClassOrInterfaceTypeDetails>();
        final TemplateDataDictionary dataDictionary = buildDictionary(type,
                moduleName);
        templateTypeDetails.add(templateService.getTemplateDetails(dataDictionary,
                type.getTemplate(), templateService.getDestinationJavaType(type, moduleName),
                moduleName, TEMPLATE_DIR));
        return templateTypeDetails;
    }
}
