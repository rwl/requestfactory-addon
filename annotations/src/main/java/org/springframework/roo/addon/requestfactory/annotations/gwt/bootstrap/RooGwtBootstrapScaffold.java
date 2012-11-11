package org.springframework.roo.addon.requestfactory.annotations.gwt.bootstrap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RooGwtBootstrapScaffold {

    String MODULE_ATTRIBUTE = "module";

    String module();
}
