package com.github.t1.config;

import static lombok.AccessLevel.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.inject.InjectionException;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import org.joda.convert.StringConvert;

import com.github.t1.stereotypes.Annotations;

/**
 * The point where a configuration should go into, i.e. the field annotated as {@link Config}. This is on the class
 * level, not the instance.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class ConfigurationPoint {
    private static final StringConvert STRING_CONVERT = StringConvert.INSTANCE;

    @RequiredArgsConstructor
    public abstract class ConfigValue {
        protected ConfigurationPoint configPoint() {
            return ConfigurationPoint.this;
        }

        protected Object convert(String value) {
            return STRING_CONVERT.convertFromString(configPoint().type(), resolve(value));
        }

        private String resolve(String value) {
            return (value == null) ? null : value.replace("$$", "$");
        }

        public abstract void addConfigTartet(Object target);

        public abstract void removeConfigTartet(Object target);

        /**
         * Do <em>not</em> produce the actual value... could be, e.g., a password. <br/>
         * And take care to not recurse into {@link ConfigurationPoint#toString()}
         */
        @Override
        public String toString() {
            return "config value for '" + configPoint().name() + "'";
        }
    }

    public abstract class GettableConfigValue extends ConfigValue {
        protected abstract Object getValue();
    }

    public abstract class UpdatableConfigValue extends GettableConfigValue {
        private final List<Object> targets = new ArrayList<>();

        @Override
        public void addConfigTartet(Object target) {
            this.targets.add(target);
            configPoint().set(target, getValue());
        }

        @Override
        public void removeConfigTartet(Object target) {
            targets.remove(target);
        }

        public void updateAllConfigTargets() {
            Object value = getValue();
            for (Object target : targets) {
                configPoint().set(target, value);
            }
        }
    }

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

    public String name() {
        String name = config().name();
        return Config.USE_FIELD_NAME.equals(name) ? field.getName() : name;
    }

    protected abstract Class<?> type();

    public void setConfigValue(ConfigValue configValue) {
        this.configValue = configValue;
        log.debug("configure {}", this);
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

    public void addConfigTarget(Object target) {
        configValue.addConfigTartet(target);
    }

    public void removeConfigTarget(Object target) {
        configValue.removeConfigTartet(target);
    }

    public abstract void set(Object target, Object value);

    @Override
    public String toString() {
        return type().getSimpleName() + " field '" + field.getName() + "' in " + field.getDeclaringClass() //
                + " named '" + name() + "' " //
                + ((configValue == null) ? "unconfigured" : ("configured to " + configValue));
    }
}