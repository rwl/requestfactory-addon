package org.springframework.roo.addon.requestfactory.android.project.metadata;

import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ACTIVITY;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.customdata.taggers.CustomDataKeyDecorator;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;

@Component(immediate = true)
@Service
public final class AndroidActivityMetadataProviderImpl
        extends AbstractItdMetadataProvider
        implements AndroidActivityMetadataProvider {

    @Reference private CustomDataKeyDecorator customDataKeyDecorator;
    @Reference private ProjectOperations projectOperations;

    protected void activate(final ComponentContext context) {
        metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier
                .getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(ROO_ACTIVITY);
    }

    protected void deactivate(final ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(PhysicalTypeIdentifier
                .getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(ROO_ACTIVITY);
        customDataKeyDecorator.unregisterMatchers(getClass());
    }

    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            final String metadataIdentificationString,
            final JavaType aspectName,
            final PhysicalTypeMetadata governorPhysicalTypeMetadata,
            final String itdFilename) {

        final ActivityAnnotationValues activityAnnotationValues =
                new ActivityAnnotationValues(governorPhysicalTypeMetadata);

        final ClassOrInterfaceTypeDetails typeDetails = getGovernor(
                metadataIdentificationString);

        final String moduleName = PhysicalTypeIdentifier.getPath(
                typeDetails.getDeclaredByMetadataId()).getModule();
        final String topLevelPackage = projectOperations.getTopLevelPackage(
                moduleName).getFullyQualifiedPackageName();
        
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

    public String getItdUniquenessFilenameSuffix() {
        return "Activity";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            final String metadataIdentificationString) {
        final JavaType javaType = AndroidActivityMetadata.getJavaType(
                metadataIdentificationString);
        final LogicalPath path = AndroidActivityMetadata.getPath(
                metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return AndroidActivityMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return AndroidActivityMetadata.getMetadataIdentiferType();
    }
}