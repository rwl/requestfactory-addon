package org.springframework.roo.addon.requestfactory.annotations.android;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface RooResourceString {

    int value() default ResourceId.DEFAULT_VALUE;
}
