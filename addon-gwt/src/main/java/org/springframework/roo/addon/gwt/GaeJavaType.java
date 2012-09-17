package org.springframework.roo.addon.gwt;

import org.springframework.roo.model.JavaType;

public final class GaeJavaType {

    public static final JavaType KEY = new JavaType(
            "com.google.appengine.api.datastore.Key");
    public static final JavaType KEY_FACTORY = new JavaType(
            "com.google.appengine.api.datastore.KeyFactory");

    /**
     * Constructor is private to prevent instantiation
     */
    private GaeJavaType() {
    }
}
