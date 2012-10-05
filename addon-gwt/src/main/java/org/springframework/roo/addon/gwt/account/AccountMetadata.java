package org.springframework.roo.addon.gwt.account;

import static org.springframework.roo.model.Jsr303JavaType.NOT_NULL;
import static org.springframework.roo.model.Jsr303JavaType.SIZE;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
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
import org.springframework.roo.metadata.MetadataIdentificationUtils;
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

    private final TypeManagementService typeManagementService;
    private final TypeLocationService typeLocationService;
    private final String sharedPackageName;

    private FieldMetadata identityUrlField;

    public AccountMetadata(String identifier, JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata,
            TypeManagementService typeManagementService, TypeLocationService typeLocationService, String sharedPackageName) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '" + identifier + "' does not appear to be a valid");
        this.typeManagementService = typeManagementService;
        this.typeLocationService = typeLocationService;
        this.sharedPackageName = sharedPackageName;

        createRoleEnum();

        identityUrlField = getIdentityUrlField();
        builder.addField(identityUrlField);
        builder.addMethod(getUsernameAccessor());

        builder.addField(getEmailField());

        // Adding a new sample method definition
//        builder.addMethod(getSampleMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
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
        JavaType name = new JavaType(packageName + ".Role");
        if (typeLocationService.getTypeDetails(name) != null) {
            return;
        }

        final String physicalTypeId = PhysicalTypeIdentifier.createIdentifier(
                name, AccountMetadata.getPath(getId()));
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                physicalTypeId, Modifier.PUBLIC, name,
                PhysicalTypeCategory.ENUMERATION);
        cidBuilder.addEnumConstant(roleUser);
        cidBuilder.addEnumConstant(roleAdmin);
        ClassOrInterfaceTypeDetails cid = cidBuilder.build();
        typeManagementService.createOrUpdateTypeOnDisk(cid);
    }

    /**
     * Create metadata for a field definition.
     *
     * @return a FieldMetadata object
     */
    private FieldMetadata getIdentityUrlField() {
        JavaSymbolName fieldName = new JavaSymbolName("identityUrl");

        FieldMetadata existing = governorTypeDetails.getField(fieldName);
        if (existing != null) {
            return existing;
        }

        // Note private fields are private to the ITD, not the target type, this is undesirable if a dependent method is pushed in to the target type
        int modifier = 0;

        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder notNullAnnotation = new AnnotationMetadataBuilder(NOT_NULL);
        AnnotationMetadataBuilder sizeAnnotation = new AnnotationMetadataBuilder(SIZE);
        sizeAnnotation.addIntegerAttribute("min", 8);
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

    private MethodMetadataBuilder getUsernameAccessor() {

        // See if the user provided the field
        if (!getId().equals(identityUrlField.getDeclaredByMetadataId())) {
            return null;
        }

        // Locate the identifier field, and compute the name of the accessor
        // that will be produced
        JavaSymbolName requiredAccessorName = new JavaSymbolName("getUsername");

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("return this."
                + identityUrlField.getFieldName().getSymbolName() + ";");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                requiredAccessorName, identityUrlField.getFieldType(),
                bodyBuilder);
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

    private MethodMetadata getSampleMethod() {
        // Specify the desired method name
        JavaSymbolName methodName = new JavaSymbolName("sampleMethod");

        // Check if a method with the same signature already exists in the target type
        final MethodMetadata method = methodExists(methodName, new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            // If it already exists, just return the method and omit its generation via the ITD
            return method;
        }

        // Define method annotations (none in this case)
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter types (none in this case)
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("System.out.println(\"Hello World\");");

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(), Modifier.PUBLIC, methodName, JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata instance
    }

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
