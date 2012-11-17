package org.springframework.roo.addon.requestfactory;

import java.util.List;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.model.JavaType;

/**
 * Interface for {@link RequestFactoryTypeServiceImpl}.
 *
 * @author James Tyrrell
 * @since 1.1.2
 */
public interface RequestFactoryTypeService {

    FieldMetadata getParentField(
            ClassOrInterfaceTypeDetails childType);
    

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
}
