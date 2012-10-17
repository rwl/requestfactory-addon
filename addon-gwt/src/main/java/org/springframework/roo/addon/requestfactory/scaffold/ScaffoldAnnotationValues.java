package org.springframework.roo.addon.requestfactory.scaffold;

import static org.springframework.roo.addon.requestfactory.scaffold.RooRequestFactory.PARENT_PROPERTY_DEFAULT;
import static org.springframework.roo.addon.requestfactory.scaffold.ScaffoldJavaType.ROO_REQUEST_FACTORY;

import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;

/**
 * The values of a parsed {@link RooRequestFactory} annotation.
 */
public class ScaffoldAnnotationValues extends AbstractAnnotationValues {

    @AutoPopulate private String parent = PARENT_PROPERTY_DEFAULT;

    /**
     * Constructor
     *
     * @param annotatedType
     */
    public ScaffoldAnnotationValues(final MemberHoldingTypeDetailsMetadataItem<?> annotatedType) {
        super(annotatedType, ROO_REQUEST_FACTORY);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getParentProperty() {
        return parent;
    }
}
