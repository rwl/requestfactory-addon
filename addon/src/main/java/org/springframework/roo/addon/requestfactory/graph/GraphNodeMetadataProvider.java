package org.springframework.roo.addon.requestfactory.graph;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.requestfactory.annotations.graph.RooGraphNode;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Provides {@link GraphNodeMetadata}.
 */
@Component
@Service
public final class GraphNodeMetadataProvider extends
        AbstractItdMetadataProvider {

    protected void activate(final ComponentContext context) {
        metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier
                .getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(new JavaType(RooGraphNode.class.getName()));
    }

    protected void deactivate(final ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(PhysicalTypeIdentifier
                .getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(new JavaType(RooGraphNode.class.getName()));
    }

    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            final String metadataIdentificationString,
            final JavaType aspectName,
            final PhysicalTypeMetadata governorPhysicalTypeMetadata,
            final String itdFilename) {
        final GraphNodeAnnotationValues graphNodeAnnotationValues =
                new GraphNodeAnnotationValues(governorPhysicalTypeMetadata);
        return new GraphNodeMetadata(metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata, graphNodeAnnotationValues);
    }

    public String getItdUniquenessFilenameSuffix() {
        return "Graph_Node";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            final String metadataIdentificationString) {
        final JavaType javaType = GraphNodeMetadata.getJavaType(
                metadataIdentificationString);
        final LogicalPath path = GraphNodeMetadata.getPath(
                metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return GraphNodeMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return GraphNodeMetadata.getMetadataIdentiferType();
    }
}
