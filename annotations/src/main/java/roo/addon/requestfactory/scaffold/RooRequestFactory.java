package roo.addon.requestfactory.scaffold;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RooRequestFactory {

    String PARENT_PROPERTY_ATTRIBUTE = "parentProperty";
    String PARENT_PROPERTY_DEFAULT = "";

    String PRIMARY_PROPERTY_ATTRIBUTE = "primaryProperty";
    String PRIMARY_PROPERTY_DEFAULT = "";

    String SECONDARY_PROPERTY_ATTRIBUTE = "secondaryProperty";
    String SECONDARY_PROPERTY_DEFAULT = "";

    /**
     * @return the field name of the parent reference
     */
    String parentProperty() default PARENT_PROPERTY_DEFAULT;

    /**
     * @return primary property to be used when rendering
     */
    String primaryProperty() default PRIMARY_PROPERTY_DEFAULT;

    /**
     * @return secondary property to be used when rendering
     */
    String secondaryProperty() default SECONDARY_PROPERTY_DEFAULT;
}
