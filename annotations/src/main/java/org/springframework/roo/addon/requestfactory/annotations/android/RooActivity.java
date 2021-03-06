package org.springframework.roo.addon.requestfactory.annotations.android;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RooActivity {

    String NO_TITLE_ATTRIBUTE = "noTitle";
    String FULLSCREEN_ATTRIBUTE = "fullscreen";

    String value() default "";

    boolean noTitle() default false;

    boolean fullscreen() default false;
}
