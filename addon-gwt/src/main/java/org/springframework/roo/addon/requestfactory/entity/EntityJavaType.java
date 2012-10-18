package org.springframework.roo.addon.requestfactory.entity;

import org.springframework.roo.model.JavaType;

public final class EntityJavaType {

    public static final JavaType KEY = new JavaType(
            "com.google.appengine.api.datastore.Key");
    public static final JavaType KEY_FACTORY = new JavaType(
            "com.google.appengine.api.datastore.KeyFactory");

    public static final JavaType ROO_REQUEST_FACTORY = new JavaType(
            RooRequestFactory.class.getName());
    public static final JavaType ROO_REQUEST_FACTORY_EXCLUDE = new JavaType(
            RooRequestFactoryExclude.class.getName());

    /**
     * Constructor is private to prevent instantiation
     */
    private EntityJavaType() {
    }
}
