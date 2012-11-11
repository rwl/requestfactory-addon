package org.springframework.roo.addon.requestfactory.entity;

import static org.springframework.roo.addon.requestfactory.annotations.entity.RooRequestFactory.PARENT_PROPERTY_DEFAULT;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.ROO_REQUEST_FACTORY;

import org.springframework.roo.addon.requestfactory.annotations.entity.RooRequestFactory;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;

/**
 * The values of a parsed {@link RooRequestFactory} annotation.
 */
public class EntityAnnotationValues extends AbstractAnnotationValues {

    @AutoPopulate private String parent = PARENT_PROPERTY_DEFAULT;

    /**
     * Constructor
     *
     * @param annotatedType
     */
    public EntityAnnotationValues(final MemberHoldingTypeDetailsMetadataItem<?> annotatedType) {
        super(annotatedType, ROO_REQUEST_FACTORY);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getParentProperty() {
        return parent;
    }
}
