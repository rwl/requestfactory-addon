package org.springframework.roo.addon.requestfactory.visualize;

import static org.springframework.roo.addon.requestfactory.annotations.visualize.RooMapMarker.LAT_FIELD_DEFAULT;
import static org.springframework.roo.addon.requestfactory.annotations.visualize.RooMapMarker.LON_FIELD_DEFAULT;
import static org.springframework.roo.addon.requestfactory.visualize.VisualizeJavaType.ROO_MAP_MARKER;

import org.springframework.roo.addon.requestfactory.annotations.visualize.RooMapMarker;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;

/**
 * The values of a parsed {@link RooMapMarker} annotation.
 */
public class MapMarkerAnnotationValues extends AbstractAnnotationValues {

    @AutoPopulate private String lat = LAT_FIELD_DEFAULT;
    @AutoPopulate private String lon = LON_FIELD_DEFAULT;

    public MapMarkerAnnotationValues(
            final MemberHoldingTypeDetailsMetadataItem<?> annotatedType) {
        super(annotatedType, ROO_MAP_MARKER);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }
}
