package com.github.t1.config;

import java.lang.reflect.Field;

import javax.enterprise.inject.InjectionException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class ConfigurationPoint {
    private final Field field;
    private final Object value;

    public void configure(Object target) {
        try {
            field.set(target, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InjectionException("can't set " + field + " to a " + value.getClass().getSimpleName(), e);
        }
    }

    public void deconfigure(Object target) {
        try {
            field.set(target, null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InjectionException("can't set " + field + " to null", e);
        }
    }
}