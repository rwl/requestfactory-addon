package org.springframework.roo.addon.requestfactory;

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

    boolean isRequestFactoryAddonInstallationPossible();

    boolean isRequestFactoryServerInstallationPossible();

    boolean isRequestFactoryClientInstallationPossible();

    boolean isRequestFactoryCommandAvailable();

    boolean isScaffoldAvailable();

    void proxyAll(JavaPackage proxyPackage, Pom serverModule);

    void proxyType(JavaPackage proxyPackage, JavaType type, Pom serverModule);

    void requestAll(JavaPackage requestPackage);

    void requestType(JavaPackage requestPackage, JavaType type);

    void setupRequestFactoryAddon();
    
    void setupRequestFactoryServer();

    void setupRequestFactoryClient();

    void scaffoldAll();

    void scaffoldType(JavaType type);
}