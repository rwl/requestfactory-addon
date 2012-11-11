package org.springframework.roo.addon.requestfactory.android;

import java.util.ArrayList;
import java.util.List;

import org.springframework.roo.addon.requestfactory.RequestFactoryPath;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;

public class AndroidType extends RequestFactoryType {

    public static final AndroidType PROXY_ACTIVITY = new AndroidType(AndroidPaths.ACTIVITY, true, "ProxyActivity", "proxyActivity", "ProxyActivity", false, false, true);

    public static final AndroidType[] ALL_TYPES = new AndroidType[] {
        PROXY_ACTIVITY
    };

    public static List<AndroidType> getAndroidMirrorTypes() {
        final List<AndroidType> mirrorTypes = new ArrayList<AndroidType>();
        for (final AndroidType androidType : AndroidType.ALL_TYPES) {
            if (androidType.isMirrorType()) {
                mirrorTypes.add(androidType);
            }
        }
        return mirrorTypes;
    }

    protected final boolean createViewXml;

    public AndroidType(RequestFactoryPath path, boolean mirrorType, String suffix, String name, String template, boolean createAbstract, boolean overwriteConcrete, boolean createViewXml) {
        super(path, mirrorType, suffix, name, template, createAbstract, overwriteConcrete);
        this.createViewXml = createViewXml;
    }

    public boolean isCreateViewXml() {
        return createViewXml;
    }
}
