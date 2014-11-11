package com.github.t1.config;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.inject.InjectionException;

abstract class ConfigurationPoint {
    public static Optional<ConfigurationPoint> on(Field field, ConfigSource configSource) {
        if (!configSource.canConfigure(field))
            return Optional.empty();
        ConfigurationPoint configurationPoint = createConfigPointFor(field);
        configurationPoint.source = configSource;
        return Optional.of(configurationPoint);
    }

    public static ConfigurationPoint createConfigPointFor(Field field) {
        field.setAccessible(true);
        ConfigurationPoint configPoint = (field.getType().isAssignableFrom(AtomicReference.class)) //
                ? new AtomicReferenceConfigurationPoint() //
                : new StandardConfigurationPoint();
        configPoint.field = field;
        return configPoint;
    }

    private Field field;
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
