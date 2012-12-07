package org.springframework.roo.addon.requestfactory.android;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Interface of operations this add-on offers.
 */
public interface AndroidProjectOperations {

    boolean isActivityAvailable();
    boolean isViewAvailable();

    void activity(JavaType type, String layout);
    void view(JavaType type, JavaType view, String identifier,
            JavaSymbolName fieldName, Dimension height,
            Dimension width);
    void resourceString(JavaType type, String name, JavaSymbolName fieldName,
            String value, Dimension height, Dimension width);
}