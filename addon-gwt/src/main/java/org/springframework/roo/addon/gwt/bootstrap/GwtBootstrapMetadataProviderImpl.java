package org.springframework.roo.addon.gwt.bootstrap;


import static org.springframework.roo.addon.gwt.bootstrap.GwtBootstrapDataKeys.COUNT_BY_PARENT_METHOD;
import static org.springframework.roo.addon.gwt.bootstrap.GwtBootstrapDataKeys.FIND_ENTRIES_BY_PARENT_METHOD;
import static org.springframework.roo.addon.gwt.bootstrap.GwtBootstrapJavaType.ROO_GWT_BOOTSTRAP;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ENTITY;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaCrudAnnotationValues;
import org.springframework.roo.addon.jpa.entity.JpaEntityAnnotationValues;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.customdata.taggers.CustomDataKeyDecorator;
import org.springframework.roo.classpath.customdata.taggers.MethodMatcher;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;

/**
 * Provides {@link ExampleMetadata}. This type is called by Roo to retrieve the metadata for this add-on.
 * Use this type to reference external types and services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique add-on ITD identifier.
 *
 * @since 1.1
 */
@Component(immediate = true)
@Service
public final class GwtBootstrapMetadataProviderImpl extends AbstractItdMetadataProvider implements GwtBootstrapMetadataProvider {

    @Reference private CustomDataKeyDecorator customDataKeyDecorator;
    @Reference private ProjectOperations projectOperations;

    /**
     * The activate method for this OSGi component, this will be called by the OSGi container upon bundle activation
     * (result of the 'addon install' command)
     *
     * @param context the component context can be used to get access to the OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(ROO_GWT_BOOTSTRAP);
        registerMatchers();
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the OSGi container upon bundle deactivation
     * (result of the 'addon uninstall' command)
     *
     * @param context the component context can be used to get access to the OSGi container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(ROO_GWT_BOOTSTRAP);
        customDataKeyDecorator.unregisterMatchers(getClass());
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(String metadataIdentificationString, JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata, String itdFilename) {

        final JpaCrudAnnotationValues crudAnnotationValues = new JpaCrudAnnotationValues(governorPhysicalTypeMetadata);
        final JpaEntityAnnotationValues jpaEntityAnnotationValues = new JpaEntityAnnotationValues(governorPhysicalTypeMetadata, ROO_JPA_ENTITY);
        final GwtBootstrapAnnotationValues gwtBootstrapAnnotationValues = new GwtBootstrapAnnotationValues(governorPhysicalTypeMetadata);

        // we need the plural
        JavaType entity = GwtBootstrapMetadata.getJavaType(metadataIdentificationString);
        LogicalPath path = GwtBootstrapMetadata.getPath(metadataIdentificationString);
        String pluralId = PluralMetadata.createIdentifier(entity, path);
        PluralMetadata pluralMetadata = (PluralMetadata) metadataService.get(pluralId);
        if (pluralMetadata == null) {
            // Can't acquire the plural
            return null;
        }

        final List<FieldMetadata> idFields = persistenceMemberLocator.getIdentifierFields(entity);
        if (idFields.size() != 1) {
            // The ID field metadata is either unavailable or not stable yet
            return null;
        }
        final FieldMetadata idField = idFields.get(0);

        final MemberDetails memberDetails = getMemberDetails(governorPhysicalTypeMetadata);
        if (memberDetails == null) {
            return null;
        }
        FieldMetadata parentField = null;
        final String parentFieldName = gwtBootstrapAnnotationValues.getParentField();
        if (!parentFieldName.isEmpty()) {
            for (FieldMetadata field : memberDetails.getFields()) {
                if (field.getFieldName().getSymbolName().equals(parentFieldName)) {
                    parentField = field;
                    break;
                }
            }
            if (parentField == null) {
                return null;
            }
        }

        final String entityName = StringUtils.defaultIfEmpty(
                jpaEntityAnnotationValues.getEntityName(),
                entity.getSimpleTypeName());

        boolean isGaeEnabled = false;

        final String moduleName = path.getModule();
        if (projectOperations.isProjectAvailable(moduleName)) {
            // If the project itself changes, we want a chance to refresh this
            // item
            metadataDependencyRegistry.registerDependency(
                    ProjectMetadata.getProjectIdentifier(moduleName),
                    metadataIdentificationString);
            isGaeEnabled = projectOperations
                    .isFeatureInstalledInFocusedModule(FeatureNames.GAE);
        }

        // Pass dependencies required by the metadata in through its constructor
        return new GwtBootstrapMetadata(metadataIdentificationString, aspectName,
                crudAnnotationValues, governorPhysicalTypeMetadata, gwtBootstrapAnnotationValues,
                pluralMetadata.getPlural(), idField, parentField, entityName, isGaeEnabled);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name will be **_ROO_Gwt_Bootstrap.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "Gwt_Bootstrap";
    }

    protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
        JavaType javaType = GwtBootstrapMetadata.getJavaType(metadataIdentificationString);
        LogicalPath path = GwtBootstrapMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return GwtBootstrapMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return GwtBootstrapMetadata.getMetadataIdentiferType();
    }

    public GwtBootstrapAnnotationValues getAnnotationValues(final JavaType javaType) {
        Validate.notNull(javaType, "JavaType required");
        final String physicalTypeId = typeLocationService
                .getPhysicalTypeIdentifier(javaType);
        if (StringUtils.isBlank(physicalTypeId)) {
            return null;
        }
        final MemberHoldingTypeDetailsMetadataItem<?> governor = (MemberHoldingTypeDetailsMetadataItem<?>) metadataService
                .get(physicalTypeId);
        if (MemberFindingUtils.getAnnotationOfType(governor,
                ROO_GWT_BOOTSTRAP) == null) {
            // The type is not annotated with @RooGwtBootstrap
            return null;
        }
        return new GwtBootstrapAnnotationValues(governor);
    }

    @SuppressWarnings("unchecked")
    private void registerMatchers() {
        customDataKeyDecorator.registerMatchers(getClass(),
                new MethodMatcher(COUNT_BY_PARENT_METHOD, ROO_GWT_BOOTSTRAP, new JavaSymbolName("countByParentMethod"), "count", true, false, "ByParentId"),
                new MethodMatcher(FIND_ENTRIES_BY_PARENT_METHOD, ROO_GWT_BOOTSTRAP, new JavaSymbolName("findEntriesByParentMethod"), "find", false, true, "EntriesByParentId"));
    }
}