package org.springframework.roo.addon.requestfactory.entity;

import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY_ENTITY;
import static org.springframework.roo.model.SpringJavaType.AUTOWIRED;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.layers.service.ServiceAnnotationValues;
import org.springframework.roo.addon.layers.service.ServiceAnnotationValuesFactory;
import org.springframework.roo.addon.layers.service.ServiceInterfaceLocator;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooRequestFactoryEntity;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.layers.CoreLayerProvider;
import org.springframework.roo.classpath.layers.LayerType;
import org.springframework.roo.classpath.layers.MemberTypeAdditions;
import org.springframework.roo.classpath.layers.MethodParameter;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.support.util.PairList;

/**
 * The {@link org.springframework.roo.classpath.layers.LayerProvider} that
 * provides an application's service layer.
 *
 * @author Stefan Schmidt
 * @author Andrew Swan
 * @since 1.2.0
 */
@Component
@Service
public class ServiceLayerProvider extends CoreLayerProvider {

    @Reference private MetadataService metadataService;
    @Reference private ServiceAnnotationValuesFactory serviceAnnotationValuesFactory;
    @Reference private ServiceInterfaceLocator serviceInterfaceLocator;
    @Reference TypeLocationService typeLocationService;

    public int getLayerPosition() {
        return LayerType.SERVICE.getPosition() + 1;
    }

    public MemberTypeAdditions getMemberTypeAdditions(final String callerMID,
            final String methodIdentifier, final JavaType targetEntity,
            final JavaType idType, final MethodParameter... methodParameters) {
        Validate.isTrue(StringUtils.isNotBlank(callerMID),
                "Caller's metadata identifier required");
        Validate.notNull(methodIdentifier, "Method identifier required");
        Validate.notNull(targetEntity, "Target entity type required");
        Validate.notNull(methodParameters,
                "Method param names and types required (may be empty)");

        // Check whether this is even a known service layer method
        final List<JavaType> parameterTypes = new PairList<JavaType, JavaSymbolName>(
                methodParameters).getKeys();
        final ServiceLayerMethod method = ServiceLayerMethod.valueOf(
                methodIdentifier, parameterTypes, targetEntity, idType);
        if (method == null) {
            return null;
        }

        // Check the entity has a plural form
        final String pluralId = PluralMetadata.createIdentifier(targetEntity,
                typeLocationService.getTypePath(targetEntity));
        final PluralMetadata pluralMetadata = (PluralMetadata) metadataService
                .get(pluralId);
        if (pluralMetadata == null || pluralMetadata.getPlural() == null) {
            return null;
        }

        // Loop through the service interfaces that claim to support the given
        // target entity
        for (final ClassOrInterfaceTypeDetails serviceInterface : serviceInterfaceLocator
                .getServiceInterfaces(targetEntity)) {
            // Get the values of the @RooService annotation for this service
            // interface
            final ServiceAnnotationValues annotationValues = serviceAnnotationValuesFactory
                    .getInstance(serviceInterface);
            final JavaType[] domainTypes = annotationValues.getDomainTypes();

            for (final JavaType domainType : domainTypes) {
                final ClassOrInterfaceTypeDetails domainTypeDetails =
                        typeLocationService.getTypeDetails(domainType);
                if (domainTypeDetails == null) {
                    continue;
                }

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

                if (annotationValues != null) {
                    // Check whether this method is implemented by the given service
                    final String methodName = method.getName(annotationValues,
                            parentField,
                            targetEntity, pluralMetadata.getPlural());
                    if (StringUtils.isNotBlank(methodName)) {
                        // The service implements the method; get the additions to
                        // be made by the caller
                        final MemberTypeAdditions methodAdditions = getMethodAdditions(
                                callerMID, methodName, serviceInterface.getName(),
                                Arrays.asList(methodParameters));

                        // Return these additions
                        return methodAdditions;
                    }
                }
            }
        }
        // None of the services for this entity were able to provide the method
        return null;
    }

    /**
     * Returns the additions the caller should make in order to invoke the given
     * method for the given domain entity.
     *
     * @param callerMID the caller's metadata ID (required)
     * @param methodName the name of the method being invoked (required)
     * @param serviceInterface the domain service type (required)
     * @param parameterNames the names of the parameters being passed by the
     *            caller to the method
     * @return a non-<code>null</code> set of additions
     */
    private MemberTypeAdditions getMethodAdditions(final String callerMID,
            final String methodName, final JavaType serviceInterface,
            final List<MethodParameter> parameters) {
        // The method is supported by this service interface; make a builder
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                callerMID);

        // Add an autowired field of the type of this service
        final String fieldName = StringUtils.uncapitalize(serviceInterface
                .getSimpleTypeName());
        cidBuilder.addField(new FieldMetadataBuilder(callerMID, 0, Arrays
                .asList(new AnnotationMetadataBuilder(AUTOWIRED)),
                new JavaSymbolName(fieldName), serviceInterface));

        // Generate an additions object that includes a call to the method
        return MemberTypeAdditions.getInstance(cidBuilder, fieldName,
                methodName, false, parameters);
    }

    // -------------------- Setters for use by unit tests ----------------------

    void setMetadataService(final MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    void setServiceAnnotationValuesFactory(
            final ServiceAnnotationValuesFactory serviceAnnotationValuesFactory) {
        this.serviceAnnotationValuesFactory = serviceAnnotationValuesFactory;
    }

    void setServiceInterfaceLocator(
            final ServiceInterfaceLocator serviceInterfaceLocator) {
        this.serviceInterfaceLocator = serviceInterfaceLocator;
    }
}
