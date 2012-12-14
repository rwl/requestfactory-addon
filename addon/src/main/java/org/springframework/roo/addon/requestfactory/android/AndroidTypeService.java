package org.springframework.roo.addon.requestfactory.android;

public interface AndroidTypeService {
    
    void addActvity(final String moduleName, final String activityName,
            final boolean mainActivity);
    
    String getAndroidManifestXml(final String moduleName);

    void setApplicationName(String moduleName, String applicationName);
}
