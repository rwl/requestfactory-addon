package org.springframework.roo.addon.requestfactory.entity;

import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY_ENTITY;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY_REPOSITORY;
import static org.springframework.roo.model.RooJavaType.ROO_REPOSITORY_MONGO;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.layers.repository.jpa.RepositoryJpaAnnotationValues;
import org.springframework.roo.addon.layers.repository.mongo.RepositoryMongoAnnotationValues;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooRequestFactoryEntity;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.customdata.taggers.CustomDataKeyDecorator;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.itd.AbstractMemberDiscoveringItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Provides {@link RepositoryMetadata}.
 */
@Component(immediate = true)
@Service
public final class RepositoryMetadataProviderImpl extends AbstractMemberDiscoveringItdMetadataProvider
        implements RepositoryMetadataProvider {

    @Reference private CustomDataKeyDecorator customDataKeyDecorator;
    private final Map<JavaType, String> domainTypeToRepositoryMidMap = new LinkedHashMap<JavaType, String>();
    private final Map<String, JavaType> repositoryMidToDomainTypeMap = new LinkedHashMap<String, JavaType>();

    /**
     * The activate method for this OSGi component.
     *
     * @param context the component context can be used to get access to the
     * OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        super.setDependsOnGovernorBeingAClass(false);
        metadataDependencyRegistry.addNotificationListener(this);
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(ROO_REQUEST_FACTORY_REPOSITORY);
        registerMatchers();
    }

    /**
     * The deactivate method for this OSGi component.
     *
     * @param context the component context can be used to get access to the
     * OSGi container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.removeNotificationListener(this);
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(ROO_REQUEST_FACTORY_REPOSITORY);
        customDataKeyDecorator.unregisterMatchers(getClass());
    }

    @Override
    protected String getLocalMidToRequest(final ItdTypeDetails itdTypeDetails) {
        // Determine the governor for this ITD, and whether any metadata is even
        // hoping to hear about changes to that JavaType and its ITDs
        final JavaType governor = itdTypeDetails.getName();
        final String localMid = domainTypeToRepositoryMidMap.get(governor);
        if (localMid != null) {
            return localMid;
        }

        final MemberHoldingTypeDetails memberHoldingTypeDetails = typeLocationService
                .getTypeDetails(governor);
        if (memberHoldingTypeDetails != null) {
            for (final JavaType type : memberHoldingTypeDetails
                    .getLayerEntities()) {
                final String localMidType = domainTypeToRepositoryMidMap
                        .get(type);
                if (localMidType != null) {
                    return localMidType;
                }
            }
        }
        return null;
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            final String metadataIdentificationString, final JavaType aspectName,
            final PhysicalTypeMetadata governorPhysicalType, final String itdFilename) {

        final JavaType domainType;
        if (MemberFindingUtils.getAnnotationOfType(governorPhysicalType
                .getMemberHoldingTypeDetails().getAnnotations(),
                ROO_REPOSITORY_MONGO) != null) {
            final RepositoryMongoAnnotationValues repositoryMongoAnnotationValues =
                    new RepositoryMongoAnnotationValues(governorPhysicalType);
            domainType = repositoryMongoAnnotationValues.getDomainType();
        } else {
            final RepositoryJpaAnnotationValues repositoryJpaAnnotationValues =
                    new RepositoryJpaAnnotationValues(governorPhysicalType);
            domainType = repositoryJpaAnnotationValues.getDomainType();
        }

        // Remember that this entity JavaType matches up with this metadata
        // identification string
        // Start by clearing any previous association
        final JavaType oldEntity = repositoryMidToDomainTypeMap
                .get(metadataIdentificationString);
        if (oldEntity != null) {
            domainTypeToRepositoryMidMap.remove(oldEntity);
        }
        domainTypeToRepositoryMidMap.put(domainType,
                metadataIdentificationString);
        repositoryMidToDomainTypeMap.put(metadataIdentificationString,
                domainType);

        ClassOrInterfaceTypeDetails entity = typeLocationService.getTypeDetails(domainType);
        FieldMetadata parentProperty = null;
        final String parentPropertyName = RequestFactoryUtils.getStringAnnotationValue(
                entity, ROO_REQUEST_FACTORY_ENTITY, RooRequestFactoryEntity
                .PARENT_PROPERTY_ATTRIBUTE, "");
        if (!parentPropertyName.isEmpty()) {
            for (FieldMetadata field : entity.getDeclaredFields()) {
                if (field.getFieldName().getSymbolName().equals(parentPropertyName)) {
                    parentProperty = field;
                    break;
                }
            }
            if (parentProperty == null) {
                return null;
            }
        }

        return new RepositoryMetadata(metadataIdentificationString, aspectName,
                governorPhysicalType, parentProperty);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_RequestFactory.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "RequestFactory";
    }

    protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
        JavaType javaType = RepositoryMetadata.getJavaType(metadataIdentificationString);
        LogicalPath path = RepositoryMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return RepositoryMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return RepositoryMetadata.getMetadataIdentiferType();
    }

    //@SuppressWarnings("unchecked")
    private void registerMatchers() {
    }
}