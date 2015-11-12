package com.github.t1.config;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

import javax.interceptor.InterceptorBinding;

/**
 * All beans that have at least one {@link Config} annotation are automatically marked as being configured.
 *
 * @see ConfigChangeDelayInterceptor
 */
@InterceptorBinding
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface ConfiguredBean {}
