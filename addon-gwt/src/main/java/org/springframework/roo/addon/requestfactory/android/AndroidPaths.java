package org.springframework.roo.addon.requestfactory.android;

import org.springframework.roo.addon.requestfactory.RequestFactoryPath;

public class AndroidPaths {

    public static final RequestFactoryPath MANAGED_ACTIVITY = new RequestFactoryPath("/client/managed/activity", "module/client/managed/activity/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD

    public static final RequestFactoryPath[] ALL_PATHS = new RequestFactoryPath[] {
        MANAGED_ACTIVITY
    };

    private AndroidPaths() {
    }
}
