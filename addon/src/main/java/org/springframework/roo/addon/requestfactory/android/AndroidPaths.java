package org.springframework.roo.addon.requestfactory.android;

import java.io.File;

import org.springframework.roo.addon.requestfactory.RequestFactoryPath;

public class AndroidPaths {

    public static final String SEP = File.separator;

    public static final String LAYOUT_PATH = SEP + "res" + SEP + "layout";
    public static final String VALUES_PATH = SEP + "res" + SEP + "values";

    public static final RequestFactoryPath ACTIVITY = new RequestFactoryPath("/activity", "module/client/activity/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath LAYOUT = new RequestFactoryPath(LAYOUT_PATH, "module/client/layout/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath[] ALL_PATHS = new RequestFactoryPath[] {
        ACTIVITY, LAYOUT
    };

    private AndroidPaths() {
    }
}
