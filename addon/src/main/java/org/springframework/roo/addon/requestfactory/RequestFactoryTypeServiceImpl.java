package org.springframework.roo.addon.requestfactory;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_LOCATOR;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_REQUEST;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_UNMANAGED_REQUEST;
import static org.springframework.roo.model.JavaType.LONG_OBJECT;
import static org.springframework.roo.model.JdkJavaType.BIG_DECIMAL;
import static org.springframework.roo.model.JdkJavaType.DATE;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.JdkJavaType.SET;
import static org.springframework.roo.model.JpaJavaType.EMBEDDABLE;
import static org.springframework.roo.model.JpaJavaType.MANY_TO_ONE;
import static org.springframework.roo.model.JpaJavaType.ONE_TO_MANY;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.customdata.CustomDataKeys;
import org.springframework.roo.classpath.details.AbstractIdentifiableAnnotatedJavaStructureBuilder;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.ConstructorMetadata;
import org.springframework.roo.classpath.details.ConstructorMetadataBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.IdentifiableAnnotatedJavaStructure;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.persistence.PersistenceMemberLocator;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;


/**
 * Provides a basic implementation of {@link RequestFactoryTypeService}.
 *
 * @author James Tyrrell
 * @since 1.1.2
 */
@Component
@Service
public class RequestFactoryTypeServiceImpl implements RequestFactoryTypeService {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(RequestFactoryTypeServiceImpl.class);

    @Reference RequestFactoryFileManager requestFactoryFileManager;
    @Reference MemberDetailsScanner memberDetailsScanner;
    @Reference MetadataService metadataService;
    @Reference PersistenceMemberLocator persistenceMemberLocator;
    @Reference ProjectOperations projectOperations;
    @Reference TypeLocationService typeLocationService;

    private final Set<String> warnings = new LinkedHashSet<String>();
    private final Timer warningTimer = new Timer();

    @Override
    public FieldMetadata getParentField(ClassOrInterfaceTypeDetails childType) {
        for (FieldMetadata field : childType.getFieldsWithAnnotation(MANY_TO_ONE)) {
            
            final String fieldTypeId = typeLocationService
                    .getPhysicalTypeIdentifier(field.getFieldType());
            if (fieldTypeId == null
                    || metadataService.get(fieldTypeId) == null) {
                continue;
            }
            final MemberHoldingTypeDetails fieldType = ((PhysicalTypeMetadata) metadataService
                    .get(fieldTypeId)).getMemberHoldingTypeDetails();
            
            for (FieldMetadata opposite : fieldType.getFieldsWithAnnotation(ONE_TO_MANY)) {
                AnnotationMetadata oneToMany = opposite.getAnnotation(ONE_TO_MANY);
                AnnotationAttributeValue<String> mappedBy = oneToMany.getAttribute("mappedBy");
                if (mappedBy != null
                        && mappedBy.getValue().equals(field.getFieldName().getSymbolName())) {
                    return field;
                }
            }
        }
        return null;
    }

