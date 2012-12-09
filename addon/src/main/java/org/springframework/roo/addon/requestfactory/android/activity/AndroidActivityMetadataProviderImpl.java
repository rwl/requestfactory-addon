package org.springframework.roo.addon.requestfactory.android.activity;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ACTIVITY;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaCrudAnnotationValues;
import org.springframework.roo.addon.jpa.entity.JpaEntityAnnotationValues;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeService;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.customdata.taggers.CustomDataKeyDecorator;
import org.springframework.roo.classpath.customdata.taggers.MethodMatcher;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;

@Component(immediate = true)
@Service
public final class AndroidActivityMetadataProviderImpl extends AbstractItdMetadataProvider implements AndroidActivityMetadataProvider {

    @Reference private CustomDataKeyDecorator customDataKeyDecorator;
    @Reference private ProjectOperations projectOperations;
    @Reference private RequestFactoryTypeService requestFactoryTypeService;

    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(ROO_ACTIVITY);
    }

    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(ROO_ACTIVITY);
        customDataKeyDecorator.unregisterMatchers(getClass());
    }

    protected ItdTypeDetailsProvidingMetadataItem getMetadata(String metadataIdentificationString, JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata, String itdFilename) {

        final ActivityAnnotationValues activityAnnotationValues = new ActivityAnnotationValues(governorPhysicalTypeMetadata);

        final ClassOrInterfaceTypeDetails activity = getGovernor(metadataIdentificationString);

        final String moduleName = PhysicalTypeIdentifier.getPath(
                activity.getDeclaredByMetadataId()).getModule();
        final String topLevelPackage = projectOperations.getTopLevelPackage(
                moduleName).getFullyQualifiedPackageName();
        
        /*final JpaCrudAnnotationValues crudAnnotationValues = new JpaCrudAnnotationValues(governorPhysicalTypeMetadata);
        final JpaEntityAnnotationValues jpaEntityAnnotationValues = new JpaEntityAnnotationValues(governorPhysicalTypeMetadata, ROO_JPA_ENTITY);

        // we need the plural
        JavaType entity = AndroidActivityMetadata.getJavaType(metadataIdentificationString);
        LogicalPath path = AndroidActivityMetadata.getPath(metadataIdentificationString);
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

        final ClassOrInterfaceTypeDetails entityDetails = getGovernor(metadataIdentificationString);
        FieldMetadata parentProperty = requestFactoryTypeService.getParentField(entityDetails);
        final boolean activeRecord = entityDetails.getAnnotation(ROO_JPA_ACTIVE_RECORD) != null;

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
                    .isFeatureInstalledInModule(FeatureNames.GAE, moduleName);
        }*/

        // Pass dependencies required by the metadata in through its constructor
        return new AndroidActivityMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, 
                activityAnnotationValues, topLevelPackage);
    }

    private ClassOrInterfaceTypeDetails getGovernor(
            final String metadataIdentificationString) {
        final JavaType governorTypeName = AndroidActivityMetadata
                .getJavaType(metadataIdentificationString);
        final LogicalPath governorTypePath = AndroidActivityMetadata
                .getPath(metadataIdentificationString);

        final String physicalTypeId = PhysicalTypeIdentifier.createIdentifier(
                governorTypeName, governorTypePath);
        return typeLocationService.getTypeDetails(physicalTypeId);
    }

    /**
     * Define the unique ITD file name extension.
     */
    public String getItdUniquenessFilenameSuffix() {
        return "Activity";
    }

    protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
        JavaType javaType = AndroidActivityMetadata.getJavaType(metadataIdentificationString);
        LogicalPath path = AndroidActivityMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return AndroidActivityMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return AndroidActivityMetadata.getMetadataIdentiferType();
    }
}