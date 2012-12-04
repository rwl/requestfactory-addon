package org.springframework.roo.addon.requestfactory;

import java.util.List;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
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

    void checkPrimitive(JavaType type);

    ClassOrInterfaceTypeDetailsBuilder createAbstractBuilder(
            ClassOrInterfaceTypeDetailsBuilder concreteClass,
            List<MemberHoldingTypeDetails> extendsTypesDetails,
            String moduleName);

    void displayWarning(String warning);

    boolean isAllowableReturnType(JavaType type);

    boolean isAllowableReturnType(MethodMetadata method);

    boolean isCollectionType(JavaType returnType);

    boolean isCommonType(JavaType type);

    boolean isDomainObject(JavaType returnType,
            ClassOrInterfaceTypeDetails ptmd);

    boolean isEmbeddable(ClassOrInterfaceTypeDetails ptmd);

    boolean isEntity(JavaType type);

    boolean isEnum(ClassOrInterfaceTypeDetails ptmd);

    boolean isEnum(JavaType type);

    boolean isPrimitive(JavaType type);

    boolean isPublicAccessor(MethodMetadata method);

    boolean isRequestFactoryCompatible(JavaType type);

    boolean isTypeCommon(JavaType type);

    boolean isValidMethodReturnType(MethodMetadata method,
            MemberHoldingTypeDetails memberHoldingTypeDetail);

    ClassOrInterfaceTypeDetails lookupTargetFromX(
            ClassOrInterfaceTypeDetails annotatedType,
            JavaType... annotations);

    ClassOrInterfaceTypeDetails lookupXFromEntity(
            ClassOrInterfaceTypeDetails entity,
            JavaType... annotations);
}
