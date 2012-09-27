package org.springframework.roo.addon.gwt.bootstrap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RooGwtBootstrap {

    String PARENT_FIELD_ATTRIBUTE = "parentField";
    String PARENT_FIELD_DEFAULT = "";

    /**
     * @return the field name of the parent reference
     */
    String parentField() default PARENT_FIELD_DEFAULT;
}
