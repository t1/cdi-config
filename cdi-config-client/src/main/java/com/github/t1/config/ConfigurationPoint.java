package com.github.t1.config;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.inject.InjectionException;

import lombok.*;

import com.github.t1.stereotypes.Annotations;

/**
 * The point where a configuration should go into, i.e. the field annotated as {@link Config}, i.e. on the class level,
 * not the instance.
 */
@RequiredArgsConstructor
abstract class ConfigurationPoint {
    public static Optional<ConfigurationPoint> on(Field field) {
        if (config(field) == null)
            return Optional.empty();
        field.setAccessible(true);
        return Optional.of((field.getType().isAssignableFrom(AtomicReference.class)) //
                ? new AtomicReferenceConfigurationPoint(field) //
                : new StandardConfigurationPoint(field) //
                );
    }

    private static Config config(Field field) {
        return Annotations.on(field).getAnnotation(Config.class);
    }

    @Getter
    private final Field field;
    @Setter
    private Object value;

    public String propertyName() {
        String name = config().name();
        return Config.USE_FIELD_NAME.equals(name) ? field.getName() : name;
    }

    public Config config() {
        return config(field);
    }

    protected Object value() {
        return value;
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
