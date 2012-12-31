package org.springframework.roo.addon.requestfactory.android.project;

import org.springframework.roo.addon.requestfactory.android.types.Dimension;
import org.springframework.roo.addon.requestfactory.android.types.Orientation;
import org.springframework.roo.addon.requestfactory.android.types.Permission;
import org.springframework.roo.addon.requestfactory.android.types.SystemService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Interface of operations this add-on offers.
 */
public interface AndroidProjectOperations {

    boolean isActivityAvailable();
    boolean isViewAvailable();

    void activity(JavaType type, String layout, boolean main,
            boolean noTitle, boolean fullscreen);
    void view(JavaType type, String view, String identifier,
            JavaSymbolName fieldName, Dimension height,
            Dimension width);
    void resourceString(JavaType type, String name, JavaSymbolName fieldName,
            String value);
    void layout(String name, Dimension height, Dimension width,
            Orientation orientation);
    void systemService(JavaType typeName, JavaSymbolName fieldName,
            SystemService value, boolean addPermissions);
    void permission(Permission name);
    void fragment(JavaType name, String layout, boolean support);
    void dependency(AndroidProjectDependency name);
}