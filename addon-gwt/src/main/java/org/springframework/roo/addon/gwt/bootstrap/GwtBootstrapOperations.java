package org.springframework.roo.addon.gwt.bootstrap;

import org.springframework.roo.classpath.operations.Cardinality;
import org.springframework.roo.classpath.operations.Fetch;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Interface of operations this add-on offers. Typically used by a
 * command type or an external add-on.
 *
 * @since 1.1
 */
public interface GwtBootstrapOperations {

    /**
     * Indicate commands should be available
     *
     * @return true if it should be available, otherwise false
     */
    boolean isCommandAvailable();

    /**
     * Annotate the provided Java type with the trigger of this add-on
     */
    void annotateType(JavaType type, final JavaSymbolName parentProperty,
            final JavaSymbolName primaryProperty,
            final JavaSymbolName secondaryProperty);

    void addFieldListJpa(JavaSymbolName fieldName, JavaType fieldType,
            JavaType typeName, JavaSymbolName mappedBy, boolean notNull,
            boolean nullRequired, Integer sizeMin, Integer sizeMax,
            Cardinality cardinality, Fetch fetch, String comment,
            boolean transientModifier, boolean permitReservedWords);
}