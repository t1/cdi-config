package com.github.t1.config;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

@Retention(RUNTIME)
@Target({ FIELD })
public @interface Config {
    public static final String USE_FIELD_NAME = "###_USE_FIELD_NAME_###";

    public String name() default USE_FIELD_NAME;
}
