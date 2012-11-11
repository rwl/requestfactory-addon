package org.springframework.roo.addon.requestfactory.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target( { ElementType.FIELD } )
public @interface RooHelpText {

    String value() default "";
}
