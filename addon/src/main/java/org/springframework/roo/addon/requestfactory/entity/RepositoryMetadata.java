package org.springframework.roo.addon.requestfactory.entity;

import static org.springframework.roo.addon.requestfactory.entity.RepositoryJavaType.PAGE;
import static org.springframework.roo.addon.requestfactory.entity.RepositoryJavaType.PAGEABLE;
import static java.lang.reflect.Modifier.ABSTRACT;
import static org.springframework.roo.model.JdkJavaType.LIST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetailsBuilder;
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
 * This type produces metadata for a new ITD. It uses an {@link ItdTypeDetailsBuilder} provided by
 * {@link AbstractItdTypeDetailsProvidingMetadataItem} to register a field in the ITD and a new method.
 */
public class RepositoryMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    private static final InvocableMemberBodyBuilder BODY = new InvocableMemberBodyBuilder();

    private static final String PROVIDES_TYPE_STRING = RepositoryMetadata.class.getName();
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
    
    private final FieldMetadata parentProperty;
    private final JavaType domainType;

    public RepositoryMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            JavaType domainType,
            FieldMetadata parentProperty) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
            + identifier + "' does not appear to be a valid");

        this.parentProperty = parentProperty;
        this.domainType = domainType;

        if (!isValid()) {
            return;
        }

//        builder.addMethod(getFindEntriesByParentMethod());
//        builder.addMethod(getFindByParentMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    private MethodMetadata getFindEntriesByParentMethod() {
        if (parentProperty == null) {
            return null;
        }

        JavaSymbolName methodName = new JavaSymbolName("findBy"
                + parentProperty.getFieldName()
                .getSymbolNameCapitalisedFirstLetter());

        final MethodMetadata method = methodExists(methodName,
                new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            return method;
        }

        final JavaType[] parameterTypes = { parentProperty.getFieldType(),
                PAGEABLE };
        final String parentParamName = StringUtils.uncapitalize(parentProperty
                .getFieldType().getSimpleTypeName());
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName(parentParamName),
                new JavaSymbolName("pageable"));

        final JavaType returnType = new JavaType(
                PAGE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(domainType));

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), ABSTRACT, methodName, returnType,
                AnnotatedJavaType.convertFromJavaTypes(parameterTypes),
                parameterNames, BODY);

        return methodBuilder.build();
    }

    private MethodMetadata getFindByParentMethod() {
        if (parentProperty == null) {
            return null;
        }

        final JavaSymbolName methodName = new JavaSymbolName("findBy"
                + parentProperty.getFieldName()
                .getSymbolNameCapitalisedFirstLetter());

        final MethodMetadata method = methodExists(methodName,
                new ArrayList<AnnotatedJavaType>());
        if (method != null) {
            return method;
        }

        final JavaType returnType = new JavaType(
                LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(domainType));

        final JavaType[] parameterTypes = { parentProperty.getFieldType() };

        final String parentParamName = StringUtils.uncapitalize(parentProperty
                .getFieldType().getSimpleTypeName());
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName(parentParamName));
        
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), ABSTRACT, methodName, returnType,
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
