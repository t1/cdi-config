package com.github.t1.config;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;
import java.net.URI;

/**
 * see {@value #WIKI}
 */
@Retention(RUNTIME)
@Target({ FIELD })
public @interface Config {
    public static final URI WIKI = URI.create("https://github.com/t1/cdi-config/wiki/");

    public static final String USE_FIELD_NAME = "###_USE_FIELD_NAME_###";

    public String name() default USE_FIELD_NAME;

    public String description() default "";
}
