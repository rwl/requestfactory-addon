package org.springframework.roo.addon.requestfactory.entity;

import org.springframework.roo.addon.requestfactory.annotations.entity.RooExclude;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooHelpText;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooInvisible;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooPassword;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooReadOnly;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooRequestFactoryEntity;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooRequestFactoryRepository;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooRequestFactoryService;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooTextArea;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooUneditable;
import org.springframework.roo.addon.requestfactory.annotations.entity.RooUnits;
import org.springframework.roo.model.JavaType;

public final class EntityJavaType {

    public static final JavaType KEY = new JavaType(
            "com.google.appengine.api.datastore.Key");
    public static final JavaType KEY_FACTORY = new JavaType(
            "com.google.appengine.api.datastore.KeyFactory");
    public static final JavaType UNOWNED = new JavaType(
            "com.google.appengine.datanucleus.annotations.Unowned");

    public static final JavaType ROO_REQUEST_FACTORY_ENTITY = new JavaType(
            RooRequestFactoryEntity.class);
    public static final JavaType ROO_REQUEST_FACTORY_REPOSITORY = new JavaType(
            RooRequestFactoryRepository.class);
    public static final JavaType ROO_REQUEST_FACTORY_SERVICE = new JavaType(
            RooRequestFactoryService.class);
    
    public static final JavaType EXCLUDE = new JavaType(
            RooExclude.class);
    public static final JavaType READ_ONLY = new JavaType(
            RooReadOnly.class);
    public static final JavaType INVISIBLE = new JavaType(
            RooInvisible.class);
    public static final JavaType UNEDITABLE = new JavaType(
            RooUneditable.class);
    public static final JavaType HELP_TEXT = new JavaType(
            RooHelpText.class);
    public static final JavaType TEXT_AREA = new JavaType(
            RooTextArea.class);
    public static final JavaType PASSWORD = new JavaType(
            RooPassword.class);
    public static final JavaType UNITS = new JavaType(
            RooUnits.class);

    /**
     * Constructor is private to prevent instantiation
     */
    private EntityJavaType() {
    }
}
