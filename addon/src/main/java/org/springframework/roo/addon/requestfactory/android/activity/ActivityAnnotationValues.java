package org.springframework.roo.addon.requestfactory.android.activity;

import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ACTIVITY;

import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;


public class ActivityAnnotationValues extends AbstractAnnotationValues {

    @AutoPopulate private String value = "";

    public ActivityAnnotationValues(final MemberHoldingTypeDetailsMetadataItem<?> annotatedType) {
        super(annotatedType, ROO_ACTIVITY);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getLayout() {
        return value;
    }
}
