package org.springframework.roo.addon.requestfactory.annotations.gwt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RooRequestFactoryGwt {

    String MODULE_ATTRIBUTE = "module";

    String module();
}
