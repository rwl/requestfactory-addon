package org.springframework.roo.addon.requestfactory.entity;

import static java.lang.reflect.Modifier.ABSTRACT;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY_ENTITY;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY_REPOSITORY;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY_SERVICE;
import static org.springframework.roo.addon.requestfactory.entity.RepositoryJavaType.PAGE;
import static org.springframework.roo.addon.requestfactory.entity.RepositoryJavaType.PAGEABLE;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ACTIVE_RECORD;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ENTITY;
import static org.springframework.roo.model.RooJavaType.ROO_MONGO_ENTITY;
import static org.springframework.roo.model.RooJavaType.ROO_PLURAL;
import static org.springframework.roo.model.RooJavaType.ROO_TO_STRING;
import static org.springframework.roo.model.JdkJavaType.LIST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.layers.repository.jpa.RepositoryJpaLocator;
import org.springframework.roo.addon.layers.repository.mongo.RepositoryMongoLocator;
import org.springframework.roo.addon.layers.service.ServiceInterfaceLocator;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooRequestFactoryEntity;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.CollectionUtils;


/**
 * Implementation of operations this add-on offers.
 */
@Component
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

    @Reference private ServiceInterfaceLocator serviceInterfaceLocator;

    @Reference private RepositoryJpaLocator repositoryJpaLocator;

    @Reference private RepositoryMongoLocator repositoryMongoLocator;

    @Reference private PathResolver pathResolver;

    public boolean isCommandAvailable() {
        if (!projectOperations.isFocusedProjectAvailable()) {
            return false;
        }
        if (typeLocationService.findTypesWithAnnotation(ROO_JPA_ACTIVE_RECORD,
               ROO_JPA_ENTITY, ROO_MONGO_ENTITY).size() == 0) {
            return false;
        }
        return true;
    }

    public void annotateType(JavaType javaType, final JavaSymbolName parentProperty, final JavaSymbolName primaryProperty, final JavaSymbolName secondaryProperty) {
        Validate.notNull(javaType, "Java type required");

        ClassOrInterfaceTypeDetails domainTypeDetails = typeLocationService.getTypeDetails(javaType);
        if (domainTypeDetails == null) {
            return;
        }

        boolean layered = false;
        for (ClassOrInterfaceTypeDetails service : serviceInterfaceLocator.getServiceInterfaces(javaType)) {
            if (MemberFindingUtils.getAnnotationOfType(service.getAnnotations(), ROO_REQUEST_FACTORY_SERVICE) == null) {
                ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(service);
                AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(ROO_REQUEST_FACTORY_SERVICE);
                cidBuilder.addAnnotation(annotationBuilder.build());
                typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
            }
            layered = true;
        }

        // Service layer required to delegate to Pageable finders
        if (layered) {
            if (MemberFindingUtils.getAnnotationOfType(domainTypeDetails.getAnnotations(), ROO_MONGO_ENTITY) != null) {
                for (ClassOrInterfaceTypeDetails repository : repositoryMongoLocator.getRepositories(javaType)) {
                    annotateRepository(domainTypeDetails, parentProperty, repository);
                }
            } else {
                for (ClassOrInterfaceTypeDetails repository : repositoryJpaLocator.getRepositories(javaType)) {
                    annotateRepository(domainTypeDetails, parentProperty, repository);
                }
            }
        } else if (MemberFindingUtils.getAnnotationOfType(domainTypeDetails.getAnnotations(), ROO_JPA_ACTIVE_RECORD) == null) {
            LOGGER.severe("Service layer and repository layer required when active records not used");
            return;
        }

        if (MemberFindingUtils.getAnnotationOfType(domainTypeDetails.getAnnotations(), ROO_REQUEST_FACTORY_ENTITY) == null) {
            ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(domainTypeDetails);

            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(ROO_REQUEST_FACTORY_ENTITY);
            if (parentProperty != null) {
                annotationBuilder.addStringAttribute(RooRequestFactoryEntity.PARENT_PROPERTY_ATTRIBUTE, parentProperty.getSymbolName());
            }
            if (primaryProperty != null) {
                annotationBuilder.addStringAttribute(RooRequestFactoryEntity.PRIMARY_PROPERTY_ATTRIBUTE, primaryProperty.getSymbolName());
            }
            if (secondaryProperty != null) {
                annotationBuilder.addStringAttribute(RooRequestFactoryEntity.SECONDARY_PROPERTY_ATTRIBUTE, secondaryProperty.getSymbolName());
            }

            classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());

            typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
        }
    }

    private void annotateRepository(final ClassOrInterfaceTypeDetails domainTypeDetails,
            final JavaSymbolName parentFieldName,
            final ClassOrInterfaceTypeDetails repository) {
        Validate.notNull(domainTypeDetails);
        Validate.notNull(repository);

        if (MemberFindingUtils.getAnnotationOfType(repository.getAnnotations(), ROO_REQUEST_FACTORY_REPOSITORY) == null) {
            ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(repository);
            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(ROO_REQUEST_FACTORY_REPOSITORY);
            classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
            typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
        }

        if (parentFieldName != null && !StringUtils.isEmpty(parentFieldName.getSymbolName())) {
            final JavaSymbolName finderName = new JavaSymbolName("findBy"
                    + parentFieldName.getSymbolNameCapitalisedFirstLetter());
            if (repository.getMethod(finderName) == null) {
                FieldMetadata parentField = null;
                for (FieldMetadata field : domainTypeDetails.getDeclaredFields()) {
                    if (field.getFieldName().equals(parentFieldName)) {
                        parentField = field;
                        break;
                    }
                }
                if (parentField == null) {
                    return;
                }

                final String interfaceIdentifier = pathResolver
                        .getFocusedCanonicalPath(Path.SRC_MAIN_JAVA, repository.getType());

                final String interfaceMdId = PhysicalTypeIdentifier.createIdentifier(
                        repository.getType(), pathResolver.getPath(interfaceIdentifier));

                final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(repository);


                final JavaType[] entriesParameterTypes = { parentField.getFieldType(),
                        PAGEABLE };
                final List<JavaSymbolName> entriesParameterNames = Arrays.asList(
                        parentFieldName,
                        new JavaSymbolName("pageable"));

                final JavaType pageReturnType = new JavaType(
                        PAGE.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                        Arrays.asList(domainTypeDetails.getType()));

                MethodMetadataBuilder entriesMethodBuilder = new MethodMetadataBuilder(
                        interfaceMdId, 0, finderName, pageReturnType,
                        AnnotatedJavaType.convertFromJavaTypes(entriesParameterTypes),
                        entriesParameterNames, new InvocableMemberBodyBuilder());

                cidBuilder.addMethod(entriesMethodBuilder);


                final JavaType[] parameterTypes = { parentField.getFieldType() };
                final List<JavaSymbolName> parameterNames = Arrays.asList(
                        parentFieldName );

                final JavaType listReturnType = new JavaType(
                        LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                        Arrays.asList(domainTypeDetails.getType()));

                MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                        interfaceMdId, 0, finderName, listReturnType,
                        AnnotatedJavaType.convertFromJavaTypes(parameterTypes),
                        parameterNames, new InvocableMemberBodyBuilder());

                cidBuilder.addMethod(methodBuilder);

                typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
            }
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