package org.springframework.roo.addon.requestfactory.android;

import org.springframework.roo.addon.requestfactory.annotations.android.RooRequestFactoryAndroid;
import org.springframework.roo.model.JavaType;

public final class AndroidJavaType {

    public static final JavaType ROO_ANDROID_SCAFFOLD = new JavaType(
            RooRequestFactoryAndroid.class);

    /**
     * Constructor is private to prevent instantiation
     */
    private AndroidJavaType() {
    }
}
