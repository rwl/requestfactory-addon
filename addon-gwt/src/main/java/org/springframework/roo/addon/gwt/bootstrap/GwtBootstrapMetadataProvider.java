package org.springframework.roo.addon.gwt.bootstrap;

import org.springframework.roo.classpath.itd.ItdTriggerBasedMetadataProvider;
import org.springframework.roo.model.JavaType;

public interface GwtBootstrapMetadataProvider extends ItdTriggerBasedMetadataProvider {

    /**
     * Returns the values of the Gwt Bootstrap-related annotation on the given Java type
     * (if any).
     *
     * @param javaType can be <code>null</code>
     * @return <code>null</code> if no values can be found
     */
    GwtBootstrapAnnotationValues getAnnotationValues(JavaType javaType);
}