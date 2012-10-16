package org.springframework.roo.addon.gwt;

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;
import org.springframework.roo.project.maven.Pom;

/**
 * Provides GWT operations.
 *
 * @author Ben Alex
 * @author James Tyrrell
 * @since 1.1
 */
public interface RequestFactoryOperations extends Feature {

    /**
     * The delimiter for multi-level paths specified by a "<source path="..." />
     * element in a module's *.gwt.xml file.
     */
    String PATH_DELIMITER = "/";

    boolean isRequestFactoryServerInstallationPossible();
    boolean isRequestFactoryClientInstallationPossible();

    boolean isRequestFactoryCommandAvailable();

    void proxyAll(JavaPackage proxyPackage, Pom locatorModule);

    void proxyType(JavaPackage proxyPackage, JavaType type, Pom locatorModule);

    void requestAll(JavaPackage requestPackage);

    void requestType(JavaPackage requestPackage, JavaType type);

    void setupRequestFactoryServer();
    void setupRequestFactoryClient();
}