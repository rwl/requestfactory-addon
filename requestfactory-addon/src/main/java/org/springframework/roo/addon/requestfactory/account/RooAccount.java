package org.springframework.roo.addon.requestfactory.account;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RooAccount {

    String SHARED_PACKAGE_ATTRIBUTE = "sharedPackage";
    String SHARED_PACKAGE_DEFAULT = "";

    /**
     * @return the package for enums shared with GWT
     */
    String sharedPackage() default SHARED_PACKAGE_DEFAULT;
}
