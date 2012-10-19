package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import java.util.Collection;
import java.util.List;

import org.springframework.roo.addon.requestfactory.RequestFactoryOperations;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeService;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeServiceImpl;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;

/**
 * Interface for {@link RequestFactoryTypeServiceImpl}.
 *
 * @author James Tyrrell
 * @since 1.1.2
 */
public interface GwtBootstrapTypeService {

    List<ClassOrInterfaceTypeDetails> buildType(RequestFactoryType destType,
            ClassOrInterfaceTypeDetails templateClass,
            List<MemberHoldingTypeDetails> extendsTypes, String moduleName);

    void buildType(RequestFactoryType destType,
            List<ClassOrInterfaceTypeDetails> templateTypeDetails,
            String moduleName);

    List<MemberHoldingTypeDetails> getExtendsTypes(
            ClassOrInterfaceTypeDetails childType);

    JavaType getClientSideLeafType(JavaType returnType, JavaType governorType,
            boolean requestType, boolean convertPrimitive);

    List<MethodMetadata> getProxyMethods(
            ClassOrInterfaceTypeDetails governorTypeDetails);

    /**
     * Returns the project type in the given module that implements
     * {@link com.google.web.bindery.requestfactory.shared.ServiceLocator}.
     * There is no guarantee that this type actually exists.
     *
     * @param moduleName the module in which to find the locator (can't be
     *            <code>null</code>)
     * @return a non-<code>null</code> type
     * @since 1.2.0
     */
    JavaType getServiceLocator(String moduleName);

    boolean isDomainObject(JavaType type);

    ClassOrInterfaceTypeDetails lookupEntityFromLocator(
            ClassOrInterfaceTypeDetails request);

    ClassOrInterfaceTypeDetails lookupEntityFromProxy(
            ClassOrInterfaceTypeDetails proxy);

    ClassOrInterfaceTypeDetails lookupEntityFromRequest(
            ClassOrInterfaceTypeDetails request);

    ClassOrInterfaceTypeDetails lookupProxyFromEntity(
            ClassOrInterfaceTypeDetails entity);

    ClassOrInterfaceTypeDetails lookupProxyFromRequest(
            ClassOrInterfaceTypeDetails request);

    ClassOrInterfaceTypeDetails lookupRequestFromEntity(
            ClassOrInterfaceTypeDetails entity);

    ClassOrInterfaceTypeDetails lookupRequestFromProxy(
            ClassOrInterfaceTypeDetails proxy);

    ClassOrInterfaceTypeDetails lookupTargetServiceFromRequest(
            ClassOrInterfaceTypeDetails request);

    ClassOrInterfaceTypeDetails lookupUnmanagedRequestFromProxy(
            ClassOrInterfaceTypeDetails proxy);

    ClassOrInterfaceTypeDetails lookupUnmanagedRequestFromEntity(
            ClassOrInterfaceTypeDetails entity);



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
