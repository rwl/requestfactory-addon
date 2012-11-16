package org.springframework.roo.addon.requestfactory.entity;

import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY_ENTITY;
import static org.springframework.roo.model.RooJavaType.ROO_MONGO_ENTITY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.layers.repository.jpa.RepositoryJpaLocator;
import org.springframework.roo.addon.layers.repository.mongo.RepositoryMongoLocator;
import org.springframework.roo.addon.layers.service.ServiceAnnotationValues;
import org.springframework.roo.addon.layers.service.ServiceClassMetadata;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooRequestFactoryEntity;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.itd.AbstractMemberDiscoveringItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Provides {@link ServiceClassMetadata} for building the ITD for the
 * implementation class of a user project's service.
 * 
 * @author Stefan Schmidt
 * @author Andrew Swan
 * @since 1.2.0
 */
@Component(immediate = true)
@Service
public class ServiceImplMetadataProvider extends
        AbstractMemberDiscoveringItdMetadataProvider {
    
    @Reference RepositoryJpaLocator repositoryJpaLocator;
    @Reference RepositoryMongoLocator repositoryMongoLocator;
    
    private final Map<JavaType, String> managedEntityTypes =
            new HashMap<JavaType, String>();

    protected void activate(final ComponentContext context) {
        metadataDependencyRegistry.addNotificationListener(this);
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        setIgnoreTriggerAnnotations(true);
    }

    @Override
    protected String createLocalIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return ServiceImplMetadata.createIdentifier(javaType, path);
    }

    protected void deactivate(final ComponentContext context) {
        metadataDependencyRegistry.removeNotificationListener(this);
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            final String metadataIdentificationString) {
        final JavaType javaType = ServiceImplMetadata
                .getJavaType(metadataIdentificationString);
        final LogicalPath path = ServiceImplMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    public String getItdUniquenessFilenameSuffix() {
        return "RequestFactory_Service";
    }

    @Override
    protected String getLocalMidToRequest(final ItdTypeDetails itdTypeDetails) {
        // Determine the governor for this ITD, and whether any metadata is even
        // hoping to hear about changes to that JavaType and its ITDs
        final JavaType governor = itdTypeDetails.getName();
        final String localMid = managedEntityTypes.get(governor);
        if (localMid != null) {
            return localMid;
        }

        final MemberHoldingTypeDetails memberHoldingTypeDetails =
                typeLocationService.getTypeDetails(governor);
        if (memberHoldingTypeDetails != null) {
            for (final JavaType type : memberHoldingTypeDetails
                    .getLayerEntities()) {
                final String localMidType = managedEntityTypes.get(type);
                if (localMidType != null) {
                    return localMidType;
                }
            }
        }
        return null;
    }

    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            final String metadataIdentificationString,
            final JavaType aspectName,
            final PhysicalTypeMetadata governorPhysicalTypeMetadata,
            final String itdFilename) {
        final ClassOrInterfaceTypeDetails serviceClass =
                governorPhysicalTypeMetadata.getMemberHoldingTypeDetails();
        if (serviceClass == null) {
            return null;
        }
        ServiceMetadata serviceInterfaceMetadata = null;
        for (final JavaType implementedType : serviceClass.getImplementsTypes()) {
            final ClassOrInterfaceTypeDetails potentialServiceInterfaceTypeDetails = 
                    typeLocationService.getTypeDetails(implementedType);
            if (potentialServiceInterfaceTypeDetails != null) {
                final LogicalPath path = PhysicalTypeIdentifier.getPath(
                        potentialServiceInterfaceTypeDetails.getDeclaredByMetadataId());
                final String implementedTypeId = ServiceMetadata
                        .createIdentifier(implementedType, path);
                if ((serviceInterfaceMetadata = (ServiceMetadata) metadataService
                        .get(implementedTypeId)) != null) {
                    // Found the metadata for the service interface
                    break;
                }
            }
        }
        if (serviceInterfaceMetadata == null || !serviceInterfaceMetadata.isValid()) {
            return null;
        }

        // Register this provider for changes to the service interface // TODO
        // move this down in case we return null early below?
        metadataDependencyRegistry.registerDependency(serviceInterfaceMetadata
                .getId(), metadataIdentificationString);

        final ServiceAnnotationValues serviceAnnotationValues = 
                serviceInterfaceMetadata.getServiceAnnotationValues();
        final JavaType[] domainTypes = serviceAnnotationValues
                .getDomainTypes();

        /*
         * For each domain type, collect (1) the plural and (2) the parent
         * property field.
         */
        final Map<JavaType, String> domainTypePlurals = new HashMap<JavaType, String>();
        final Map<JavaType, FieldMetadata> domainTypeParentFields = new HashMap<JavaType, FieldMetadata>();
        final Map<JavaType, JavaType> domainTypeParentRepos = new HashMap<JavaType, JavaType>();
        final Map<JavaType, FieldMetadata> domainTypeToIdTypeMap = new HashMap<JavaType, FieldMetadata>();
        for (final JavaType domainType : domainTypes) {
            final List<FieldMetadata> idFields = persistenceMemberLocator
                    .getIdentifierFields(domainType);
            if (idFields.size() != 1) {
                // The ID field metadata is either unavailable or not stable yet
                return null;
            }
            final FieldMetadata idField = idFields.get(0);
            domainTypeToIdTypeMap.put(domainType, idField);
            
            // Collect the plural for this domain type
            final ClassOrInterfaceTypeDetails domainTypeDetails = 
                    typeLocationService.getTypeDetails(domainType);
            if (domainTypeDetails == null) {
                return null;
            }
            final LogicalPath path = PhysicalTypeIdentifier.getPath(
                    domainTypeDetails.getDeclaredByMetadataId());
            final String pluralId = PluralMetadata.createIdentifier(
                    domainType, path);
            final PluralMetadata pluralMetadata =
                    (PluralMetadata) metadataService.get(pluralId);
            if (pluralMetadata == null) {
                return null;
            }
            domainTypePlurals.put(domainType, pluralMetadata.getPlural());

            FieldMetadata parentField = null;
            final String parentPropertyName = RequestFactoryUtils
                    .getStringAnnotationValue(domainTypeDetails,
                            ROO_REQUEST_FACTORY_ENTITY,
                            RooRequestFactoryEntity.PARENT_PROPERTY_ATTRIBUTE,
                            "");
            if (!parentPropertyName.isEmpty()) {
                for (FieldMetadata field : domainTypeDetails
                        .getDeclaredFields()) {
                    if (field.getFieldName().getSymbolName()
                            .equals(parentPropertyName)) {
                        parentField = field;
                        break;
                    }
                }
                if (parentField == null) {
                    return null;
                }
            }
            domainTypeParentFields.put(domainType, parentField);
            
            if (parentField != null) {
                final ClassOrInterfaceTypeDetails parentRepoDetails;
                if (domainTypeDetails.getAnnotation(ROO_MONGO_ENTITY) != null) {
                    parentRepoDetails = repositoryMongoLocator.getRepositories(parentField.getFieldType()).iterator().next();
                } else {
                    parentRepoDetails = repositoryJpaLocator.getRepositories(parentField.getFieldType()).iterator().next();
                }
                domainTypeParentRepos.put(domainType, parentRepoDetails.getType());
            }

            // Maintain a list of entities that are being handled by this layer
            managedEntityTypes.put(domainType, metadataIdentificationString);

            // Register this provider for changes to the domain type or its
            // plural
            metadataDependencyRegistry.registerDependency(domainTypeDetails
                    .getDeclaredByMetadataId(), metadataIdentificationString);
            metadataDependencyRegistry.registerDependency(pluralId,
                    metadataIdentificationString);
        }
        final MemberDetails serviceClassDetails = memberDetailsScanner
                .getMemberDetails(getClass().getName(), serviceClass);
        return new ServiceImplMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, serviceClassDetails,
                serviceAnnotationValues, domainTypeToIdTypeMap, domainTypeParentFields,
                domainTypeParentRepos, domainTypePlurals);
    }

    public String getProvidesType() {
        return ServiceImplMetadata.getMetadataIdentiferType();
    }
}
