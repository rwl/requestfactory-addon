package org.springframework.roo.addon.requestfactory.android.project;

public enum AndroidProjectDependency {
    ACTION_BAR_SHERLOCK ("abs"),
    ENDLESS ("endless"),
    SUPPORT_LIBRARY ("support"),
    REQUEST_FACTORY_ANNOTATIONS ("annotations");
    
    private final String tag;
    
    private AndroidProjectDependency(final String tag) {
        this.tag = tag;
    }
    
    public String getTag() {
        return tag;
    }
}
