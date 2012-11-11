package org.springframework.roo.addon.requestfactory.annotations.android;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RooAndroidScaffold {

    String MODULE_ATTRIBUTE = "module";

    String module();
}
