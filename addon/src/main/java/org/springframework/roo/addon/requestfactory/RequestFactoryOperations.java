package org.springframework.roo.addon.requestfactory;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
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

    void setupRequestFactory(String section);

    boolean isRequestFactoryInstalled(String moduleName, boolean server);

    void createProxy(ClassOrInterfaceTypeDetails entity,
            JavaPackage destinationPackage, Pom serverModule);

    void createRequestInterface(ClassOrInterfaceTypeDetails entity,
            JavaPackage destinationPackage);

    void createUnmanagedRequestInterface(ClassOrInterfaceTypeDetails entity,
            JavaPackage destinationPackage);

    void createRequestInterfaceIfNecessary(ClassOrInterfaceTypeDetails entity,
            JavaPackage destinationPackage);

    AnnotationMetadata getRooGwtRequestAnnotation(
            ClassOrInterfaceTypeDetails entity);

    AnnotationMetadata getRooGwtUnmanagedRequestAnnotation(
            ClassOrInterfaceTypeDetails entity);

    void copyServerDirectoryContents(String module);

    void copySharedDirectoryContents(String module);

    void copyDirectoryContents(RequestFactoryPath requestFactoryPath, 
            String module, Class<?> loadingClass);

    void createScaffold(ClassOrInterfaceTypeDetails proxy);

    void updateFile(String sourceAntPath, String targetDirectory,
            String segmentPackage, boolean overwrite, Class<?> loadingClass);

    String processTemplate(String input, String segmentPackage);

    CharSequence getSharedTopLevelPackageName();

    CharSequence getImportAccountHookup();

    CharSequence getImportRoleHookup();

    CharSequence getAccountHookup();
}