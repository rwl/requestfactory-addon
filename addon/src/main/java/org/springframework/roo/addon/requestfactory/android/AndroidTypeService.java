package org.springframework.roo.addon.requestfactory.android;

import java.util.Collection;

public interface AndroidTypeService {
    
    void addActvity(final String moduleName, final String activityName,
            final boolean mainActivity);
    
    String getAndroidManifestXml(final String moduleName);

    void setApplicationName(String moduleName, String applicationName);

    void addPermission(String moduleName, String permissionName);

    void addDependencies(String moduleName,
            Collection<? extends AndroidDependency> newDependencies);
}
