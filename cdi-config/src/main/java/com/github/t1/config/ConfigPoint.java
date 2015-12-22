package com.github.t1.config;

import static lombok.AccessLevel.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.inject.InjectionException;

import com.github.t1.stereotypes.Annotations;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * The point where a config should go into, i.e. the field annotated as {@link Config}. This is on the class level and
 * contains references to all instances of this class. It also holds the {@link ConfigValue} to get the value from.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class ConfigPoint {
    public static ConfigPoint on(Field field) {
        if (config(field) == null)
            return null;
        field.setAccessible(true);
        return (field.getType().isAssignableFrom(AtomicReference.class)) //
                ? new AtomicReferenceConfigPoint(field) //
                : new StandardConfigPoint(field) //
                ;
    }

    private static Config config(Field field) {
        return Annotations.on(field).getAnnotation(Config.class);
    }

    @Getter(PROTECTED)
    private final Field field;
    private ConfigValue configValue;

    Config config() {
        return config(field);
    }

    public String name() {
        String name = config().name();
        return name.isEmpty() ? field.getName() : name;
    }

    public String description() {
        return config().description();
    }

    public Class<?> container() {
        return field.getDeclaringClass();
    }

    public String meta() {
        return config().meta();
    }

    public Optional<String> defaultValue() {
        String defaultValue = config().defaultValue();
        return (Config.NO_DEFAULT_VALUE.equals(defaultValue)) ? Optional.empty() : Optional.of(defaultValue);
    }

    public abstract Class<?> type();

    public void configureTo(ConfigValue configValue) {
        if (this.configValue != null)
            throw new IllegalStateException("configValue already set");
        if (configValue.getValue(type()) == null)
            return;
        this.configValue = configValue;
        log.debug("configure {}", this);
        configValue.addObserver(this::update);
    }

    private void update() {
        Object value = getValue();
        for (Object instance : instances)
            set(instance, value);
    }

    public Object getValue() {
        return configValue.getValue(type());
    }

    public boolean isWritable() {
        return configValue.isWritable();
    }

    public void writeValue(String value) {
        configValue.writeValue(value);
    }

    public boolean isConfigured() {
        return configValue != null;
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

    private final List<Object> instances = new ArrayList<>();

    public void addConfigTarget(Object target) {
        instances.add(target);
        set(target, getValue());
    }

    public void removeConfigTarget(Object target) {
        instances.remove(target);
    }

    public abstract void set(Object target, Object value);

    @Override
    public String toString() {
        return type().getSimpleName() + " field '" + field.getName() + "' in " + field.getDeclaringClass() //
                + " named '" + name() + "' " //
                + ((configValue == null) ? "unconfigured" : ("configured to " + configValue));
    }
}
