package com.github.t1.config;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

import javax.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface Config {
    public static final String USE_FIELD_NAME = "###_USE_FIELD_NAME_###";

    public String name() default USE_FIELD_NAME;
}
