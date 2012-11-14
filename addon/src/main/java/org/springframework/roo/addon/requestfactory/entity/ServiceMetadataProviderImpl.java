package org.springframework.roo.addon.requestfactory.entity;

import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY_ENTITY;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY_SERVICE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.layers.service.ServiceAnnotationValues;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooRequestFactoryEntity;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.customdata.taggers.CustomDataKeyDecorator;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.itd.AbstractMemberDiscoveringItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Provides {@link ExampleMetadata}.
 */
@Component(immediate = true)
@Service
public final class ServiceMetadataProviderImpl extends AbstractMemberDiscoveringItdMetadataProvider
        implements ServiceMetadataProvider {

    @Reference private CustomDataKeyDecorator customDataKeyDecorator;
    private final Map<JavaType, String> domainTypeToServiceMidMap = new LinkedHashMap<JavaType, String>();
    private final Map<String, JavaType> serviceMidToDomainTypeMap = new LinkedHashMap<String, JavaType>();

    /**
     * The activate method for this OSGi component.
     *
     * @param context the component context can be used to get access to the
     * OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        super.setDependsOnGovernorBeingAClass(false);
        metadataDependencyRegistry.addNotificationListener(this);
        metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier
                .getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(ROO_REQUEST_FACTORY_SERVICE);
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
        metadataDependencyRegistry.deregisterDependency(PhysicalTypeIdentifier
                .getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(ROO_REQUEST_FACTORY_SERVICE);
        customDataKeyDecorator.unregisterMatchers(getClass());
    }

    @Override
    protected String getLocalMidToRequest(final ItdTypeDetails itdTypeDetails) {
        // Determine the governor for this ITD, and whether any metadata is even
        // hoping to hear about changes to that JavaType and its ITDs
        final JavaType governor = itdTypeDetails.getName();
        final String localMid = domainTypeToServiceMidMap.get(governor);
        if (localMid != null) {
            return localMid;
        }

        final MemberHoldingTypeDetails memberHoldingTypeDetails = typeLocationService
                .getTypeDetails(governor);
        if (memberHoldingTypeDetails != null) {
            for (final JavaType type : memberHoldingTypeDetails
                    .getLayerEntities()) {
                final String localMidType = domainTypeToServiceMidMap
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
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(String metadataIdString,
            JavaType aspectName, PhysicalTypeMetadata governorPhysicalType,
            String itdFilename) {
        final JavaType domainType;
        final ServiceAnnotationValues serviceAnnotationValues =
                    new ServiceAnnotationValues(governorPhysicalType);
        domainType = serviceAnnotationValues.getDomainTypes()[0];

        // Remember that this entity JavaType matches up with this metadata
        // identification string
        // Start by clearing any previous association
        final JavaType oldEntity = serviceMidToDomainTypeMap
                .get(metadataIdString);
        if (oldEntity != null) {
            domainTypeToServiceMidMap.remove(oldEntity);
        }
        domainTypeToServiceMidMap.put(domainType, metadataIdString);
        serviceMidToDomainTypeMap.put(metadataIdString, domainType);

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

        final String pluralId = PluralMetadata.createIdentifier(domainType,
                typeLocationService.getTypePath(domainType));
        PluralMetadata pluralMetadata = (PluralMetadata) metadataService.get(pluralId);
        if (pluralMetadata == null) {
            // Can't acquire the plural
            return null;
        }

        final List<FieldMetadata> idFields = persistenceMemberLocator.getIdentifierFields(domainType);
        if (idFields.size() != 1) {
            // The ID field metadata is either unavailable or not stable yet
            return null;
        }
        final FieldMetadata idField = idFields.get(0);

        // Pass dependencies required by the metadata in through its constructor
        return new ServiceMetadata(metadataIdString, aspectName,
                governorPhysicalType, serviceAnnotationValues, domainType,
                pluralMetadata.getPlural(), idField, parentProperty);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_Roo_RequestFactory.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "RequestFactory";
    }

    protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
        JavaType javaType = ServiceMetadata.getJavaType(metadataIdentificationString);
        LogicalPath path = ServiceMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return ServiceMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return ServiceMetadata.getMetadataIdentiferType();
    }

    //@SuppressWarnings("unchecked")
    private void registerMatchers() {
    }
}