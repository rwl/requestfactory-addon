package org.springframework.roo.addon.requestfactory.android.types;

public enum Orientation {
    VERTICAL ("vertical"),
    HORIZONTAL ("horizontal");
    
    private final String value;
    
    private Orientation(final String value) {
        this.value = value;
    }
    
    public String value() {
        return value;
    }
}
