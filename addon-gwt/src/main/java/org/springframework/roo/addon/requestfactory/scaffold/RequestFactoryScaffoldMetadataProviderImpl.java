package org.springframework.roo.addon.requestfactory.scaffold;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

@Component(immediate = true)
@Service
public class RequestFactoryScaffoldMetadataProviderImpl extends AbstractScaffoldMetadataProviderImpl implements RequestFactoryScaffoldMetadataProvider {

    protected void activate(final ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
    }

    protected void deactivate(final ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
    }

    @Override
    protected MetadataItem createMetadataItem(final String metadataIdentificationString) {
        return new RequestFactoryScaffoldMetadata(
                metadataIdentificationString);
    }

    @Override
    protected String createLocalIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return RequestFactoryScaffoldMetadata.createIdentifier(javaType, path);
    }

    @Override
    protected ClassOrInterfaceTypeDetails getGovernor(
            final String metadataIdentificationString) {
        final JavaType governorTypeName = RequestFactoryScaffoldMetadata
                .getJavaType(metadataIdentificationString);
        final LogicalPath governorTypePath = RequestFactoryScaffoldMetadata
                .getPath(metadataIdentificationString);

        final String physicalTypeId = PhysicalTypeIdentifier.createIdentifier(
                governorTypeName, governorTypePath);
        return typeLocationService.getTypeDetails(physicalTypeId);
    }

    @Override
    public String getProvidesType() {
        return RequestFactoryScaffoldMetadata.getMetadataIdentifierType();
    }
}
