package org.springframework.roo.addon.requestfactory.entity;

import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.KEY;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.KEY_FACTORY;
import static org.springframework.roo.addon.requestfactory.entity.RepositoryJavaType.PAGE_REQUEST;
import static org.springframework.roo.model.JavaType.INT_PRIMITIVE;
import static org.springframework.roo.model.JavaType.LONG_PRIMITIVE;
import static org.springframework.roo.model.JavaType.STRING;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.SpringJavaType.AUTOWIRED;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.addon.layers.service.ServiceAnnotationValues;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * @author Stefan Schmidt
 * @since 1.2.0
 */
public class ServiceImplMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String PROVIDES_TYPE_STRING = ServiceImplMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public static String createIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static JavaType getJavaType(final String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static LogicalPath getPath(final String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(final String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }
        
    public ServiceImplMetadata(
            final String identifier,
            final JavaType aspectName,
            final PhysicalTypeMetadata governorPhysicalTypeMetadata,
            final MemberDetails governorDetails,
            final ServiceAnnotationValues annotationValues,
            final Map<JavaType, FieldMetadata> domainTypeToIdTypeMap,
            final Map<JavaType, FieldMetadata> domainTypeParentFields,
            final Map<JavaType, JavaType> domainTypeParentRepos,
            final Map<JavaType, String> domainTypePlurals) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.notNull(domainTypeParentFields, "Parent fields required");
        Validate.notNull(annotationValues, "Annotation values required");
        Validate.notNull(governorDetails, "Governor details required");
        Validate.notNull(domainTypePlurals, "Domain type plurals required");

        for (final Entry<JavaType, FieldMetadata> entry : domainTypeToIdTypeMap
                .entrySet()) {
            final JavaType domainType = entry.getKey();
            final FieldMetadata identifierField = entry.getValue();
            final FieldMetadata parentField = domainTypeParentFields.get(domainType);
            final String plural = domainTypePlurals.get(domainType);

            builder.addField(getParentRepositoryField(domainTypeParentRepos.get(domainType), parentField));
            builder.addMethod(getFindEntriesByParentMethod(domainType, identifierField, parentField));
            builder.addMethod(getFindByParentMethod(domainType, plural, identifierField, parentField));
            builder.addMethod(getCountByParentMethod(domainType, plural, identifierField, parentField));
            builder.addMethod(getFindByStringIdMethod(domainType, identifierField));
        }
        
        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    private MethodMetadata getFindByStringIdMethod(final JavaType domainType, final FieldMetadata identifierField) {
        if (!KEY.equals(identifierField.getFieldType())) {
            return null;
        }

        final String idFieldName = identifierField.getFieldName().getSymbolName();
        final JavaSymbolName methodName = new JavaSymbolName("find"
                + domainType.getSimpleTypeName() + "ByStringId");
        final JavaType parameterType = STRING;
        final List<JavaSymbolName> parameterNames = Arrays
                .asList(new JavaSymbolName(idFieldName));
        final JavaType returnType = domainType;

        // Check if a method with the same signature already exists in the target type
        final MethodMetadata method = getGovernorMethod(methodName, parameterType);
        if (method != null) {
            // If it already exists, just return the method and omit its generation via the ITD
            return method;
        }

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder.appendFormalLine("if (" + idFieldName + " == null || "
                + idFieldName + ".length() == 0) return null;");

        bodyBuilder.appendFormalLine("return " + StringUtils.uncapitalize(domainType
                .getSimpleTypeName()) + "Repository.findOne("
                + KEY_FACTORY.getNameIncludingTypeParameters(true, builder
                        .getImportRegistrationResolver())
                + ".stringToKey(" + idFieldName + "));");

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, returnType,
                AnnotatedJavaType.convertFromJavaTypes(parameterType),
                parameterNames, bodyBuilder);

        return methodBuilder.build();
    }

    private MethodMetadata getFindEntriesByParentMethod(final JavaType domainType,
            final FieldMetadata identifierField,
            final FieldMetadata parentProperty) {
        if (parentProperty == null) {
            return null;
        }

        final JavaType idType = KEY.equals(identifierField.getFieldType())
                ? STRING : identifierField.getFieldType();
        final JavaType[] parameterTypes = { idType, INT_PRIMITIVE, INT_PRIMITIVE };

        JavaSymbolName methodName = new JavaSymbolName("find" + domainType
                .getSimpleTypeName() + "EntriesByParentId");

        final MethodMetadata method = getGovernorMethod(methodName,
                parameterTypes);
        if (method != null) {
            return method;
        }

        final String idParamName = StringUtils.uncapitalize(parentProperty
                .getFieldType().getSimpleTypeName()) + "Id";
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName(idParamName),
                new JavaSymbolName("firstResult"),
                new JavaSymbolName("maxResults"));
        final JavaType returnType = new JavaType(
                LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(domainType));

        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        final String idParam = KEY.equals(identifierField)
                ? (KEY_FACTORY.getNameIncludingTypeParameters(true,
                        builder.getImportRegistrationResolver())
                        + ".stringToKey(" + idParamName + ")") : idParamName;
        bodyBuilder.appendFormalLine("final "
                + parentProperty.getFieldType().getNameIncludingTypeParameters(
                        false, builder.getImportRegistrationResolver())
                + " parent = " + StringUtils.uncapitalize(parentProperty
                        .getFieldType().getSimpleTypeName())
                + "Repository" + ".findOne(" + idParam + ");");

        bodyBuilder.appendFormalLine("return " + StringUtils.uncapitalize(
                domainType.getSimpleTypeName()) + "Repository.findBy"
                + parentProperty.getFieldType().getSimpleTypeName()
                + "(parent, new "
                + PAGE_REQUEST.getNameIncludingTypeParameters(false,
                        builder.getImportRegistrationResolver()) 
                + "(firstResult / maxResults, maxResults)).getContent();");

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, returnType,
                AnnotatedJavaType.convertFromJavaTypes(parameterTypes),
                parameterNames, bodyBuilder);

        return methodBuilder.build();
    }

    private MethodMetadata getFindByParentMethod(final JavaType domainType,
            final String plural,
            final FieldMetadata identifierField,
            final FieldMetadata parentProperty) {
        if (parentProperty == null) {
            return null;
        }

        JavaSymbolName methodName = new JavaSymbolName("find" + plural
                + "ByParentId");

        final JavaType idType = KEY.equals(identifierField.getFieldType())
                ? STRING : identifierField.getFieldType();
        final JavaType[] parameterTypes = { idType };

        final MethodMetadata method = getGovernorMethod(methodName,
                parameterTypes);
        if (method != null) {
            return method;
        }

        final String idParamName = StringUtils.uncapitalize(parentProperty
                .getFieldType().getSimpleTypeName()) + "Id";
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName(idParamName));
        final JavaType returnType = new JavaType(
                LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(domainType));

        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        final String idParam = KEY.equals(identifierField)
                ? (KEY_FACTORY.getNameIncludingTypeParameters(true,
                        builder.getImportRegistrationResolver())
                        + ".stringToKey(" + idParamName + ")") : idParamName;
        bodyBuilder.appendFormalLine("final "
                + parentProperty.getFieldType().getNameIncludingTypeParameters(
                        false, builder.getImportRegistrationResolver())
                + " parent = " + StringUtils.uncapitalize(parentProperty
                        .getFieldType().getSimpleTypeName())
                + "Repository" + ".findOne(" + idParam + ");");

        bodyBuilder.appendFormalLine("return " + StringUtils.uncapitalize(
                domainType.getSimpleTypeName()) + "Repository.findBy"
                + parentProperty.getFieldType().getSimpleTypeName()
                + "(parent);");

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, returnType,
                AnnotatedJavaType.convertFromJavaTypes(parameterTypes),
                parameterNames, bodyBuilder);

        return methodBuilder.build();
    }

    private MethodMetadata getCountByParentMethod(final JavaType domainType,
            final String plural,
            final FieldMetadata identifierField, final FieldMetadata parentProperty) {
        if (parentProperty == null) {
            return null;
        }

        final JavaType idType = KEY.equals(identifierField.getFieldType())
                ? STRING : identifierField.getFieldType();
        final JavaType[] parameterTypes = { idType };

        JavaSymbolName methodName = new JavaSymbolName("count" + plural + "ByParentId");

        final MethodMetadata method = getGovernorMethod(methodName, parameterTypes);
        if (method != null) {
            return method;
        }

        final String idParamName = StringUtils.uncapitalize(parentProperty
                .getFieldType().getSimpleTypeName()) + "Id";
        final List<JavaSymbolName> parameterNames = Arrays.asList(
                new JavaSymbolName(idParamName));

        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        final String idParam = KEY.equals(identifierField)
                ? (KEY_FACTORY.getNameIncludingTypeParameters(true,
                        builder.getImportRegistrationResolver())
                        + ".stringToKey(" + idParamName + ")") : idParamName;
        bodyBuilder.appendFormalLine("final "
                + parentProperty.getFieldType().getNameIncludingTypeParameters(
                        false, builder.getImportRegistrationResolver())
                + " parent = " + StringUtils.uncapitalize(parentProperty
                        .getFieldType().getSimpleTypeName())
                + "Repository" + ".findOne(" + idParam + ");");

        bodyBuilder.appendFormalLine("return " + StringUtils.uncapitalize(
                domainType.getSimpleTypeName()) + "Repository.findBy"
                + parentProperty.getFieldType().getSimpleTypeName()
                + "(parent).size();");

        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, methodName, LONG_PRIMITIVE,
                AnnotatedJavaType.convertFromJavaTypes(parameterTypes),
                parameterNames, bodyBuilder);

        return methodBuilder.build();
    }
    
    private FieldMetadata getParentRepositoryField(final JavaType parentRepository,
            final FieldMetadata parentProperty) {
        if (parentProperty == null) {
            return null;
        }
        
        final JavaSymbolName fieldName = new JavaSymbolName(
                StringUtils.uncapitalize(parentProperty.getFieldType()
                        .getSimpleTypeName()) + "Repository");

        final FieldMetadata existing = governorTypeDetails.getField(fieldName);
        if (existing != null) {
            return existing;
        }

        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        final AnnotationMetadataBuilder autoWiredAnnotation = new AnnotationMetadataBuilder(AUTOWIRED);
        annotations.add(autoWiredAnnotation);

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                getId(), 0,  annotations, fieldName, parentRepository);

        return fieldBuilder.build();
    }

    @Override
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
