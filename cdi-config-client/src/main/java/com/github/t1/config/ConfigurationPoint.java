package com.github.t1.config;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

interface ConfigurationPoint {
    public static Optional<ConfigurationPoint> on(Field field, ConfigSource configSource) {
        Optional<Object> value = configSource.getValueFor(field);
        if (!value.isPresent())
            return Optional.empty();
        return Optional.of(getConfigPointFor(field, value.get()));
    }

    public static ConfigurationPoint getConfigPointFor(Field field, Object value) {
        field.setAccessible(true);
        if (field.getType().isAssignableFrom(AtomicReference.class))
            return new AtomicReferenceConfigurationPoint(field, value);
        return new StandardConfigurationPoint(field, value);
    }

    public void configure(Object target);

    public void deconfigure(Object target);
}
