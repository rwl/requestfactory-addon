package org.springframework.roo.addon.requestfactory.android;

import java.io.File;

import org.springframework.roo.addon.requestfactory.RequestFactoryPath;

public class AndroidPaths {

    private static final String SEP = File.separator;

    public static final String RES_LAYOUT = SEP + "res" + SEP + "layout";

    public static final RequestFactoryPath ACTIVITY = new RequestFactoryPath("/activity", "module/client/activity/" + RequestFactoryPath.templateSelector);
//    public static final RequestFactoryPath RES_LAYOUT = new RequestFactoryPath("/res/layout", "module/layout/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath[] ALL_PATHS = new RequestFactoryPath[] {
        ACTIVITY
    };

    private AndroidPaths() {
    }
}
