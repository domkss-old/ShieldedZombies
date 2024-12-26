package net.domkss.shieldedzombies.config;

import java.lang.annotation.*;

/**
 * The annotated field will be included in the config file with default value if not already present
 * <p>
 * Use the `comment` parameter to add descriptive notes.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ConfigField {
    /**
     * Optional comment to describe the field purpose in the YAML file.
     *
     * @return the comment string (default is empty)
     */
    String comment()  default "";
}