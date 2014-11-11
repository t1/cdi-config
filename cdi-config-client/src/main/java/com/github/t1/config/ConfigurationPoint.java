package com.github.t1.config;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.inject.InjectionException;

import lombok.Setter;

/**
 * The point where a configuration should go into, i.e. the field annotated as {@link Config}, i.e. on the class level,
 * not the instance.
 */
abstract class ConfigurationPoint {
    public static ConfigurationPoint on(Field field) {
        field.setAccessible(true);
        ConfigurationPoint configPoint = (field.getType().isAssignableFrom(AtomicReference.class)) //
                ? new AtomicReferenceConfigurationPoint() //
                : new StandardConfigurationPoint();
        configPoint.field = field;
        return configPoint;
    }

    private Field field;
    @Setter
    private ConfigSource source;

    protected Object value() {
        return source.getValueFor(field);
    }

    protected Class<?> type() {
        return field.getType();
    }

    protected Object get(Object target) {
        try {
            return field.get(target);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InjectionException("can't get " + field, e);
        }
    }

    protected void set(Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            String type = (value == null) ? null : value.getClass().getSimpleName();
            throw new InjectionException("can't set " + field + " to a " + type, e);
        }
    }

    public abstract void configure(Object target);

    public abstract void deconfigure(Object target);
}
