package org.springframework.roo.addon.requestfactory.entity;

import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ACTIVE_RECORD;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ENTITY;
import static org.springframework.roo.model.RooJavaType.ROO_PLURAL;
import static org.springframework.roo.model.RooJavaType.ROO_TO_STRING;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooRequestFactory;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.CollectionUtils;


/**
 * Implementation of operations this add-on offers.
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class EntityOperationsImpl implements EntityOperations {

    private static final Logger LOGGER = HandlerUtils.getLogger(EntityOperationsImpl.class);

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

    public void annotateType(JavaType javaType, final JavaSymbolName parentProperty, final JavaSymbolName primaryProperty, final JavaSymbolName secondaryProperty) {
        // Use Roo's Assert type for null checks
        Validate.notNull(javaType, "Java type required");

        // Obtain ClassOrInterfaceTypeDetails for this java type
        ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);

        // Test if the annotation already exists on the target type
        if (existing != null && MemberFindingUtils.getAnnotationOfType(existing.getAnnotations(), ROO_REQUEST_FACTORY) == null) {
            ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);

            // Create Annotation metadata
            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(ROO_REQUEST_FACTORY);
            if (parentProperty != null) {
                annotationBuilder.addStringAttribute(RooRequestFactory.PARENT_PROPERTY_ATTRIBUTE, parentProperty.getSymbolName());
            }
            if (primaryProperty != null) {
                annotationBuilder.addStringAttribute(RooRequestFactory.PRIMARY_PROPERTY_ATTRIBUTE, primaryProperty.getSymbolName());
            }
            if (secondaryProperty != null) {
                annotationBuilder.addStringAttribute(RooRequestFactory.SECONDARY_PROPERTY_ATTRIBUTE, secondaryProperty.getSymbolName());
            }

            // Add annotation to target type
            classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());

            // Save changes to disk
            typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
        }
    }

    @Override
    public void annotateTypeWithPlural(JavaType javaType, String name) {
        Validate.notNull(javaType, "Java type required");

        ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);
        if (existing != null && MemberFindingUtils.getAnnotationOfType(existing.getAnnotations(), ROO_PLURAL) == null) {
            ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);

            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(ROO_PLURAL);
            if (name != null) {
                annotationBuilder.addStringAttribute("value", name);
            }

            classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());

            typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
        }
    }

    @Override
    public void annotateTypeWithToString(JavaType javaType, Set<String> excludeFields, String methodName) {
        Validate.notNull(javaType, "Java type required");

        ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);
        if (existing != null) {
            ClassOrInterfaceTypeDetailsBuilder cid = new ClassOrInterfaceTypeDetailsBuilder(existing);

            final List<StringAttributeValue> excludeFieldsList = new ArrayList<StringAttributeValue>();
            if (!CollectionUtils.isEmpty(excludeFields)) {
                for (final String field : excludeFields) {
                    if (existing.getField(new JavaSymbolName(field)) == null) {
                        LOGGER.warning("-excludeFields option can only contain existing field names");
                        return;
                    }
                    excludeFieldsList.add(new StringAttributeValue(new JavaSymbolName(
                            "value"), field));
                }
            }

            if (MemberFindingUtils.getAnnotationOfType(existing.getAnnotations(), ROO_TO_STRING) != null) {
                cid.removeAnnotation(ROO_TO_STRING);
            }

            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(ROO_TO_STRING);
            if (methodName != null) {
                annotationBuilder.addStringAttribute("toStringMethod", methodName);
            }
            if (excludeFields != null) {
                ArrayAttributeValue<StringAttributeValue> aav = new ArrayAttributeValue<StringAttributeValue>(
                        new JavaSymbolName("excludeFields"), excludeFieldsList);
                annotationBuilder.addAttribute(aav);
            }

            cid.addAnnotation(annotationBuilder.build());

            typeManagementService.createOrUpdateTypeOnDisk(cid.build());
        }
    }
}