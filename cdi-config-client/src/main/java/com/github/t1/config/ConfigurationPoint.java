package com.github.t1.config;

import static lombok.AccessLevel.*;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.inject.InjectionException;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import com.github.t1.stereotypes.Annotations;

/**
 * The point where a configuration should go into, i.e. the field annotated as {@link Config} on the class level, not
 * the instance.
 */
@Slf4j
@RequiredArgsConstructor
abstract class ConfigurationPoint {
    public static ConfigurationPoint on(Field field) {
        if (config(field) == null)
            return null;
        field.setAccessible(true);
        return (field.getType().isAssignableFrom(AtomicReference.class)) //
                ? new AtomicReferenceConfigurationPoint(field) //
                : new StandardConfigurationPoint(field) //
        ;
    }

    private static Config config(Field field) {
        return Annotations.on(field).getAnnotation(Config.class);
    }

    @Getter(PROTECTED)
    private final Field field;

    private ConfigValue configValue;

    public Config config() {
        return config(field);
    }

    protected String name() {
        String name = config().name();
        return Config.USE_FIELD_NAME.equals(name) ? field.getName() : name;
    }

    protected abstract Class<?> type();

    public void setConfigValue(ConfigValue configValue) {
        this.configValue = configValue;
        log.debug("configure {}", this);
    }

    protected Object getField(Object target) {
        try {
            return field.get(target);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InjectionException("can't get " + field, e);
        }
    }

    protected void setField(Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            String type = (value == null) ? null : value.getClass().getSimpleName();
            throw new InjectionException("can't set " + field + " to a " + type, e);
        }
    }

    public void addConfigTarget(Object target) {
        configValue.addConfigTartet(target);
    }

    public abstract void set(Object target, Object value);

    public void removeConfigTarget(Object target) {
        configValue.removeConfigTartet(target);
    }

    @Override
    public String toString() {
        return type().getSimpleName() + " field '" + field.getName() + "' in " + field.getDeclaringClass() //
                + " to " + configValue.getConfigSourceInfo();
    }
}
