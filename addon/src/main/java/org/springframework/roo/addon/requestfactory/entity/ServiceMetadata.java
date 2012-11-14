package org.springframework.roo.addon.requestfactory.entity;

import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.KEY;
import static org.springframework.roo.model.JavaType.INT_PRIMITIVE;
import static org.springframework.roo.model.JavaType.LONG_PRIMITIVE;
import static org.springframework.roo.model.JavaType.STRING;
import static org.springframework.roo.model.JdkJavaType.LIST;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.addon.layers.service.ServiceAnnotationValues;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * This type produces metadata for a new service ITD.
 */
public class ServiceMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    private static final InvocableMemberBodyBuilder BODY = new InvocableMemberBodyBuilder();

    private static final int PUBLIC_ABSTRACT = Modifier.PUBLIC
            | Modifier.ABSTRACT;

    private static final String PROVIDES_TYPE_STRING = ServiceMetadata.class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdString);
    }

    public static final LogicalPath getPath(String metadataIdString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(
                PROVIDES_TYPE_STRING, metadataIdString);
    }

    public static boolean isValid(String metadataIdString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(
                PROVIDES_TYPE_STRING, metadataIdString);
    }

    private final ServiceAnnotationValues annotationValues;

    private final JavaType domainType;
    private final String plural;
    private final FieldMetadata identifierField;
    private final FieldMetadata parentProperty;

    public ServiceMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            ServiceAnnotationValues annotationValues, JavaType domainType,
            String plural, FieldMetadata idField, FieldMetadata parentProperty) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        this.annotationValues = annotationValues;
        this.domainType = domainType;
        this.plural = plural;
        this.identifierField = idField;
        this.parentProperty = parentProperty;

        if (!isValid()) {
            return;
        }

        builder.addMethod(getFindEntriesByParentMethod());
        builder.addMethod(getCountByParentMethod());
        builder.addMethod(getFindByStringIdMethod());

        itdTypeDetails = builder.build();
    }

    private MethodMetadata getFindByStringIdMethod() {
        if (!KEY.equals(identifierField.getFieldType())) {
            return null;
        }

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

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), PUBLIC_ABSTRACT, methodName, returnType,
                AnnotatedJavaType.convertFromJavaTypes(parameterType),
                parameterNames, BODY);

        return methodBuilder.build();
    }

    private MethodMetadata getFindEntriesByParentMethod() {
        if (parentProperty == null) {
            return null;
        }

        JavaSymbolName methodName = new JavaSymbolName("find" + domainType
                .getSimpleTypeName() + "EntriesByParentId");

        final MethodMetadata method = methodExists(methodName,
                new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            return method;
        }

        final JavaType idType = KEY.equals(identifierField.getFieldType())
                ? STRING : identifierField.getFieldType();
        final JavaType[] parameterTypes = { idType, INT_PRIMITIVE, INT_PRIMITIVE };

        final String idParamName = StringUtils.uncapitalize(parentProperty
                .getFieldType().getSimpleTypeName()) + "Id";
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName(idParamName),
                new JavaSymbolName("firstResult"),
                new JavaSymbolName("maxResults"));
        final JavaType returnType = new JavaType(
                LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(domainType));

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), PUBLIC_ABSTRACT, methodName, returnType,
                AnnotatedJavaType.convertFromJavaTypes(parameterTypes),
                parameterNames, BODY);

        return methodBuilder.build();
    }

    private MethodMetadata getCountByParentMethod() {
        if (parentProperty == null) {
            return null;
        }

        JavaSymbolName methodName = new JavaSymbolName("count" + plural + "ByParentId");

        final MethodMetadata method = methodExists(methodName,
                new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            return method;
        }

        final JavaType idType = KEY.equals(identifierField.getFieldType())
                ? STRING : identifierField.getFieldType();
        final JavaType[] parameterTypes = { idType };

        final String idParamName = StringUtils.uncapitalize(parentProperty
                .getFieldType().getSimpleTypeName()) + "Id";
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName(idParamName));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), PUBLIC_ABSTRACT, methodName, LONG_PRIMITIVE,
                AnnotatedJavaType.convertFromJavaTypes(parameterTypes),
                parameterNames, BODY);

        return methodBuilder.build();
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
        return null;
    }

    public ServiceAnnotationValues getServiceAnnotationValues() {
        return annotationValues;
    }

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
