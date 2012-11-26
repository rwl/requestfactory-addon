package org.springframework.roo.addon.requestfactory.annotations.visualize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RooMapMarker {

    String LAT_FIELD_ATTRIBUTE = "lat";
    String LON_FIELD_ATTRIBUTE = "lon";
    
    String LAT_FIELD_DEFAULT = "lat";
    String LON_FIELD_DEFAULT = "lon";

    String lat() default LAT_FIELD_DEFAULT;
    String lon() default LON_FIELD_DEFAULT;
}
