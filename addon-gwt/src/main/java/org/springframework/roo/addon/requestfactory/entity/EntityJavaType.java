package org.springframework.roo.addon.requestfactory.entity;

import org.springframework.roo.model.JavaType;

public final class EntityJavaType {

    public static final JavaType KEY = new JavaType(
            "com.google.appengine.api.datastore.Key");
    public static final JavaType KEY_FACTORY = new JavaType(
            "com.google.appengine.api.datastore.KeyFactory");
    public static final JavaType UNOWNED = new JavaType(
            "com.google.appengine.datanucleus.annotations.Unowned");

    public static final JavaType TEXT_TYPE = new JavaType(
            TextType.class.getName());

    public static final JavaType ROO_REQUEST_FACTORY = new JavaType(
            RooRequestFactory.class.getName());
    public static final JavaType EXCLUDE = new JavaType(
            RooExclude.class.getName());
    public static final JavaType READ_ONLY = new JavaType(
            RooReadOnly.class.getName());
    public static final JavaType INVISIBLE = new JavaType(
            RooInvisible.class.getName());
    public static final JavaType UNEDITABLE = new JavaType(
            RooUneditable.class.getName());
    public static final JavaType HELP_TEXT = new JavaType(
            RooHelpText.class.getName());
    public static final JavaType TEXT_AREA = new JavaType(
            RooTextArea.class.getName());
    public static final JavaType PASSWORD = new JavaType(
            RooPassword.class.getName());

    /**
     * Constructor is private to prevent instantiation
     */
    private EntityJavaType() {
    }
}
