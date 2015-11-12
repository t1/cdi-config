package com.github.t1.config;

import java.lang.reflect.Field;
import java.util.*;

import com.github.t1.config.ConfigurationPoint.GettableConfigValue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MapConfigSource<T extends GettableConfigValue> implements ConfigSource {
    private final Map<Field, T> map = new HashMap<>();

    @Override
    public void configure(ConfigurationPoint configPoint) {
        T configValue = configValueFor(configPoint);
        if (configValue.getValue() != null) {
            configPoint.setConfigValue(configValue);
        }
    }

    protected Collection<T> mapValues() {
        return map.values();
    }

    private T configValueFor(ConfigurationPoint configPoint) {
        return map.computeIfAbsent(configPoint.getField(), c -> {
            T value = createConfigValueFor(configPoint);
            log.debug("created {}", value);
            return value;
        });
    }

    protected abstract T createConfigValueFor(ConfigurationPoint configPoint);
}
