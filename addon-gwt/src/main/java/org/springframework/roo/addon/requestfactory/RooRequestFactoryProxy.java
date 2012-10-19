package org.springframework.roo.addon.requestfactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RooRequestFactoryProxy {

    String SERVER_MODULE_ATTRIBUTE = "serverModule";
    String SERVER_MODULE_DEFAULT = "";

    String[] exclude() default {};

    String[] readOnly() default {};

    boolean scaffold() default false;

    /**
     * @return the fully-qualified type name this key instance was mirrored from
     */
    String value();

    String serverModule() default SERVER_MODULE_DEFAULT;
}
