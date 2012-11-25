package org.springframework.roo.addon.requestfactory.graph;

import org.springframework.roo.addon.requestfactory.annotations.graph.RooGraphNode;
import org.springframework.roo.model.JavaType;


public final class GraphJavaType {

    public static final JavaType ROO_GRAPH_NODE = new JavaType(
            RooGraphNode.class);

    /**
     * Constructor is private to prevent instantiation
     */
    private GraphJavaType() {
    }
}
