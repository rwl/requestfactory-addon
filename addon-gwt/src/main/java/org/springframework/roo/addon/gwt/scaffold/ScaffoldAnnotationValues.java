package org.springframework.roo.addon.gwt.scaffold;

import static org.springframework.roo.addon.gwt.scaffold.RooRequestFactory.PARENT_PROPERTY_DEFAULT;
import static org.springframework.roo.addon.gwt.scaffold.ScaffoldJavaType.ROO_GWT_BOOTSTRAP;

import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;

/**
 * The values of a parsed {@link RooRequestFactory} annotation.
 */
public class ScaffoldAnnotationValues extends AbstractAnnotationValues {

    @AutoPopulate private String parentProperty = PARENT_PROPERTY_DEFAULT;

    /**
     * Constructor
     *
     * @param annotatedType
     */
    public ScaffoldAnnotationValues(final MemberHoldingTypeDetailsMetadataItem<?> annotatedType) {
        super(annotatedType, ROO_GWT_BOOTSTRAP);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getParentProperty() {
        return parentProperty;
    }
}
