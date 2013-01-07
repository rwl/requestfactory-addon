package org.springframework.roo.addon.requestfactory.rest;

import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

public interface RestProjectOperations extends Feature {
    
    String FEATURE_NAME = "rest";

    boolean isSetupAvailable();

    boolean isRestResourceAvailable();

    void setup();

    void restResource(JavaType name, boolean hide, String path, String rel);
}
