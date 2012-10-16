package org.springframework.roo.addon.gwt.gwt.bootstrap;

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface of operations this add-on offers. Typically used by a
 * command type or an external add-on.
 *
 * @since 1.1
 */
public interface GwtBootstrapOperations extends Feature {

    /**
     * The delimiter for multi-level paths specified by a "<source path="..." />
     * element in a module's *.gwt.xml file.
     */
    String PATH_DELIMITER = "/";

    boolean isScaffoldAvailable();

    boolean isGwtInstallationPossible();

    void setupGwtBootstrap();

    void scaffoldAll(JavaPackage proxyPackage, JavaPackage requestPackage);

    void scaffoldType(JavaPackage proxyPackage, JavaPackage requestPackage,
            JavaType type);

    void updateGaeConfiguration();
}