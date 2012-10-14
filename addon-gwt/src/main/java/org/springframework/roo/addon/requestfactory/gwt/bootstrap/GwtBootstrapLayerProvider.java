package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.ImportMetadataBuilder;
import org.springframework.roo.classpath.layers.CoreLayerProvider;
import org.springframework.roo.classpath.layers.LayerType;
import org.springframework.roo.classpath.layers.MemberTypeAdditions;
import org.springframework.roo.classpath.layers.MethodParameter;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.support.util.PairList;

/**
 * The {@link org.springframework.roo.classpath.layers.LayerProvider} for the
 * {@link LayerType#ACTIVE_RECORD} layer.
 *
 * @author Stefan Schmidt
 * @author Andrew Swan
 * @author Richard Lincoln
 */
@Component
@Service
public class GwtBootstrapLayerProvider extends CoreLayerProvider {

    @Reference private GwtBootstrapMetadataProvider gwtBootstrapMetadataProvider;
    @Reference private MetadataService metadataService;
    @Reference TypeLocationService typeLocationService;

//    @Reference protected MemberDetailsScanner memberDetailsScanner;

    public int getLayerPosition() {
        //return LayerType.ACTIVE_RECORD.getPosition();
        return LayerType.DAO.getPosition();
    }

    /*@Override
    public int getPriority() {
        return 1;
    }*/

    @Override
    public MemberTypeAdditions getMemberTypeAdditions(final String callerMID,
            final String methodIdentifier, final JavaType targetEntity,
            final JavaType idType, final MethodParameter... callerParameters) {
        Validate.isTrue(StringUtils.isNotBlank(callerMID),
                "Metadata identifier required");
        Validate.notBlank(methodIdentifier, "Method identifier required");
        Validate.notNull(targetEntity, "Target enitity type required");

        // Get the values of this entity's @RooGwtBootstrap annotation
        final GwtBootstrapAnnotationValues annotationValues = gwtBootstrapMetadataProvider
                .getAnnotationValues(targetEntity);
        if (annotationValues == null) {
            return null;
        }

        // Check the entity has a plural form
        final String plural = getPlural(targetEntity);
        if (StringUtils.isBlank(plural)) {
            return null;
        }

        // Look for an entity layer method with this ID and types of parameter
        final List<JavaType> parameterTypes = new PairList<JavaType, JavaSymbolName>(
                callerParameters).getKeys();
        final GwtBootstrapLayerMethod method = GwtBootstrapLayerMethod.valueOf(
                methodIdentifier, parameterTypes, targetEntity, idType);
        if (method == null) {
            return null;
        }

        final FieldMetadata parentProperty = null;//getParentField(targetEntity, annotationValues.getParentField());
        /*if (parentField == null) {
            return null;
        }*/

        // It's an entity layer method; see if it's specified by the annotation
        final String methodName = method.getName(annotationValues,
                targetEntity, plural, parentProperty);
        if (StringUtils.isBlank(methodName)) {
            return null;
        }

        // We have everything needed to generate a method call
        final List<MethodParameter> callerParameterList = Arrays
                .asList(callerParameters);
        final String methodCall = method.getCall(annotationValues,
                targetEntity, plural, parentProperty, callerParameterList);
        final ClassOrInterfaceTypeDetailsBuilder additionsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                callerMID);
        if (method.isStatic()) {
            additionsBuilder.add(ImportMetadataBuilder.getImport(callerMID,
                    targetEntity));
        }
        return new MemberTypeAdditions(additionsBuilder, methodName,
                methodCall, method.isStatic(),
                method.getParameters(callerParameterList));
    }

    /*private FieldMetadata getParentField(JavaType targetEntity, String parentFieldName) {

        final String physicalTypeIdentifier = typeLocationService.getPhysicalTypeIdentifier(targetEntity);
        if (physicalTypeIdentifier == null) {
            return null;
        }
        // We need to lookup the metadata we depend on
        final PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService.get(physicalTypeIdentifier);


        // We need to abort if we couldn't find dependent metadata
        if (physicalTypeMetadata == null || !physicalTypeMetadata.isValid()) {
            return null;
        }

        final ClassOrInterfaceTypeDetails cid = physicalTypeMetadata.getMemberHoldingTypeDetails();
        if (cid == null) {
            // Abort if the type's class details aren't available (parse error
            // etc)
            return null;
        }


        final MemberDetails memberDetails = memberDetailsScanner.getMemberDetails(getClass().getName(), cid);
        if (memberDetails == null) {
            return null;
        }
        FieldMetadata parentField = null;
        if (!parentFieldName.isEmpty()) {
            for (FieldMetadata field : memberDetails.getFields()) {
                if (field.getFieldName().getSymbolName().equals(parentFieldName)) {
                    parentField = field;
                    break;
                }
            }
            if (parentField == null) {
                return null;
            }
        }
        return parentField;
    }*/

    /**
     * Returns the plural form of the given entity
     *
     * @param javaType the entity for which to get the plural (required)
     * @return <code>null</code> if it can't be found or is actually
     *         <code>null</code>
     */
    private String getPlural(final JavaType javaType) {
        final String key = PluralMetadata.createIdentifier(javaType,
                typeLocationService.getTypePath(javaType));
        final PluralMetadata pluralMetadata = (PluralMetadata) metadataService
                .get(key);
        if (pluralMetadata == null) {
            // Can't acquire the plural
            return null;
        }
        return pluralMetadata.getPlural();
    }

    /**
     * For use by unit tests
     *
     * @param gwtBootstrapMetadataProvider
     */
    void setJpaActiveRecordMetadataProvider(
            final GwtBootstrapMetadataProviderImpl gwtBootstrapMetadataProvider) {
        this.gwtBootstrapMetadataProvider = gwtBootstrapMetadataProvider;
    }

    /**
     * For use by unit tests
     *
     * @param metadataService
     */
    void setMetadataService(final MetadataService metadataService) {
        this.metadataService = metadataService;
    }
}