    @Override
    public List<ClassOrInterfaceTypeDetails> buildType(final RequestFactoryType destType,
            final ClassOrInterfaceTypeDetails templateClass,
            final List<MemberHoldingTypeDetails> extendsTypes,
            final String moduleName) {
        try {
            // A type may consist of a concrete type which depend on
            final List<ClassOrInterfaceTypeDetails> types = new ArrayList<ClassOrInterfaceTypeDetails>();
            final ClassOrInterfaceTypeDetailsBuilder templateClassBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    templateClass);

            if (destType.isCreateAbstract()) {
                final ClassOrInterfaceTypeDetailsBuilder abstractClassBuilder = createAbstractBuilder(
                        templateClassBuilder, extendsTypes, moduleName);

                final ArrayList<FieldMetadataBuilder> fieldsToRemove = new ArrayList<FieldMetadataBuilder>();
                for (final JavaSymbolName fieldName : destType
                        .getWatchedFieldNames()) {
                    for (final FieldMetadataBuilder fieldBuilder : templateClassBuilder
                            .getDeclaredFields()) {
                        if (fieldBuilder.getFieldName().equals(fieldName)) {
                            final FieldMetadataBuilder abstractFieldBuilder = new FieldMetadataBuilder(
                                    abstractClassBuilder
                                            .getDeclaredByMetadataId(),
                                    fieldBuilder.build());
                            abstractClassBuilder
                                    .addField(convertModifier(abstractFieldBuilder));
                            fieldsToRemove.add(fieldBuilder);
                            break;
                        }
                    }
                }

                templateClassBuilder.getDeclaredFields().removeAll(
                        fieldsToRemove);

                final List<MethodMetadataBuilder> methodsToRemove = new ArrayList<MethodMetadataBuilder>();
                for (final JavaSymbolName methodName : destType
                        .getWatchedMethods().keySet()) {
                    for (final MethodMetadataBuilder methodBuilder : templateClassBuilder
                            .getDeclaredMethods()) {
                        final List<JavaType> params = new ArrayList<JavaType>();
                        for (final AnnotatedJavaType param : methodBuilder
                                .getParameterTypes()) {
                            params.add(new JavaType(param.getJavaType()
                                    .getFullyQualifiedTypeName()));
                        }
                        if (methodBuilder.getMethodName().equals(methodName)) {
                            if (destType.getWatchedMethods().get(methodName)
                                    .containsAll(params)) {
                                final MethodMetadataBuilder abstractMethodBuilder = new MethodMetadataBuilder(
                                        abstractClassBuilder
                                                .getDeclaredByMetadataId(),
                                        methodBuilder.build());
                                abstractClassBuilder
                                        .addMethod(convertModifier(abstractMethodBuilder));
                                methodsToRemove.add(methodBuilder);
                                break;
                            }
                        }
                    }
                }

                templateClassBuilder.removeAll(methodsToRemove);

                for (final JavaType innerTypeName : destType
                        .getWatchedInnerTypes()) {
                    for (final ClassOrInterfaceTypeDetailsBuilder innerTypeBuilder : templateClassBuilder
                            .getDeclaredInnerTypes()) {
                        if (innerTypeBuilder.getName().getSimpleTypeName()
                                .equals(innerTypeName.getSimpleTypeName())) {
                            final ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(
                                    abstractClassBuilder
                                            .getDeclaredByMetadataId(),
                                    innerTypeBuilder.build());
                            builder.setName(new JavaType(
                                    innerTypeBuilder.getName()
                                            .getSimpleTypeName() + "_Roo",
                                    0, DataType.TYPE, null, innerTypeBuilder
                                            .getName().getParameters()));

                            templateClassBuilder.getDeclaredInnerTypes()
                                    .remove(innerTypeBuilder);
                            if (innerTypeBuilder.getPhysicalTypeCategory()
                                    .equals(PhysicalTypeCategory.INTERFACE)) {
                                final ClassOrInterfaceTypeDetailsBuilder interfaceInnerTypeBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                                        innerTypeBuilder.build());
                                abstractClassBuilder.addInnerType(builder);
                                templateClassBuilder.getDeclaredInnerTypes()
                                        .remove(innerTypeBuilder);
                                interfaceInnerTypeBuilder
                                        .clearDeclaredMethods();
                                interfaceInnerTypeBuilder
                                        .getDeclaredInnerTypes().clear();
                                interfaceInnerTypeBuilder.getExtendsTypes()
                                        .clear();
                                interfaceInnerTypeBuilder
                                        .getExtendsTypes()
                                        .add(new JavaType(
                                                builder.getName()
                                                        .getSimpleTypeName(),
                                                0,
                                                DataType.TYPE,
                                                null,
                                                Collections
                                                        .singletonList(new JavaType(
                                                                "V",
                                                                0,
                                                                DataType.VARIABLE,
                                                                null,
                                                                new ArrayList<JavaType>()))));
                                templateClassBuilder.getDeclaredInnerTypes()
                                        .add(interfaceInnerTypeBuilder);
                            }
                            break;
                        }
                    }
                }

                abstractClassBuilder.setImplementsTypes(templateClass
                        .getImplementsTypes());
                templateClassBuilder.getImplementsTypes().clear();
                templateClassBuilder.getExtendsTypes().clear();
                templateClassBuilder.getExtendsTypes().add(
                        abstractClassBuilder.getName());
                types.add(abstractClassBuilder.build());
            }

            types.add(templateClassBuilder.build());

