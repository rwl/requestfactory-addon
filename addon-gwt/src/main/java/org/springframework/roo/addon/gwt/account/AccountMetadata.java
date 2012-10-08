package org.springframework.roo.addon.gwt.account;

import static org.springframework.roo.model.Jsr303JavaType.NOT_NULL;
import static org.springframework.roo.model.Jsr303JavaType.SIZE;
import static org.springframework.roo.model.JdkJavaType.ARRAY_LIST;
import static org.springframework.roo.model.JdkJavaType.COLLECTION;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.JdkJavaType.SET;
import static org.springframework.roo.model.JdkJavaType.HASH_SET;
import static org.springframework.roo.model.JpaJavaType.ENUMERATED;
import static org.springframework.roo.model.JpaJavaType.ENUM_TYPE;
import static org.springframework.roo.model.JpaJavaType.TYPED_QUERY;
import static org.springframework.roo.model.JavaType.BOOLEAN_PRIMITIVE;
import static org.springframework.roo.model.JavaType.INT_PRIMITIVE;
import static org.springframework.roo.model.JavaType.STRING;
import static org.springframework.roo.model.SpringJavaType.TRANSACTIONAL;

import static org.springframework.roo.addon.gwt.account.AccountJavaType.SIMPLE_GRANTED_AUTHORITY;
import static org.springframework.roo.addon.gwt.account.AccountJavaType.USER_DETAILS;
import static org.springframework.roo.addon.gwt.bootstrap.GwtBootstrapJavaType.KEY;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.addon.gwt.bootstrap.ListField;
import org.springframework.roo.addon.jpa.activerecord.JpaCrudAnnotationValues;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.operations.jsr303.SetField;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * This type produces metadata for a new ITD. It uses an {@link ItdTypeDetailsBuilder} provided by
 * {@link AbstractItdTypeDetailsProvidingMetadataItem} to register a field in the ITD and a new method.
 *
 * @since 1.1.0
 */
