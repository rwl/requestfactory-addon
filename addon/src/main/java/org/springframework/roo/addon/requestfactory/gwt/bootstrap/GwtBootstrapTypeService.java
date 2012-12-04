package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import java.util.Collection;
import java.util.List;

import org.springframework.roo.addon.requestfactory.RequestFactoryOperations;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeServiceImpl;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.model.JavaPackage;
import org.w3c.dom.Document;

/**
 * Interface for {@link RequestFactoryTypeServiceImpl}.
 *
 * @author James Tyrrell
 * @since 1.1.2
 */
public interface GwtBootstrapTypeService {

    void buildType(RequestFactoryType destType,
            List<ClassOrInterfaceTypeDetails> templateTypeDetails,
            String moduleName);
    
    
    
    /**
     * Adds the given GWT source path to the given module's gwt.xml file.
     *
     * @param sourcePath the path relative to the gwt.xml file, delimited by
     *            {@link RequestFactoryOperations#PATH_DELIMITER}
     * @param moduleName the project module whose gwt.xml file is to be updated
     */
    void addSourcePath(String sourcePath, String moduleName);

    void addInheritsModule(final String inherits, final String moduleName);

    String getGwtModuleXml(String moduleName);

    Document getGwtXmlDocument(final String gwtModuleCanonicalPath);

    /**
     * Returns the Java packages within the given module that contain GWT source
     *
     * @param moduleName the name of the module (empty for the root or only
     *            module)
     * @return a non-<code>null</code> collection
     */
    Collection<JavaPackage> getSourcePackages(String moduleName);

    /**
     * Indicates whether the given method's return type resides within any of
     * the given GWT source packages
     *
     * @param method
     * @param memberHoldingTypeDetail
     * @param sourcePackages
     * @return
     */
    boolean isMethodReturnTypeInSourcePath(MethodMetadata method,
            MemberHoldingTypeDetails memberHoldingTypeDetail,
            Iterable<JavaPackage> sourcePackages);

}
