package org.springframework.roo.addon.gwt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RooRequestFactoryProxy {

    String LOCATOR_MODULE_ATTRIBUTE = "locatorModule";
    String LOCATOR_MODULE_DEFAULT = "";

    String[] exclude() default {};

    String[] readOnly() default {};

    boolean scaffold() default false;

    /**
     * @return the fully-qualified type name this key instance was mirrored from
     */
    String value();

    String locatorModule() default LOCATOR_MODULE_DEFAULT;
}
