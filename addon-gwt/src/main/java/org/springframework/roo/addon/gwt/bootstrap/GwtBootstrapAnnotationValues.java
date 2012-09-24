package org.springframework.roo.addon.gwt.bootstrap;

import static org.springframework.roo.addon.gwt.bootstrap.GwtBootstrapJavaType.ROO_GWT_BOOTSTRAP;
import static org.springframework.roo.addon.gwt.bootstrap.RooGwtBootstrap.PARENT_FIELD_DEFAULT;

import org.apache.commons.lang3.StringUtils;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;

/**
 * The values of a parsed {@link RooGwtBootstrap} annotation.
 */
public class GwtBootstrapAnnotationValues extends AbstractAnnotationValues {

    @AutoPopulate private String parentField = PARENT_FIELD_DEFAULT;

    /**
     * Constructor
     *
     * @param annotatedType
     */
    public GwtBootstrapAnnotationValues(final MemberHoldingTypeDetailsMetadataItem<?> annotatedType) {
        super(annotatedType, ROO_GWT_BOOTSTRAP);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getParentField() {
        return StringUtils.defaultIfEmpty(parentField, PARENT_FIELD_DEFAULT);
    }
}
