package org.springframework.roo.addon.requestfactory.graph;

import org.springframework.roo.model.JavaType;


public interface GraphOperations {

    boolean isAnnotateCommandAvailable();

    boolean isSetupCommandAvailable();

    void annotateNodeType(final JavaType type, final String x,
            final String y);

    void setupGraph();
}
