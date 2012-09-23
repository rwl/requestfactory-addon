package org.springframework.roo.addon.gwt.bootstrap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RooGwtBootstrap {

    /**
     * @return the field name of the parent reference
     */
    String parent() default "";
}
