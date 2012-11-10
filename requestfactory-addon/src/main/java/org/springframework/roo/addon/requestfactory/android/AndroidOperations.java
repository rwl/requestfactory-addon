package org.springframework.roo.addon.requestfactory.android;

import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.maven.Pom;

/**
 * Interface of operations this add-on offers.
 */
public interface AndroidOperations {

    boolean isScaffoldAvailable();

    void scaffoldAll(final Pom module);

    void scaffoldType(JavaType type, final Pom module);
}