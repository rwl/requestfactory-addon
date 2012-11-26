package org.springframework.roo.addon.requestfactory.visualize;

import org.springframework.roo.addon.requestfactory.annotations.visualize.RooMapMarker;
import org.springframework.roo.model.JavaType;


public final class VisualizeJavaType {

    public static final JavaType ROO_MAP_MARKER = new JavaType(
            RooMapMarker.class);

    /**
     * Constructor is private to prevent instantiation
     */
    private VisualizeJavaType() {
    }
}
