package org.springframework.roo.addon.requestfactory.visualize;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.requestfactory.annotations.visualize.RooMapMarker;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Provides {@link MapMarkerMetadata}.
 */
@Component
@Service
public final class MapMarkerMetadataProvider extends
        AbstractItdMetadataProvider {

    protected void activate(final ComponentContext context) {
        metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier
                .getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(new JavaType(RooMapMarker.class.getName()));
    }

    protected void deactivate(final ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(PhysicalTypeIdentifier
                .getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(new JavaType(RooMapMarker.class.getName()));
    }

    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            final String metadataIdentificationString,
            final JavaType aspectName,
            final PhysicalTypeMetadata governorPhysicalTypeMetadata,
            final String itdFilename) {
        final MapMarkerAnnotationValues mapMarkerAnnotationValues =
                new MapMarkerAnnotationValues(governorPhysicalTypeMetadata);
        return new MapMarkerMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, mapMarkerAnnotationValues);
    }

    public String getItdUniquenessFilenameSuffix() {
        return "Map_Marker";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            final String metadataIdentificationString) {
        final JavaType javaType = MapMarkerMetadata.getJavaType(
                metadataIdentificationString);
        final LogicalPath path = MapMarkerMetadata.getPath(
                metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return MapMarkerMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return MapMarkerMetadata.getMetadataIdentiferType();
    }
}
