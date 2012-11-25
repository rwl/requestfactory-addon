package org.springframework.roo.addon.requestfactory.graph;

import static org.springframework.roo.addon.requestfactory.annotations.graph.RooGraphNode.X_FIELD_DEFAULT;
import static org.springframework.roo.addon.requestfactory.annotations.graph.RooGraphNode.Y_FIELD_DEFAULT;
import static org.springframework.roo.addon.requestfactory.graph.GraphJavaType.ROO_GRAPH_NODE;

import org.springframework.roo.addon.requestfactory.annotations.graph.RooGraphNode;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;

/**
 * The values of a parsed {@link RooGraphNode} annotation.
 */
public class GraphNodeAnnotationValues extends AbstractAnnotationValues {

    @AutoPopulate private String x = X_FIELD_DEFAULT;
    @AutoPopulate private String y = Y_FIELD_DEFAULT;

    public GraphNodeAnnotationValues(
            final MemberHoldingTypeDetailsMetadataItem<?> annotatedType) {
        super(annotatedType, ROO_GRAPH_NODE);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }
}
