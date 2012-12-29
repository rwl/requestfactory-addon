package org.springframework.roo.addon.requestfactory.android.project.metadata;

import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_FRAGMENT;

import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;


public class FragmentAnnotationValues extends AbstractAnnotationValues {

    @AutoPopulate private String value = "";

    public FragmentAnnotationValues(final MemberHoldingTypeDetailsMetadataItem<?> annotatedType) {
        super(annotatedType, ROO_FRAGMENT);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getLayout() {
        return value;
    }
}
