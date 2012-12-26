package org.springframework.roo.addon.requestfactory.android;

import org.springframework.roo.addon.requestfactory.annotations.android.RooActivity;
import org.springframework.roo.addon.requestfactory.annotations.android.RooOnCreate;
import org.springframework.roo.addon.requestfactory.annotations.android.RooRequestFactoryAndroid;
import org.springframework.roo.addon.requestfactory.annotations.android.RooString;
import org.springframework.roo.addon.requestfactory.annotations.android.RooSystemService;
import org.springframework.roo.addon.requestfactory.annotations.android.RooView;
import org.springframework.roo.model.JavaType;

public final class AndroidJavaType {

    public static final JavaType ROO_ANDROID_SCAFFOLD = new JavaType(
            RooRequestFactoryAndroid.class);
    
    public static final JavaType ROO_ACTIVITY = new JavaType(
            RooActivity.class);
    public static final JavaType ROO_VIEW = new JavaType(
            RooView.class);
    public static final JavaType ROO_STRING = new JavaType(
            RooString.class);
    public static final JavaType ROO_ON_CREATE = new JavaType(
            RooOnCreate.class);
    public static final JavaType ROO_SYSTEM_SERVICE = new JavaType(
            RooSystemService.class);

    public static final JavaType ANDROID_ACTIVITY = new JavaType(
            "android.app.Activity");
    public static final JavaType ANDROID_BUNDLE = new JavaType(
            "android.os.Bundle");
    public static final JavaType ANDROID_RESOURCES = new JavaType(
            "android.content.res.Resources");
    public static final JavaType ANDROID_CONTEXT = new JavaType(
            "android.content.Context");

    /**
     * Constructor is private to prevent instantiation
     */
    private AndroidJavaType() {
    }
}
