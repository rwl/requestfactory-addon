package org.springframework.roo.addon.gwt.bootstrap;

import static org.springframework.roo.addon.gwt.bootstrap.GwtBootstrapJavaType.ROO_GWT_BOOTSTRAP;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.JpaJavaType.ENTITY;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ACTIVE_RECORD;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ENTITY;
import static org.springframework.roo.model.SpringJavaType.PERSISTENT;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.Cardinality;
import org.springframework.roo.classpath.operations.Fetch;
import org.springframework.roo.classpath.operations.jsr303.CollectionField;
import org.springframework.roo.classpath.operations.jsr303.DateField;
import org.springframework.roo.classpath.operations.jsr303.FieldDetails;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.ReservedWords;
import org.springframework.roo.project.ProjectOperations;

/**
 * Implementation of operations this add-on offers.
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class GwtBootstrapOperationsImpl implements GwtBootstrapOperations {

    @Reference private MemberDetailsScanner memberDetailsScanner;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties, etc into the project configuration
     */
    @Reference private ProjectOperations projectOperations;

    /**
     * Use TypeLocationService to find types which are annotated with a given annotation in the project
     */
    @Reference private TypeLocationService typeLocationService;

    /**
     * Use TypeManagementService to change types
     */
    @Reference private TypeManagementService typeManagementService;

    /** {@inheritDoc} */
    public boolean isCommandAvailable() {

        // Check if a project has been created
        if (!projectOperations.isFocusedProjectAvailable()) {
            return false;
        }
        if (typeLocationService.findTypesWithAnnotation(ROO_JPA_ACTIVE_RECORD, ROO_JPA_ENTITY).size() == 0) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public void annotateType(JavaType javaType, final JavaSymbolName parentName) {
        // Use Roo's Assert type for null checks
        Validate.notNull(javaType, "Java type required");

        // Obtain ClassOrInterfaceTypeDetails for this java type
        ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);

        // Test if the annotation already exists on the target type
        if (existing != null && MemberFindingUtils.getAnnotationOfType(existing.getAnnotations(), ROO_GWT_BOOTSTRAP) == null) {
            ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);

            // Create JavaType instance for the add-ons trigger annotation
//            JavaType rooRooExample = new JavaType(RooGwtBootstrap.class.getName());

            // Create Annotation metadata
            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(ROO_GWT_BOOTSTRAP);
            if (parentName != null) {
                annotationBuilder.addStringAttribute(RooGwtBootstrap.PARENT_FIELD_ATTRIBUTE, parentName.getSymbolName());
            }

            // Add annotation to target type
            classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());

            // Save changes to disk
            typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
        }
    }

    @Override
    public void addFieldListJpa(JavaSymbolName fieldName, JavaType fieldType, JavaType typeName, JavaSymbolName mappedBy,
            boolean notNull, boolean nullRequired, Integer sizeMin, Integer sizeMax, Cardinality cardinality, Fetch fetch,
            String comment, boolean transientModifier, boolean permitReservedWords) {

        final ClassOrInterfaceTypeDetails cid = typeLocationService
                .getTypeDetails(fieldType);
        Validate.notNull(
                cid,
                "The specified target '--type' does not exist or can not be found. Please create this type first.");

        // Check if the requested entity is a JPA @Entity
        final MemberDetails memberDetails = memberDetailsScanner
                .getMemberDetails(this.getClass().getName(), cid);
        final AnnotationMetadata entityAnnotation = memberDetails
                .getAnnotation(ENTITY);
        final AnnotationMetadata persistentAnnotation = memberDetails
                .getAnnotation(PERSISTENT);

        if (entityAnnotation != null) {
            Validate.isTrue(cardinality == Cardinality.ONE_TO_MANY
                    || cardinality == Cardinality.MANY_TO_MANY,
                    "Cardinality must be ONE_TO_MANY or MANY_TO_MANY for the field set command");
        }
        else if (cid.getPhysicalTypeCategory() == PhysicalTypeCategory.ENUMERATION) {
            cardinality = null;
        }
        else if (persistentAnnotation != null) {
            // Yes, we can deal with that
        }
        else {
            throw new IllegalStateException(
                    "The field list command is only applicable to enum, JPA @Entity or Spring Data @Persistence elements");
        }

        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService
                .getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '" + typeName
                + "'doesn't exist");

        final String physicalTypeIdentifier = javaTypeDetails
                .getDeclaredByMetadataId();
        final ListField fieldDetails = new ListField(physicalTypeIdentifier,
                new JavaType(LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                        null, Arrays.asList(fieldType)), fieldName, fieldType,
                cardinality);
        fieldDetails.setNotNull(notNull);
        fieldDetails.setNullRequired(nullRequired);
        if (sizeMin != null) {
            fieldDetails.setSizeMin(sizeMin);
        }
        if (sizeMax != null) {
            fieldDetails.setSizeMax(sizeMax);
        }
        if (mappedBy != null) {
            fieldDetails.setMappedBy(mappedBy);
        }
        if (fetch != null) {
            fieldDetails.setFetch(fetch);
        }
        if (comment != null) {
            fieldDetails.setComment(comment);
        }

        insertField(fieldDetails, permitReservedWords, transientModifier);
    }

    private void insertField(final FieldDetails fieldDetails,
            final boolean permitReservedWords, final boolean transientModifier) {
        if (!permitReservedWords) {
            ReservedWords.verifyReservedWordsNotPresent(fieldDetails
                    .getFieldName());
            if (fieldDetails.getColumn() != null) {
                ReservedWords.verifyReservedWordsNotPresent(fieldDetails
                        .getColumn());
            }
        }

        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        fieldDetails.decorateAnnotationsList(annotations);
        String initializer = null;
        if (fieldDetails instanceof CollectionField) {
            final CollectionField collectionField = (CollectionField) fieldDetails;
            initializer = "new " + collectionField.getInitializer() + "()";
        }
        else if (fieldDetails instanceof DateField
                && fieldDetails.getFieldName().getSymbolName()
                        .equals("created")) {
            initializer = "new Date()";
        }
        int modifier = Modifier.PRIVATE;
        if (transientModifier) {
            modifier += Modifier.TRANSIENT;
        }

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                fieldDetails.getPhysicalTypeIdentifier(), modifier,
                annotations, fieldDetails.getFieldName(),
                fieldDetails.getFieldType());
        fieldBuilder.setFieldInitializer(initializer);
        typeManagementService.addField(fieldBuilder.build());
    }
}