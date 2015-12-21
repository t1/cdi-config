package com.github.t1.config;

import static lombok.AccessLevel.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.*;

import javax.enterprise.inject.InjectionException;

import org.joda.convert.StringConvert;

import com.github.t1.stereotypes.Annotations;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * The point where a config should go into, i.e. the field annotated as {@link Config}. This is on the class level, not
 * the instance.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class ConfigPoint {
    private static final Pattern EXPRESSION = Pattern.compile("\\{(?<key>[^}]*)\\}");
    private static final StringConvert STRING_CONVERT = StringConvert.INSTANCE;

    static String resolveExpressions(String value) {
        Matcher matcher = EXPRESSION.matcher(value);
        StringBuffer sb = new StringBuffer();
        boolean foundExpressions = false;
        while (matcher.find()) {
            foundExpressions = true;
            String key = matcher.group("key");
            String resolved = System.getProperty(key);
            if (resolved == null) {
                log.error("no system property for key '" + key + "'");
                resolved = "{" + key + "}";
            }
            matcher.appendReplacement(sb, resolved);
        }
        if (!foundExpressions)
            return value;
        matcher.appendTail(sb);
        String result = sb.toString();
        log.debug("resolved '{}' to '{}'", value, result);
        return result;
    }

    @RequiredArgsConstructor
    public abstract class ConfigValue {
        @Getter
        private final String name;

        protected ConfigPoint configPoint() {
            return ConfigPoint.this;
        }

        protected <T> T convert(String value, Class<T> type) {
            return STRING_CONVERT.convertFromString(type, resolve(value));
        }

        private String resolve(String value) {
            if (value == null)
                return null;
            return resolveExpressions(value);
        }

        public void addConfigTarget(Object target) {
            configPoint().set(target, getValue(configPoint().type()));
        }

        protected abstract <T> T getValue(Class<T> type);

        public void removeConfigTarget(@SuppressWarnings("unused") Object target) {}

        public boolean isWritable() {
            return false;
        }

        public void writeValue(@SuppressWarnings("unused") String value) {
            throw new UnsupportedOperationException("can't write a " + getClass().getName());
        }
    }

    public abstract class UpdatableConfigValue extends ConfigValue {
        private final List<Object> targets = new ArrayList<>();

        public UpdatableConfigValue(String name) {
            super(name);
        }

        @Override
        public void addConfigTarget(Object target) {
            this.targets.add(target);
            configPoint().set(target, getValue(configPoint().type()));
        }

        @Override
        public void removeConfigTarget(Object target) {
            targets.remove(target);
        }

        public void updateAllConfigTargets() {
            Object value = getValue(configPoint().type());
            for (Object target : targets) {
                configPoint().set(target, value);
            }
        }
    }

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

    private ConfigValue configValue;

    public ConfigValue configValue() {
        return configValue;
    }

    public void configValue(ConfigValue configValue) {
        this.configValue = configValue;
        log.debug("configure {}", this);
    }

    public Object getValue() {
        return configValue().getValue(type());
    }

    public boolean isWritable() {
        return configValue().isWritable();
    }

    public void writeValue(String value) {
        configValue().writeValue(value);
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
        configValue.addConfigTarget(target);
    }

    public void removeConfigTarget(Object target) {
        configValue.removeConfigTarget(target);
    }

    public abstract void set(Object target, Object value);

    @Override
    public String toString() {
        return type().getSimpleName() + " field '" + field.getName() + "' in " + field.getDeclaringClass() //
                + " named '" + name() + "' " //
                + ((configValue == null) ? "unconfigured" : ("configured to " + configValue));
    }
}
