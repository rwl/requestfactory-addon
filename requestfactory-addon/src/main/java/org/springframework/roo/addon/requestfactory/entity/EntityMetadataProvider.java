package org.springframework.roo.addon.requestfactory.entity;

import org.springframework.roo.classpath.itd.ItdTriggerBasedMetadataProvider;
import org.springframework.roo.model.JavaType;

public interface EntityMetadataProvider extends ItdTriggerBasedMetadataProvider {

    /**
     * Returns the values of the Gwt Bootstrap-related annotation on the given Java type
     * (if any).
     *
     * @param javaType can be <code>null</code>
     * @return <code>null</code> if no values can be found
     */
    EntityAnnotationValues getAnnotationValues(JavaType javaType);
}