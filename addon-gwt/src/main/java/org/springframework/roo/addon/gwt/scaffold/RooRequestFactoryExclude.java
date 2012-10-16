package org.springframework.roo.addon.gwt.scaffold;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target( { ElementType.FIELD, ElementType.METHOD } )
public @interface RooRequestFactoryExclude {
}
