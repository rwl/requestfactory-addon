package org.springframework.roo.addon.requestfactory.android;

import java.io.File;

import org.springframework.roo.addon.requestfactory.RequestFactoryPath;

public class AndroidPaths {

    public static final String SEP = File.separator;

    public static final String SRC_PATH = SEP + "src";

    public static final String ASSET_PATH = SEP + "assets";

    public static final String DRAWABLE_PATH = SEP + "res" + SEP + "drawable";

    public static final String LAYOUT_PATH = SEP + "res" + SEP + "layout";

    public static final String VALUES_PATH = SEP + "res" + SEP + "values";
    public static final String VALUES_LARGE_PATH = SEP + "res" + SEP + "values-large";
    public static final String VALUES_SW600DP_PATH = SEP + "res" + SEP + "values-sw600dp";

    public static final RequestFactoryPath APPLICATION = new RequestFactoryPath("/application", "module/client/application/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath ACTIVITY = new RequestFactoryPath("/activity", "module/client/activity/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath FRAGMENT = new RequestFactoryPath("/fragment", "module/client/fragment/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath ADAPTER = new RequestFactoryPath("/adapter", "module/client/adapter/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath PROCESSOR = new RequestFactoryPath("/processor", "module/client/processor/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath ASSET = new RequestFactoryPath(ASSET_PATH, "module/client/assets/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath DRAWABLE = new RequestFactoryPath(DRAWABLE_PATH, "module/client/drawable/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath LAYOUT = new RequestFactoryPath(LAYOUT_PATH, "module/client/layout/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath[] ALL_PATHS = new RequestFactoryPath[] {
        APPLICATION, ACTIVITY, FRAGMENT, ADAPTER, PROCESSOR, DRAWABLE, ASSET, LAYOUT
    };

    private AndroidPaths() {
    }
}
