package org.springframework.roo.addon.requestfactory.android;

import org.springframework.roo.addon.requestfactory.annotations.android.RooActivity;
import org.springframework.roo.addon.requestfactory.annotations.android.RooRequestFactoryAndroid;
import org.springframework.roo.addon.requestfactory.annotations.android.RooResourceString;
import org.springframework.roo.addon.requestfactory.annotations.android.RooView;
import org.springframework.roo.model.JavaType;

public final class AndroidJavaType {

    public static final JavaType ROO_ANDROID_SCAFFOLD = new JavaType(
            RooRequestFactoryAndroid.class);
    
    public static final JavaType ROO_ACTIVITY = new JavaType(
            RooActivity.class);
    public static final JavaType ROO_VIEW = new JavaType(
            RooView.class);
    public static final JavaType ROO_RESOURCE_STRING = new JavaType(
            RooResourceString.class);

    public static final JavaType ANDROID_ACTIVITY = new JavaType(
            "android.app.Activity");
    public static final JavaType ANDROID_BUNDLE = new JavaType(
            "android.os.Bundle");

    /**
     * Constructor is private to prevent instantiation
     */
    private AndroidJavaType() {
    }
}
