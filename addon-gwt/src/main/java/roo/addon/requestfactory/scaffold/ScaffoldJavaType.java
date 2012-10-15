package roo.addon.requestfactory.scaffold;

import org.springframework.roo.model.JavaType;

public final class ScaffoldJavaType {

    public static final JavaType KEY = new JavaType(
            "com.google.appengine.api.datastore.Key");
    public static final JavaType KEY_FACTORY = new JavaType(
            "com.google.appengine.api.datastore.KeyFactory");

    public static final JavaType ROO_GWT_BOOTSTRAP = new JavaType(
            RooRequestFactory.class.getName());
    public static final JavaType ROO_GWT_BOOTSTRAP_EXCLUDE = new JavaType(
            RooRequestFactoryExclude.class.getName());

    /**
     * Constructor is private to prevent instantiation
     */
    private ScaffoldJavaType() {
    }
}