public class AccountMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    // Constants
    private static final String PROVIDES_TYPE_STRING = AccountMetadata.class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

    private static final String ENTITY_MANAGER_METHOD_NAME = "entityManager";

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType, LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    private final JpaCrudAnnotationValues crudAnnotationValues;
    private final TypeManagementService typeManagementService;
    private final TypeLocationService typeLocationService;
    private final String entityName;
    private final String sharedPackageName;
    private final boolean isGaeEnabled;

    private FieldMetadata usernameField, userRolesField, emailField, nameField, statusField;
    private JavaType roleType, statusType;

    public AccountMetadata(String identifier, JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata,
            JpaCrudAnnotationValues crudAnnotationValues,
            TypeManagementService typeManagementService, TypeLocationService typeLocationService, String entityName, String sharedPackageName, boolean isGaeEnabled) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '" + identifier + "' does not appear to be a valid");
        this.crudAnnotationValues = crudAnnotationValues;
        this.typeManagementService = typeManagementService;
        this.typeLocationService = typeLocationService;
        this.entityName = entityName;
        this.sharedPackageName = sharedPackageName;
        this.isGaeEnabled = isGaeEnabled;

        ensureGovernorImplements(USER_DETAILS);

        createRoleEnum();
        userRolesField = getUserRolesField();
        builder.addField(userRolesField);

        createStatusEnum();
        statusField = getStatusField();
        builder.addField(statusField);

        usernameField = getUsernameField();
        builder.addField(usernameField);

        emailField = getEmailField();
        builder.addField(emailField);

        nameField = getNameField();
        builder.addField(nameField);

        builder.addMethod(getAuthoritiesAccessor());
        builder.addMethod(getAccountExpiredMethod());
        builder.addMethod(getLockedMethod());
        builder.addMethod(getCredentialsExpiredMethod());
        builder.addMethod(getEnabledMethod());
        builder.addMethod(getFindByUsernameMethod());

        builder.addMethod(getDeclaredGetter(userRolesField));
        builder.addMethod(getDeclaredSetter(userRolesField));
        builder.addMethod(getDeclaredGetter(statusField));
        builder.addMethod(getDeclaredSetter(statusField));
        builder.addMethod(getDeclaredGetter(usernameField));
        builder.addMethod(getDeclaredSetter(usernameField));
        builder.addMethod(getDeclaredGetter(emailField));
        builder.addMethod(getDeclaredSetter(emailField));
        builder.addMethod(getDeclaredGetter(nameField));
        builder.addMethod(getDeclaredSetter(nameField));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    private MethodMetadata getFindByUsernameMethod() {

        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("find" + destination.getSimpleTypeName() + "ByUsername");

        // Check if a method with the same signature already exists in the target type
        final MethodMetadata method = methodExists(methodName, new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            // If it already exists, just return the method and omit its generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        if (isGaeEnabled) {
            final AnnotationMetadataBuilder transactionalBuilder = new AnnotationMetadataBuilder(
                    TRANSACTIONAL);
            if (StringUtils
                    .isNotBlank(crudAnnotationValues.getTransactionManager())) {
                transactionalBuilder.addStringAttribute("value",
                        crudAnnotationValues.getTransactionManager());
            }
            annotations.add(transactionalBuilder);
        }

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter types
        final JavaType[] parameterTypes = { STRING };

        // Define method parameter names
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName("username"));
        final JavaType returnType = destination;

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        final JavaType typedQueryType = new JavaType(
                TYPED_QUERY.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, Arrays.asList(destination));

        bodyBuilder.appendFormalLine(typedQueryType.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver())
                + " q = "
                + ENTITY_MANAGER_METHOD_NAME
                + "().createQuery(\"SELECT o FROM "
                + entityName
                + " AS o WHERE o.username = :username\", "
                + destination.getSimpleTypeName()
                + ".class);");
        bodyBuilder.appendFormalLine("q.setParameter(\"username\", username);");
        bodyBuilder.appendFormalLine("return q.getSingleResult();");

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(),
                Modifier.PUBLIC | Modifier.STATIC,
                methodName,
                returnType,
                AnnotatedJavaType.convertFromJavaTypes(parameterTypes),
                parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata instance
    }

    private void createRoleEnum() {
        JavaSymbolName roleAdmin = new JavaSymbolName("ROLE_ADMIN");
        JavaSymbolName roleUser = new JavaSymbolName("ROLE_USER");

        String packageName;
        if (sharedPackageName == null || sharedPackageName.isEmpty()) {
            JavaType entity = AccountMetadata.getJavaType(getId());
            packageName = entity.getPackage().getFullyQualifiedPackageName();
        } else {
            packageName = sharedPackageName;
        }
        roleType = new JavaType(packageName + ".Role");
        if (typeLocationService.getTypeDetails(roleType) != null) {
            return;
        }

        final String physicalTypeId = PhysicalTypeIdentifier.createIdentifier(
                roleType, AccountMetadata.getPath(getId()));
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                physicalTypeId, Modifier.PUBLIC, roleType,
                PhysicalTypeCategory.ENUMERATION);
        cidBuilder.addEnumConstant(roleUser);
        cidBuilder.addEnumConstant(roleAdmin);
        ClassOrInterfaceTypeDetails cid = cidBuilder.build();
        typeManagementService.createOrUpdateTypeOnDisk(cid);
    }

    private void createStatusEnum() {
        JavaSymbolName active = new JavaSymbolName("ACTIVE");
        JavaSymbolName expired = new JavaSymbolName("EXPIRED");
        JavaSymbolName locked = new JavaSymbolName("LOCKED");
        JavaSymbolName expiredCredentials = new JavaSymbolName("EXPIRED_CREDENTIALS");

        String packageName;
        if (sharedPackageName == null || sharedPackageName.isEmpty()) {
            JavaType entity = AccountMetadata.getJavaType(getId());
            packageName = entity.getPackage().getFullyQualifiedPackageName();
        } else {
            packageName = sharedPackageName;
        }
        statusType = new JavaType(packageName + ".Status");
        if (typeLocationService.getTypeDetails(statusType) != null) {
            return;
        }

        final String physicalTypeId = PhysicalTypeIdentifier.createIdentifier(
                statusType, AccountMetadata.getPath(getId()));
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                physicalTypeId, Modifier.PUBLIC, statusType,
                PhysicalTypeCategory.ENUMERATION);
        cidBuilder.addEnumConstant(active);
        cidBuilder.addEnumConstant(expired);
        cidBuilder.addEnumConstant(locked);
        cidBuilder.addEnumConstant(expiredCredentials);
        ClassOrInterfaceTypeDetails cid = cidBuilder.build();
        typeManagementService.createOrUpdateTypeOnDisk(cid);
    }

    private FieldMetadata getUserRolesField() {
        JavaSymbolName fieldName = new JavaSymbolName("userRoles");

        FieldMetadata existing = governorTypeDetails.getField(fieldName);
        if (existing != null) {
            return existing;
        }

        // Note private fields are private to the ITD, not the target type, this is undesirable
        // if a dependent method is pushed in to the target type
        int modifier = 0;

        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder enumeratedAnnotation = new AnnotationMetadataBuilder(ENUMERATED);
        enumeratedAnnotation.addEnumAttribute("value", ENUM_TYPE, "STRING");

        JavaType fieldType = new JavaType(SET.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, Arrays.asList(roleType));

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(getId(), // Metadata ID provided by supertype
            modifier, // Using package protection rather than private
            annotations,
            fieldName, // Field name
            fieldType); // Field type

        JavaType initializer = new JavaType(HASH_SET.getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null, Arrays.asList(roleType));
        fieldBuilder.setFieldInitializer("new " + initializer + "()");

        return fieldBuilder.build();
    }

    private FieldMetadata getStatusField() {
        JavaSymbolName fieldName = new JavaSymbolName("status");

        FieldMetadata existing = governorTypeDetails.getField(fieldName);
        if (existing != null) {
            return existing;
        }

        // Note private fields are private to the ITD, not the target type, this is undesirable
        // if a dependent method is pushed in to the target type
        int modifier = 0;

        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder enumeratedAnnotation = new AnnotationMetadataBuilder(ENUMERATED);
        enumeratedAnnotation.addEnumAttribute("value", ENUM_TYPE, "STRING");

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                getId(), modifier, annotations, fieldName, statusType);
        fieldBuilder.setFieldInitializer("Status.ACTIVE");

        return fieldBuilder.build();
    }

    private MethodMetadataBuilder getAuthoritiesAccessor() {

        // See if the user provided the field
        if (!getId().equals(userRolesField.getDeclaredByMetadataId())) {
            return null;
        }

        final ImportRegistrationResolver resolver = builder.getImportRegistrationResolver();

        JavaSymbolName requiredAccessorName = new JavaSymbolName("getAuthorities");

        JavaType collection = new JavaType(COLLECTION.getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null, Arrays.asList(SIMPLE_GRANTED_AUTHORITY));
        JavaType hashSet = new JavaType(HASH_SET.getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null, Arrays.asList(SIMPLE_GRANTED_AUTHORITY));

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine(collection.getNameIncludingTypeParameters(false, resolver)
                + " authorities = " + hashSet.getNameIncludingTypeParameters(false, resolver)
                + "();");
        bodyBuilder.appendFormalLine("for (" + roleType.getSimpleTypeName() + " role : "
                + userRolesField.getFieldName().getSymbolName() + ") {");
        bodyBuilder.appendFormalLine("authorities.add(new "
                + SIMPLE_GRANTED_AUTHORITY.getSimpleTypeName() + "(role.name()));");
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("return authorities;");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                requiredAccessorName, collection, bodyBuilder);
    }

    /**
     * Create metadata for a field definition.
     *
     * @return a FieldMetadata object
     */
    private FieldMetadata getUsernameField() {
        JavaSymbolName fieldName = new JavaSymbolName("username");

        FieldMetadata existing = governorTypeDetails.getField(fieldName);
        if (existing != null) {
            return existing;
        }

        // Note private fields are private to the ITD, not the target type, this is undesirable if a dependent method is pushed in to the target type
        int modifier = 0;

        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder notNullAnnotation = new AnnotationMetadataBuilder(NOT_NULL);
        AnnotationMetadataBuilder sizeAnnotation = new AnnotationMetadataBuilder(SIZE);
        sizeAnnotation.addIntegerAttribute("min", 3);
        sizeAnnotation.addIntegerAttribute("max", 64);
        annotations.add(notNullAnnotation);
        annotations.add(sizeAnnotation);

        // Using the FieldMetadataBuilder to create the field definition.
        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(getId(), // Metadata ID provided by supertype
            modifier, // Using package protection rather than private
            annotations,
            fieldName, // Field name
            JavaType.STRING); // Field type

        return fieldBuilder.build(); // Build and return a FieldMetadata instance
    }

    private MethodMetadataBuilder getAccountExpiredMethod() {

        JavaSymbolName methodName = new JavaSymbolName("isAccountNonExpired");

        if (governorHasMethod(methodName)) {
            return null;
        }

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("return this.status != Status.EXPIRED;");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                methodName, BOOLEAN_PRIMITIVE, bodyBuilder);
    }

    private MethodMetadataBuilder getLockedMethod() {

        JavaSymbolName methodName = new JavaSymbolName("isAccountNonLocked");

        if (governorHasMethod(methodName)) {
            return null;
        }

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("return this.status != Status.LOCKED;");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                methodName, BOOLEAN_PRIMITIVE, bodyBuilder);
    }

    private MethodMetadataBuilder getCredentialsExpiredMethod() {

        JavaSymbolName methodName = new JavaSymbolName("isCredentialsNonExpired");

        if (governorHasMethod(methodName)) {
            return null;
        }

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("return this.status != Status.EXPIRED_CREDENTIALS;");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                methodName, BOOLEAN_PRIMITIVE, bodyBuilder);
    }

    private MethodMetadataBuilder getEnabledMethod() {

        JavaSymbolName methodName = new JavaSymbolName("isEnabled");

        if (governorHasMethod(methodName)) {
            return null;
        }

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("return status == Status.ACTIVE;");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                methodName, BOOLEAN_PRIMITIVE, bodyBuilder);
    }

    private FieldMetadata getEmailField() {
        JavaSymbolName fieldName = new JavaSymbolName("email");

        FieldMetadata existing = governorTypeDetails.getField(fieldName);
        if (existing != null) {
            return existing;
        }

        // Using the FieldMetadataBuilder to create the field definition.
        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(getId(), // Metadata ID provided by supertype
            0, // Using package protection rather than private
            new ArrayList<AnnotationMetadataBuilder>(), // No annotations for this field
            fieldName, // Field name
            JavaType.STRING); // Field type

        return fieldBuilder.build(); // Build and return a FieldMetadata instance
    }

    private FieldMetadata getNameField() {
        JavaSymbolName fieldName = new JavaSymbolName("name");

        FieldMetadata existing = governorTypeDetails.getField(fieldName);
        if (existing != null) {
            return existing;
        }

        // Using the FieldMetadataBuilder to create the field definition.
        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(getId(), // Metadata ID provided by supertype
            0, // Using package protection rather than private
            new ArrayList<AnnotationMetadataBuilder>(), // No annotations for this field
            fieldName, // Field name
            JavaType.STRING); // Field type

        return fieldBuilder.build(); // Build and return a FieldMetadata instance
    }

    private MethodMetadataBuilder getDeclaredGetter(final FieldMetadata field) {
        Validate.notNull(field, "Field required");

        // Compute the mutator method name
        final JavaSymbolName methodName = BeanInfoUtils
                .getAccessorMethodName(field);

        // See if the type itself declared the accessor
        if (governorHasMethod(methodName)) {
            return null;
        }

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("return this."
                + field.getFieldName().getSymbolName() + ";");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                methodName, field.getFieldType(), bodyBuilder);
    }

    private MethodMetadataBuilder getDeclaredSetter(final FieldMetadata field) {
        Validate.notNull(field, "Field required");

        // Compute the mutator method name
        final JavaSymbolName methodName = BeanInfoUtils
                .getMutatorMethodName(field);

        // Compute the mutator method parameters
        final JavaType parameterType = field.getFieldType();

        // See if the type itself declared the mutator
        if (governorHasMethod(methodName, parameterType)) {
            return null;
        }

        // Compute the mutator method parameter names
        final List<JavaSymbolName> parameterNames = Arrays.asList(field
                .getFieldName());

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("this."
                + field.getFieldName().getSymbolName() + " = "
                + field.getFieldName().getSymbolName() + ";");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                methodName, JavaType.VOID_PRIMITIVE,
                AnnotatedJavaType.convertFromJavaTypes(parameterType),
                parameterNames, bodyBuilder);
    }

    /*private MethodMetadataBuilder getIdentityUrlMutator() {

        // Locate the identifier field, and compute the name of the accessor
        // that will be produced
        JavaSymbolName requiredMutatorName = BeanInfoUtils
                .getMutatorMethodName(identityUrlField);

        final List<JavaType> parameterTypes = Arrays.asList(identityUrlField
                .getFieldType());
        final List<JavaSymbolName> parameterNames = Arrays
                .asList(new JavaSymbolName("identityUrl"));

        // We declared the field in this ITD, so produce a public mutator for it
        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("this."
                + identityUrlField.getFieldName().getSymbolName() + " = identityUrl;");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                requiredMutatorName, JavaType.VOID_PRIMITIVE,
                AnnotatedJavaType.convertFromJavaTypes(parameterTypes),
                parameterNames, bodyBuilder);
    }*/

    private MethodMetadata methodExists(JavaSymbolName methodName, List<AnnotatedJavaType> paramTypes) {
        // We have no access to method parameter information, so we scan by name alone and treat any match as authoritative
        // We do not scan the superclass, as the caller is expected to know we'll only scan the current class
        for (MethodMetadata method : governorTypeDetails.getDeclaredMethods()) {
            if (method.getMethodName().equals(methodName) && method.getParameterTypes().equals(paramTypes)) {
                // Found a method of the expected name; we won't check method parameters though
                return method;
            }
        }
        return null;
    }

    // Typically, no changes are required beyond this point

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }
}