            return types;
        }
        catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void buildType(final RequestFactoryType type,
            final List<ClassOrInterfaceTypeDetails> templateTypeDetails,
            final String moduleName) {
        type.resolveMethodsToWatch(type);
        type.resolveWatchedFieldNames(type);
        final List<ClassOrInterfaceTypeDetails> typesToBeWritten = new ArrayList<ClassOrInterfaceTypeDetails>();
        for (final ClassOrInterfaceTypeDetails templateTypeDetail : templateTypeDetails) {
            typesToBeWritten.addAll(buildType(type, templateTypeDetail,
                    getExtendsTypes(templateTypeDetail), moduleName));
        }
        requestFactoryFileManager.write(typesToBeWritten, type.isOverwriteConcrete());
    }

    @Override
    public void checkPrimitive(final JavaType type) {
        if (type.isPrimitive() && !JavaType.VOID_PRIMITIVE.equals(type)) {
            final String to = type.getSimpleTypeName();
            final String from = to.toLowerCase();
            throw new IllegalStateException(
                    "GWT does not currently support primitive types in an entity. Please change any '"
                            + from
                            + "' entity property types to 'java.lang."
                            + to + "'.");
        }
    }

    private <T extends AbstractIdentifiableAnnotatedJavaStructureBuilder<? extends IdentifiableAnnotatedJavaStructure>> T convertModifier(
            final T builder) {
        if (Modifier.isPrivate(builder.getModifier())) {
            builder.setModifier(Modifier.PROTECTED);
        }
        return builder;
    }

    @Override
    public ClassOrInterfaceTypeDetailsBuilder createAbstractBuilder(
            final ClassOrInterfaceTypeDetailsBuilder concreteClass,
            final List<MemberHoldingTypeDetails> extendsTypesDetails,
            final String moduleName) {
        final JavaType concreteType = concreteClass.getName();
        String abstractName = concreteType.getSimpleTypeName() + "_Roo";
        abstractName = concreteType.getPackage().getFullyQualifiedPackageName()
                + '.' + abstractName;
        final JavaType abstractType = new JavaType(abstractName);
        final String abstractId = PhysicalTypeIdentifier.createIdentifier(
                abstractType,
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, moduleName));
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                abstractId);
        cidBuilder.setPhysicalTypeCategory(PhysicalTypeCategory.CLASS);
        cidBuilder.setName(abstractType);
        cidBuilder.setModifier(Modifier.ABSTRACT | Modifier.PUBLIC);
        cidBuilder.getExtendsTypes().addAll(concreteClass.getExtendsTypes());
        cidBuilder.add(concreteClass.getRegisteredImports());

        for (final MemberHoldingTypeDetails extendsTypeDetails : extendsTypesDetails) {
            for (final ConstructorMetadata constructor : extendsTypeDetails
                    .getDeclaredConstructors()) {
                final ConstructorMetadataBuilder abstractConstructor = new ConstructorMetadataBuilder(
                        abstractId);
                abstractConstructor.setModifier(constructor.getModifier());

                final Map<JavaSymbolName, JavaType> typeMap = resolveTypes(
                        extendsTypeDetails.getName(), concreteClass
                                .getExtendsTypes().get(0));
                for (final AnnotatedJavaType type : constructor
                        .getParameterTypes()) {
                    JavaType newType = type.getJavaType();
                    if (type.getJavaType().getParameters().size() > 0) {
                        final ArrayList<JavaType> parameterTypes = new ArrayList<JavaType>();
                        for (final JavaType typeType : type.getJavaType()
                                .getParameters()) {
                            final JavaType typeParam = typeMap
                                    .get(new JavaSymbolName(typeType.toString()));
                            if (typeParam != null) {
                                parameterTypes.add(typeParam);
                            }
                        }
                        newType = new JavaType(type.getJavaType()
                                .getFullyQualifiedTypeName(), type
                                .getJavaType().getArray(), type.getJavaType()
                                .getDataType(),
                                type.getJavaType().getArgName(), parameterTypes);
                    }
                    abstractConstructor.getParameterTypes().add(
                            new AnnotatedJavaType(newType));
                }
                abstractConstructor.setParameterNames(constructor
                        .getParameterNames());

                final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
                bodyBuilder.newLine().indent().append("super(");

                int i = 0;
                for (final JavaSymbolName paramName : abstractConstructor
                        .getParameterNames()) {
                    bodyBuilder.append(" ").append(paramName.getSymbolName());
                    if (abstractConstructor.getParameterTypes().size() > i + 1) {
                        bodyBuilder.append(", ");
                    }
                    i++;
                }

                bodyBuilder.append(");");

                bodyBuilder.newLine().indentRemove();
                abstractConstructor.setBodyBuilder(bodyBuilder);
                cidBuilder.getDeclaredConstructors().add(abstractConstructor);
            }
        }
        return cidBuilder;
    }

    @Override
    public void displayWarning(final String warning) {
        if (!warnings.contains(warning)) {
            warnings.add(warning);
            LOGGER.warning(warning);
            warningTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    warnings.clear();
                }
            }, 15000);
        }
    }

    @Override
    public List<MemberHoldingTypeDetails> getExtendsTypes(
            final ClassOrInterfaceTypeDetails childType) {
        final List<MemberHoldingTypeDetails> extendsTypes = new ArrayList<MemberHoldingTypeDetails>();
        if (childType != null) {
            for (final JavaType javaType : childType.getExtendsTypes()) {
                final String superTypeId = typeLocationService
                        .getPhysicalTypeIdentifier(javaType);
                if (superTypeId == null
                        || metadataService.get(superTypeId) == null) {
                    continue;
                }
                final MemberHoldingTypeDetails superType = ((PhysicalTypeMetadata) metadataService
                        .get(superTypeId)).getMemberHoldingTypeDetails();
                extendsTypes.add(superType);
            }
        }
        return extendsTypes;
    }

    /**
     * Return the type arg for the client side method, given the domain method
     * return type. If domain method return type is List<Integer> or
     * Set<Integer>, returns the same. If domain method return type is
     * List<Employee>, return List<EmployeeProxy>
     *
     * @param returnType
     * @param projectMetadata
     * @param governorType
     * @return the client-side leaf type as a JavaType
     */
    @Override
    public JavaType getClientSideLeafType(final JavaType returnType,
            final JavaType governorType, final boolean requestType,
            final boolean convertPrimitive) {
        if (returnType.isPrimitive() && convertPrimitive) {
            if (!requestType) {
                checkPrimitive(returnType);
            }
            return RequestFactoryUtils.convertPrimitiveType(returnType, requestType);
        }

        if (isTypeCommon(returnType)) {
            return returnType;
        }

        if (isCollectionType(returnType)) {
            final List<JavaType> args = returnType.getParameters();
            if (args != null && args.size() == 1) {
                final JavaType elementType = args.get(0);
                final JavaType convertedJavaType = getClientSideLeafType(
                        elementType, governorType, requestType,
                        convertPrimitive);
                if (convertedJavaType == null) {
                    return null;
                }
                return new JavaType(returnType.getFullyQualifiedTypeName(), 0,
                        DataType.TYPE, null, Arrays.asList(convertedJavaType));
            }
            return returnType;
        }

        final ClassOrInterfaceTypeDetails ptmd = typeLocationService
                .getTypeDetails(returnType);
        if (isDomainObject(returnType, ptmd)) {
            if (isEmbeddable(ptmd)) {
                throw new IllegalStateException(
                        "GWT does not currently support embedding objects in entities, such as '"
                                + returnType.getSimpleTypeName() + "' in '"
                                + governorType.getSimpleTypeName() + "'.");
            }
            final ClassOrInterfaceTypeDetails typeDetails = typeLocationService
                    .getTypeDetails(returnType);
            if (typeDetails == null) {
                return null;
            }
            final ClassOrInterfaceTypeDetails proxy = lookupProxyFromEntity(typeDetails);
            if (proxy == null) {
                return null;
            }
            return proxy.getName();
        }
        return returnType;
    }

    @Override
    public List<MethodMetadata> getProxyMethods(
            final ClassOrInterfaceTypeDetails governorTypeDetails) {
        final List<MethodMetadata> proxyMethods = new ArrayList<MethodMetadata>();
        final MemberDetails memberDetails = memberDetailsScanner
                .getMemberDetails(RequestFactoryTypeServiceImpl.class.getName(),
                        governorTypeDetails);
        for (final MemberHoldingTypeDetails memberHoldingTypeDetails : memberDetails
                .getDetails()) {
            for (final MethodMetadata method : memberDetails.getMethods()) {
                if (!proxyMethods.contains(method)
                        && isPublicAccessor(method)
                        && isValidMethodReturnType(method,
                                memberHoldingTypeDetails)) {
                    if (method
                            .getCustomData()
                            .keySet()
                            .contains(CustomDataKeys.IDENTIFIER_ACCESSOR_METHOD)) {
                        proxyMethods.add(0, method);
                    }
                    else {
                        proxyMethods.add(method);
                    }
                }
            }
        }
        return proxyMethods;
    }

    @Override
    public JavaType getServiceLocator(final String moduleName) {
        return new JavaType(projectOperations.getTopLevelPackage(moduleName)
                + ".locator.RequestFactoryServiceLocator");
    }

    @Override
    public boolean isAllowableReturnType(final JavaType type) {
        return isCommonType(type) || isEntity(type) || isEnum(type);
    }

    @Override
    public boolean isAllowableReturnType(final MethodMetadata method) {
        return isAllowableReturnType(method.getReturnType());
    }

    @Override
    public boolean isCollectionType(final JavaType returnType) {
        return returnType.getFullyQualifiedTypeName().equals(
                LIST.getFullyQualifiedTypeName())
                || returnType.getFullyQualifiedTypeName().equals(
                        SET.getFullyQualifiedTypeName());
    }

    @Override
    public boolean isCommonType(final JavaType type) {
        return isTypeCommon(type) || isCollectionType(type)
                && type.getParameters().size() == 1
                && isAllowableReturnType(type.getParameters().get(0));
    }

    @Override
    public boolean isDomainObject(final JavaType type) {
        final ClassOrInterfaceTypeDetails ptmd = typeLocationService
                .getTypeDetails(type);
        return isDomainObject(type, ptmd);
    }

    @Override
    public boolean isDomainObject(final JavaType returnType,
            final ClassOrInterfaceTypeDetails ptmd) {
        return !isEnum(ptmd) && isEntity(returnType)
                && !isRequestFactoryCompatible(returnType)
                && !isEmbeddable(ptmd);
    }

    @Override
    public boolean isEmbeddable(final ClassOrInterfaceTypeDetails ptmd) {
        if (ptmd == null) {
            return false;
        }
        final AnnotationMetadata annotationMetadata = ptmd
                .getAnnotation(EMBEDDABLE);
        return annotationMetadata != null;
    }

    @Override
    public boolean isEntity(final JavaType type) {
        return persistenceMemberLocator.getIdentifierFields(type).size() == 1;
    }

    @Override
    public boolean isEnum(final ClassOrInterfaceTypeDetails ptmd) {
        return ptmd != null
                && ptmd.getPhysicalTypeCategory() == PhysicalTypeCategory.ENUMERATION;
    }

    @Override
    public boolean isEnum(final JavaType type) {
        return isEnum(typeLocationService.getTypeDetails(type));
    }

    @Override
    public boolean isPrimitive(final JavaType type) {
        return type.isPrimitive() || isCollectionType(type)
                && type.getParameters().size() == 1
                && isPrimitive(type.getParameters().get(0));
    }

    @Override
    public boolean isPublicAccessor(final MethodMetadata method) {
        return Modifier.isPublic(method.getModifier())
                && !method.getReturnType().equals(JavaType.VOID_PRIMITIVE)
                && method.getParameterTypes().isEmpty()
                && method.getMethodName().getSymbolName().startsWith("get");
    }

    @Override
    public boolean isRequestFactoryCompatible(final JavaType type) {
        return isCommonType(type) || isCollectionType(type);
    }

    @Override
    public boolean isTypeCommon(final JavaType type) {
        return JavaType.BOOLEAN_OBJECT.equals(type)
                || JavaType.CHAR_OBJECT.equals(type)
                || JavaType.BYTE_OBJECT.equals(type)
                || JavaType.SHORT_OBJECT.equals(type)
                || JavaType.INT_OBJECT.equals(type)
                || LONG_OBJECT.equals(type)
                || JavaType.FLOAT_OBJECT.equals(type)
                || JavaType.DOUBLE_OBJECT.equals(type)
                || JavaType.STRING.equals(type)
                || DATE.equals(type)
                || BIG_DECIMAL.equals(type)
                || type.isPrimitive()
                && !JavaType.VOID_PRIMITIVE.getFullyQualifiedTypeName().equals(
                        type.getFullyQualifiedTypeName());
    }

    @Override
    public boolean isValidMethodReturnType(final MethodMetadata method,
            final MemberHoldingTypeDetails memberHoldingTypeDetail) {
        final JavaType returnType = method.getReturnType();
        if (isPrimitive(returnType)) {
            displayWarning("The primitive field type, "
                    + method.getReturnType().getSimpleTypeName().toLowerCase()
                    + " of '"
                    + method.getMethodName().getSymbolName()
                    + "' in type "
                    + memberHoldingTypeDetail.getName().getSimpleTypeName()
                    + " is not currently support by GWT and will not be added to the scaffolded application.");
            return false;
        }

        final JavaSymbolName propertyName = new JavaSymbolName(
                StringUtils.uncapitalize(BeanInfoUtils
                        .getPropertyNameForJavaBeanMethod(method)
                        .getSymbolName()));
        if (!isAllowableReturnType(method)) {
            displayWarning("The field type "
                    + method.getReturnType().getFullyQualifiedTypeName()
                    + " of '"
                    + method.getMethodName().getSymbolName()
                    + "' in type "
                    + memberHoldingTypeDetail.getName().getSimpleTypeName()
                    + " is not currently support by GWT and will not be added to the scaffolded application.");
            return false;
        }
        if (propertyName.getSymbolName().equals("owner")) {
            displayWarning("'owner' is not allowed to be used as field name as it is currently reserved by GWT. Please rename the field 'owner' in type "
                    + memberHoldingTypeDetail.getName().getSimpleTypeName()
                    + ".");
            return false;
        }

        return true;
    }

    @Override
    public ClassOrInterfaceTypeDetails lookupEntityFromLocator(
            final ClassOrInterfaceTypeDetails locator) {
        Validate.notNull(locator, "Locator is required");
        return lookupTargetFromX(locator, ROO_REQUEST_FACTORY_LOCATOR);
    }

    @Override
    public ClassOrInterfaceTypeDetails lookupEntityFromProxy(
            final ClassOrInterfaceTypeDetails proxy) {
        Validate.notNull(proxy, "Proxy is required");
        return lookupTargetFromX(proxy, ROO_REQUEST_FACTORY_PROXY);
    }

    @Override
    public ClassOrInterfaceTypeDetails lookupEntityFromRequest(
            final ClassOrInterfaceTypeDetails request) {
        Validate.notNull(request, "Request is required");
        return lookupTargetFromX(request, ROO_REQUEST_FACTORY_REQUEST);
    }

    @Override
    public ClassOrInterfaceTypeDetails lookupProxyFromEntity(
            final ClassOrInterfaceTypeDetails entity) {
        return lookupXFromEntity(entity, ROO_REQUEST_FACTORY_PROXY);
    }

    @Override
    public ClassOrInterfaceTypeDetails lookupProxyFromRequest(
            final ClassOrInterfaceTypeDetails request) {
        final AnnotationMetadata annotation = RequestFactoryUtils.getFirstAnnotation(
                request, ROO_REQUEST_FACTORY_REQUEST);
        Validate.notNull(annotation, "Request '" + request.getName()
                + "' isn't annotated with '" + ROO_REQUEST_FACTORY_REQUEST
                + "'");
        final AnnotationAttributeValue<?> attributeValue = annotation
                .getAttribute("value");
        final JavaType proxyType = new JavaType(
                RequestFactoryUtils.getStringValue(attributeValue));
        return lookupProxyFromEntity(typeLocationService
                .getTypeDetails(proxyType));
    }

    @Override
    public ClassOrInterfaceTypeDetails lookupRequestFromEntity(
            final ClassOrInterfaceTypeDetails entity) {
        return lookupXFromEntity(entity, ROO_REQUEST_FACTORY_REQUEST);
    }

    @Override
    public ClassOrInterfaceTypeDetails lookupRequestFromProxy(
            final ClassOrInterfaceTypeDetails proxy) {
        final AnnotationMetadata annotation = RequestFactoryUtils.getFirstAnnotation(
                proxy, ROO_REQUEST_FACTORY_PROXY);
        Validate.notNull(annotation, "Proxy '" + proxy.getName()
                + "' isn't annotated with '" + ROO_REQUEST_FACTORY_PROXY + "'");
        final AnnotationAttributeValue<?> attributeValue = annotation
                .getAttribute("value");
        final JavaType serviceNameType = new JavaType(
                RequestFactoryUtils.getStringValue(attributeValue));
        return lookupRequestFromEntity(typeLocationService
                .getTypeDetails(serviceNameType));
    }

    @Override
    public ClassOrInterfaceTypeDetails lookupUnmanagedRequestFromProxy(
            final ClassOrInterfaceTypeDetails proxy) {
        final AnnotationMetadata annotation = RequestFactoryUtils.getFirstAnnotation(
                proxy, ROO_REQUEST_FACTORY_PROXY);
        Validate.notNull(annotation, "Proxy '" + proxy.getName()
                + "' isn't annotated with '" + ROO_REQUEST_FACTORY_PROXY + "'");
        final AnnotationAttributeValue<?> attributeValue = annotation
                .getAttribute("value");
        final JavaType serviceNameType = new JavaType(
                RequestFactoryUtils.getStringValue(attributeValue));
        return lookupUnmanagedRequestFromEntity(typeLocationService
                .getTypeDetails(serviceNameType));
    }

    @Override
    public ClassOrInterfaceTypeDetails lookupUnmanagedRequestFromEntity(
            final ClassOrInterfaceTypeDetails entity) {
        return lookupXFromEntity(entity, ROO_REQUEST_FACTORY_UNMANAGED_REQUEST);
    }

    @Override
    public ClassOrInterfaceTypeDetails lookupTargetFromX(
            final ClassOrInterfaceTypeDetails annotatedType,
            final JavaType... annotations) {
        final AnnotationMetadata annotation = RequestFactoryUtils.getFirstAnnotation(
                annotatedType, annotations);
        Validate.notNull(annotation,
                "Type '" + annotatedType.getName() + "' isn't annotated with '"
                        + StringUtils.join(Arrays.asList(annotations), ",")
                        + "'");
        final AnnotationAttributeValue<?> attributeValue = annotation
                .getAttribute("value");
        final JavaType targetType = new JavaType(
                RequestFactoryUtils.getStringValue(attributeValue));
        return typeLocationService.getTypeDetails(targetType);
    }

    @Override
    public ClassOrInterfaceTypeDetails lookupTargetServiceFromRequest(
            final ClassOrInterfaceTypeDetails request) {
        Validate.notNull(request, "Request is required");
        return lookupTargetFromX(request, RequestFactoryUtils.REQUEST_ANNOTATIONS);
    }

    @Override
    public ClassOrInterfaceTypeDetails lookupXFromEntity(
            final ClassOrInterfaceTypeDetails entity,
            final JavaType... annotations) {
        Validate.notNull(entity, "Entity not found");
        for (final ClassOrInterfaceTypeDetails cid : typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(annotations)) {
            final AnnotationMetadata annotationMetadata = RequestFactoryUtils
                    .getFirstAnnotation(cid, annotations);
            if (annotationMetadata != null) {
                final AnnotationAttributeValue<?> attributeValue = annotationMetadata
                        .getAttribute("value");
                final String value = RequestFactoryUtils.getStringValue(attributeValue);
                if (entity.getName().getFullyQualifiedTypeName().equals(value)) {
                    return cid;
                }
            }
        }
        return null;
    }

    private Map<JavaSymbolName, JavaType> resolveTypes(final JavaType generic,
            final JavaType typed) {
        final Map<JavaSymbolName, JavaType> typeMap = new LinkedHashMap<JavaSymbolName, JavaType>();
        final boolean typeCountMatch = generic.getParameters().size() == typed
                .getParameters().size();
        Validate.isTrue(typeCountMatch, "Type count must match.");

        int i = 0;
        for (final JavaType genericParamType : generic.getParameters()) {
            typeMap.put(genericParamType.getArgName(), typed.getParameters()
                    .get(i));
            i++;
        }
        return typeMap;
    }
}
