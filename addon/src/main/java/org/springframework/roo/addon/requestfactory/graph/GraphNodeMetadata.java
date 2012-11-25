package org.springframework.roo.addon.requestfactory.graph;

import static org.springframework.roo.model.JavaType.DOUBLE_OBJECT;
import static org.springframework.roo.model.Jsr303JavaType.NOT_NULL;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
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
 * This type produces metadata for a new graph node ITD.
 */
public class GraphNodeMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String PROVIDES_TYPE_STRING = GraphNodeMetadata
            .class.getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(
            final String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(
            final String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(final String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    private final GraphNodeAnnotationValues graphNodeAnnotationValues;

    private FieldMetadata xField;
    private FieldMetadata yField;

    public GraphNodeMetadata(final String identifier,
            final JavaType aspectName,
            final PhysicalTypeMetadata governorPhysicalTypeMetadata,
            final GraphNodeAnnotationValues graphNodeAnnotationValues) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");
        this.graphNodeAnnotationValues = graphNodeAnnotationValues;

        xField = getCoordField(this.graphNodeAnnotationValues.getX());
        builder.addField(xField);
        yField = getCoordField(this.graphNodeAnnotationValues.getY());
        builder.addField(yField);

        builder.addMethod(getDeclaredGetter(xField));
        builder.addMethod(getDeclaredSetter(xField));
        builder.addMethod(getDeclaredGetter(yField));
        builder.addMethod(getDeclaredSetter(yField));

        itdTypeDetails = builder.build();
    }

    private FieldMetadata getCoordField(final String fieldNameString) {
        final JavaSymbolName fieldName = new JavaSymbolName(
                fieldNameString);

        final FieldMetadata existing = governorTypeDetails
                .getField(fieldName);
        if (existing != null) {
            return existing;
        }

        int modifier = 0;

        final List<AnnotationMetadataBuilder> annotations =
                new ArrayList<AnnotationMetadataBuilder>();
        final AnnotationMetadataBuilder notNullAnnotation =
                new AnnotationMetadataBuilder(NOT_NULL);
        annotations.add(notNullAnnotation);


        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                getId(), modifier, annotations, fieldName, DOUBLE_OBJECT);
        fieldBuilder.setFieldInitializer("0.0");

        return fieldBuilder.build();
    }

    private MethodMetadataBuilder getDeclaredGetter(final FieldMetadata field) {
        Validate.notNull(field, "Field required");

        final JavaSymbolName methodName = BeanInfoUtils
                .getAccessorMethodName(field);

        if (governorHasMethod(methodName)) {
            return null;
        }

        final InvocableMemberBodyBuilder bodyBuilder =
                new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("return this."
                + field.getFieldName().getSymbolName() + ";");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                methodName, field.getFieldType(), bodyBuilder);
    }

    private MethodMetadataBuilder getDeclaredSetter(final FieldMetadata field) {
        Validate.notNull(field, "Field required");

        final JavaSymbolName methodName = BeanInfoUtils
                .getMutatorMethodName(field);

        final JavaType parameterType = field.getFieldType();

        if (governorHasMethod(methodName, parameterType)) {
            return null;
        }

        final List<JavaSymbolName> parameterNames = Arrays.asList(field
                .getFieldName());

        final InvocableMemberBodyBuilder bodyBuilder =
                new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("this."
                + field.getFieldName().getSymbolName() + " = "
                + field.getFieldName().getSymbolName() + ";");

        return new MethodMetadataBuilder(getId(), Modifier.PUBLIC,
                methodName, JavaType.VOID_PRIMITIVE,
                AnnotatedJavaType.convertFromJavaTypes(parameterType),
                parameterNames, bodyBuilder);
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
