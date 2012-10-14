package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import org.springframework.roo.model.JavaType;

public final class GwtBootstrapJavaType {

    public static final JavaType KEY = new JavaType(
            "com.google.appengine.api.datastore.Key");
    public static final JavaType KEY_FACTORY = new JavaType(
            "com.google.appengine.api.datastore.KeyFactory");

    public static final JavaType ROO_GWT_BOOTSTRAP = new JavaType(
            RooGwtBootstrap.class.getName());
    public static final JavaType ROO_GWT_BOOTSTRAP_EXCLUDE = new JavaType(
            RooGwtBootstrapExclude.class.getName());

    /**
     * Constructor is private to prevent instantiation
     */
    private GwtBootstrapJavaType() {
    }
}
