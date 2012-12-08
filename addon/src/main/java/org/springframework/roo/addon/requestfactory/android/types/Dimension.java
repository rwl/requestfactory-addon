package org.springframework.roo.addon.requestfactory.android.types;

public enum Dimension {
    MATCH_PARENT ("match_parent"),
    FILL_PARENT ("fill_parent"),
    WRAP_CONTENT ("wrap_content");
    
    private final String value;
    
    private Dimension(final String value) {
        this.value = value;
    }
    
    public String value() {
        return value;
    }
}
