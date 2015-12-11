package com.github.t1.config;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

/**
 * @see <a href="https://github.com/t1/cdi-config/wiki">wiki</a>
 */
@Retention(RUNTIME)
@Target({ FIELD })
public @interface Config {
    public static final String NO_DEFAULT_VALUE = "$$$\n###\t NO_DEFAULT_VALUE \t###\n$$$";

    /** Defaults to the field name */
    public String name() default "";

    /**
     * May be used for config tools or config files. Must be the same on all {@link ConfigPoint}s or empty. It may prove
     * helpful to use a string constant, so references are clear.
     */
    public String description() default "";

    /**
     * The string version of the default value. Must be the same on all {@link ConfigPoint}s.
     */
    public String defaultValue() default NO_DEFAULT_VALUE;
}
