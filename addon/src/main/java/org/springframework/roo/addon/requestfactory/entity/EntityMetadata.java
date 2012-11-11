package org.springframework.roo.addon.requestfactory.entity;

import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.KEY;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.KEY_FACTORY;
import static org.springframework.roo.model.JavaType.INT_PRIMITIVE;
import static org.springframework.roo.model.JavaType.LONG_PRIMITIVE;
import static org.springframework.roo.model.JavaType.STRING;
import static org.springframework.roo.model.JavaType.VOID_PRIMITIVE;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.JpaJavaType.TYPED_QUERY;
import static org.springframework.roo.model.SpringJavaType.TRANSACTIONAL;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.addon.jpa.activerecord.JpaCrudAnnotationValues;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * This type produces metadata for a new ITD. It uses an {@link ItdTypeDetailsBuilder} provided by
 * {@link AbstractItdTypeDetailsProvidingMetadataItem} to register a field in the ITD and a new method.
 *
 * @since 1.1.0
 */
public class EntityMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    // Constants
    private static final String PROVIDES_TYPE_STRING = EntityMetadata.class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

    private static final String ENTITY_MANAGER_METHOD_NAME = "entityManager";

    public static final JavaSymbolName STRING_ID_GETTER = new JavaSymbolName("getStringId");

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
    private final EntityAnnotationValues scaffoldAnnotationValues;
    private final boolean isGaeEnabled;
    private final String entityName;
    private final String plural;
    private final FieldMetadata identifierField;
    private final FieldMetadata parentProperty;

    public EntityMetadata(String identifier, JavaType aspectName, JpaCrudAnnotationValues crudAnnotationValues,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            EntityAnnotationValues scaffoldAnnotationValues, String plural, FieldMetadata idField, FieldMetadata parentProperty,
            final String entityName, final boolean isGaeEnabled) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '" + identifier + "' does not appear to be a valid");

        this.crudAnnotationValues = crudAnnotationValues;
        this.scaffoldAnnotationValues = scaffoldAnnotationValues;
        this.isGaeEnabled = isGaeEnabled;
        this.entityName = entityName;
        this.plural = plural;
        this.identifierField = idField;
        this.parentProperty = parentProperty;

        if (!isValid()) {
            return;
        }

        builder.addMethod(getStringIdGetter());
        builder.addMethod(getStringIdSetter());
        builder.addMethod(getFindEntriesByParentMethod());
        builder.addMethod(getCountByParentMethod());
        builder.addMethod(getFindByStringIdMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    private MethodMetadata getStringIdGetter() {
        if (!identifierField.getFieldType().equals(KEY)) {
            return null;
        }

        // Check if a method with the same signature already exists in the target type
        final MethodMetadata method = methodExists(STRING_ID_GETTER, new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            // If it already exists, just return the method and omit its generation via the ITD
            return method;
        }

        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("return "
                + KEY_FACTORY.getNameIncludingTypeParameters(true, builder.getImportRegistrationResolver())
                + ".keyToString(get"
                + identifierField.getFieldName().getSymbolNameCapitalisedFirstLetter()
                + "());");

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(),
                Modifier.PUBLIC, STRING_ID_GETTER, STRING, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata instance
    }

    private MethodMetadata getStringIdSetter() {
        if (!identifierField.getFieldType().equals(KEY)) {
            return null;
        }

        JavaSymbolName methodName = new JavaSymbolName("setStringId");

        // Check if a method with the same signature already exists in the target type
        final MethodMetadata method = methodExists(methodName, new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            // If it already exists, just return the method and omit its generation via the ITD
            return method;
        }

        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        final JavaType parameterType = STRING;
        final String idFieldName = identifierField.getFieldName().getSymbolName();
        final List<JavaSymbolName> parameterNames = Arrays
                .asList(new JavaSymbolName(idFieldName));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("set" + identifierField.getFieldName().getSymbolNameCapitalisedFirstLetter()
                + "("
                + KEY_FACTORY.getNameIncludingTypeParameters(true, builder.getImportRegistrationResolver())
                + ".stringToKey(" + idFieldName
                + "));");

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(),
                Modifier.PUBLIC, methodName, VOID_PRIMITIVE,
                AnnotatedJavaType.convertFromJavaTypes(parameterType),
                parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata instance
    }

    private MethodMetadata getFindByStringIdMethod() {
        if (!identifierField.getFieldType().equals(KEY)) {
            return null;
        }

        // Method definition to find or build
        final String idFieldName = identifierField.getFieldName().getSymbolName();
        final JavaSymbolName methodName = new JavaSymbolName("find"
                + destination.getSimpleTypeName() + "ByStringId");
        final JavaType parameterType = STRING;
        final List<JavaSymbolName> parameterNames = Arrays
                .asList(new JavaSymbolName(idFieldName));
        final JavaType returnType = destination;

        // Check if a method with the same signature already exists in the target type
        final MethodMetadata method = methodExists(methodName, new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            // If it already exists, just return the method and omit its generation via the ITD
            return method;
        }

        // Create method
        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        if (isGaeEnabled) {
            addTransactionalAnnotation(annotations);
        }

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder.appendFormalLine("if (" + idFieldName + " == null || "
                + idFieldName + ".length() == 0) return null;");

        bodyBuilder.appendFormalLine("return " + ENTITY_MANAGER_METHOD_NAME
                + "().find(" + returnType.getSimpleTypeName() + ".class, "
                + KEY_FACTORY.getNameIncludingTypeParameters(true, builder.getImportRegistrationResolver())
                + ".stringToKey(" + idFieldName + "));");

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC | Modifier.STATIC, methodName,
                returnType,
                AnnotatedJavaType.convertFromJavaTypes(parameterType),
                parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        return methodBuilder.build();
    }

    private MethodMetadata getFindEntriesByParentMethod() {
        if (parentProperty == null) {
            return null;
        }

        // Specify the desired method name
        /*JavaSymbolName methodName = new JavaSymbolName("find" + destination.getSimpleTypeName()
                + "EntriesBy" + parentField.getFieldName().getSymbolNameCapitalisedFirstLetter() + "Id");*/
        JavaSymbolName methodName = new JavaSymbolName("find" + destination.getSimpleTypeName() + "EntriesByParentId");

        // Check if a method with the same signature already exists in the target type
        final MethodMetadata method = methodExists(methodName, new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            // If it already exists, just return the method and omit its generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        if (isGaeEnabled) {
            addTransactionalAnnotation(annotations);
        }

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter types
        final JavaType idType = identifierField.getFieldType().equals(KEY) ? STRING : identifierField.getFieldType();
        final JavaType[] parameterTypes = { idType, INT_PRIMITIVE, INT_PRIMITIVE };

        // Define method parameter names
        final String idParamName = StringUtils.uncapitalize(parentProperty.getFieldType().getSimpleTypeName()) + "Id";
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName(idParamName),
                new JavaSymbolName("firstResult"),
                new JavaSymbolName("maxResults"));
        final JavaType returnType = new JavaType(
                LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(destination));

        // Create the method body
        String findMethodName = crudAnnotationValues.getFindMethod() + parentProperty.getFieldType().getSimpleTypeName();
        if (identifierField.getFieldType().equals(KEY)) {
            findMethodName = findMethodName + "ByStringId";
        }
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("final " + parentProperty.getFieldType().getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver())
                + " parent = "
                + parentProperty.getFieldType().getNameIncludingTypeParameters(true, builder.getImportRegistrationResolver())
                + "." + findMethodName + "(" + idParamName + ");");


        final List<JavaType> parameters = new ArrayList<JavaType>();
        parameters.add(destination);
        final JavaType typedQueryType = new JavaType(
                TYPED_QUERY.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, parameters);

        bodyBuilder.appendFormalLine(typedQueryType.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver())
                + " q = "
                + ENTITY_MANAGER_METHOD_NAME
                + "().createQuery(\"SELECT o FROM "
                + entityName
                + " AS o WHERE o."
                + parentProperty.getFieldName().getSymbolName()
                + " = :parent"
                + "\", "
                + destination.getSimpleTypeName()
                + ".class);");
        bodyBuilder.appendFormalLine("q.setParameter(\"parent\", parent);");
        bodyBuilder.appendFormalLine("q.setFirstResult(firstResult).setMaxResults(maxResults);");
        bodyBuilder.appendFormalLine("return q.getResultList();");

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

    private MethodMetadata getCountByParentMethod() {
        if (parentProperty == null) {
            return null;
        }

        // Specify the desired method name
        /*JavaSymbolName methodName = new JavaSymbolName("count" + plural
                + "By" + parentField.getFieldName().getSymbolNameCapitalisedFirstLetter() + "Id");*/
        JavaSymbolName methodName = new JavaSymbolName("count" + plural + "ByParentId");

        // Check if a method with the same signature already exists in the target type
        final MethodMetadata method = methodExists(methodName, new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            // If it already exists, just return the method and omit its generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        if (isGaeEnabled) {
            addTransactionalAnnotation(annotations);
        }

        // Define method throws types (none in this case)
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter types
        final JavaType idType = identifierField.getFieldType().equals(KEY) ? STRING : identifierField.getFieldType();
        final JavaType[] parameterTypes = { idType };

        // Define method parameter names
        final String idParamName = StringUtils.uncapitalize(parentProperty.getFieldType().getSimpleTypeName()) + "Id";
        final List<JavaSymbolName> parameterNames = Arrays.asList(new JavaSymbolName(idParamName));

        // Create the method body
        String findMethodName = crudAnnotationValues.getFindMethod() + parentProperty.getFieldType().getSimpleTypeName();
        if (identifierField.getFieldType().equals(KEY)) {
            findMethodName = findMethodName + "ByStringId";
        }
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("final " + parentProperty.getFieldType().getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver())
                + " "
                + parentProperty.getFieldName().getSymbolName() + " = "
                + parentProperty.getFieldType().getNameIncludingTypeParameters(true, builder.getImportRegistrationResolver())
                + "." + findMethodName + "(" + idParamName + ");");


        final List<JavaType> parameters = new ArrayList<JavaType>();
        parameters.add(destination);
        final JavaType typedQueryType = new JavaType(
                TYPED_QUERY.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, parameters);

        bodyBuilder.appendFormalLine(typedQueryType.getNameIncludingTypeParameters(false, builder.getImportRegistrationResolver())
                + " q = "
                + ENTITY_MANAGER_METHOD_NAME
                + "().createQuery(\"SELECT o FROM "
                + entityName
                + " AS o WHERE o."
                + parentProperty.getFieldName().getSymbolName()
                + " = :"
                + parentProperty.getFieldName().getSymbolName()
                + "\", "
                + destination.getSimpleTypeName()
                + ".class);");
        bodyBuilder.appendFormalLine("q.setParameter(\""
                + parentProperty.getFieldName().getSymbolName()
                + "\", "
                + parentProperty.getFieldName().getSymbolName()
                + ");");
        bodyBuilder.appendFormalLine("return q.getResultList().size();");

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(getId(),
                Modifier.PUBLIC | Modifier.STATIC,
                methodName,
                LONG_PRIMITIVE,
                AnnotatedJavaType.convertFromJavaTypes(parameterTypes),
                parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata instance
    }

    private void addTransactionalAnnotation(final List<AnnotationMetadataBuilder> annotations) {
        final AnnotationMetadataBuilder transactionalBuilder = new AnnotationMetadataBuilder(
                TRANSACTIONAL);
        if (StringUtils
                .isNotBlank(crudAnnotationValues.getTransactionManager())) {
            transactionalBuilder.addStringAttribute("value",
                    crudAnnotationValues.getTransactionManager());
        }
        annotations.add(transactionalBuilder);
    }

    private MethodMetadata methodExists(JavaSymbolName methodName, List<AnnotatedJavaType> paramTypes) {
        // We have no access to method parameter information, so we scan by name alone and treat any match as authoritative
        // We do not scan the superclass, as the caller is expected to know we'll only scan the current class
        for (MethodMetadata method : governorTypeDetails.getDeclaredMethods()) {
            if (method.getMethodName().equals(methodName)
//                    && method.getParameterTypes().equals(paramTypes)
                    ) {
                // Found a method of the expected name; we won't check method parameters though
                return method;
            }
        }
        /*for (MethodMetadata method : builder.build().getDeclaredMethods()) {
            if (method.getMethodName().equals(methodName)) {
                return method;
            }
        }*/
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
