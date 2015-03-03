package com.github.t1.config;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

import javax.interceptor.InterceptorBinding;

@InterceptorBinding
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface DelayedConfigChange {}
