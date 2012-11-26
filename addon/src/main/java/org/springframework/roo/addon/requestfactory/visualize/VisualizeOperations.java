package org.springframework.roo.addon.requestfactory.visualize;

import org.springframework.roo.model.JavaType;


public interface VisualizeOperations {

    boolean isAnnotateCommandAvailable();

    boolean isSetupCommandAvailable();

    void annotateNodeType(final JavaType type, final String lat,
            final String lon);

    void setupMapsGwt();
}
