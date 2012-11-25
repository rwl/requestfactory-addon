package org.springframework.roo.addon.requestfactory.annotations.graph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RooGraphNode {

    String X_FIELD_ATTRIBUTE = "x";
    String Y_FIELD_ATTRIBUTE = "y";
    
    String X_FIELD_DEFAULT = "x";
    String Y_FIELD_DEFAULT = "y";

    String x() default X_FIELD_DEFAULT;
    String y() default Y_FIELD_DEFAULT;
}
