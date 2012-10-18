package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import java.util.Collection;

import org.springframework.roo.addon.requestfactory.RequestFactoryOperations;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeService;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeServiceImpl;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.model.JavaPackage;

/**
 * Interface for {@link RequestFactoryTypeServiceImpl}.
 *
 * @author James Tyrrell
 * @since 1.1.2
 */
public interface GwtBootstrapTypeService extends RequestFactoryTypeService {

    /**
     * Adds the given GWT source path to the given module's gwt.xml file.
     *
     * @param sourcePath the path relative to the gwt.xml file, delimited by
     *            {@link RequestFactoryOperations#PATH_DELIMITER}
     * @param moduleName the project module whose gwt.xml file is to be updated
     */
    void addSourcePath(String sourcePath, String moduleName);

    String getGwtModuleXml(String moduleName);

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
