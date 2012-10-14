package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import static org.springframework.roo.addon.requestfactory.gwt.bootstrap.GwtBootstrapJavaType.ROO_GWT_BOOTSTRAP;
import static org.springframework.roo.addon.requestfactory.gwt.bootstrap.RooGwtBootstrap.PARENT_PROPERTY_DEFAULT;

import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;

/**
 * The values of a parsed {@link RooGwtBootstrap} annotation.
 */
public class GwtBootstrapAnnotationValues extends AbstractAnnotationValues {

    @AutoPopulate private String parentProperty = PARENT_PROPERTY_DEFAULT;

    /**
     * Constructor
     *
     * @param annotatedType
     */
    public GwtBootstrapAnnotationValues(final MemberHoldingTypeDetailsMetadataItem<?> annotatedType) {
        super(annotatedType, ROO_GWT_BOOTSTRAP);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getParentProperty() {
        return parentProperty;
    }
}
