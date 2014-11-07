package com.github.t1.config;

import java.lang.reflect.Field;

import javax.enterprise.inject.InjectionException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ConfigurationPoint {
    private final Field field;
    private final Object value;
    private Object oldValue;

    public void configure(Object target) {
        try {
            oldValue = field.get(target);
            field.set(target, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InjectionException("can't set " + field + " to a " + value.getClass().getSimpleName(), e);
        }
    }

    public void deconfigure(Object target) {
        try {
            field.set(target, oldValue);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InjectionException("can't set " + field + " to old value", e);
        }
    }
}