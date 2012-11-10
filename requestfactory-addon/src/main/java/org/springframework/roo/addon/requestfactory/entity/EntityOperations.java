package org.springframework.roo.addon.requestfactory.entity;

import java.util.Set;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Interface of operations this add-on offers. Typically used by a
 * command type or an external add-on.
 *
 * @since 1.1
 */
public interface EntityOperations {

    /**
     * The delimiter for multi-level paths specified by a "<source path="..." />
     * element in a module's *.gwt.xml file.
     */
    String PATH_DELIMITER = "/";

    boolean isCommandAvailable();

    void annotateType(JavaType type, final JavaSymbolName parentProperty,
            final JavaSymbolName primaryProperty,
            final JavaSymbolName secondaryProperty);

    void annotateTypeWithPlural(JavaType type, final String name);

    void annotateTypeWithToString(JavaType target, Set<String> excludeFields, String methodName);
}