package com.github.t1.config;

import java.util.*;

import com.github.t1.config.ConfigurationPoint.GettableConfigValue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MapConfigSource<T extends GettableConfigValue> implements ConfigSource {
    private final Map<String, T> map = new HashMap<>();

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
        T value = map.get(configPoint.name());
        if (value == null) {
            value = createConfigValueFor(configPoint);
            map.put(configPoint.name(), value);
            log.debug("created {}", value);
        }
        return value;
        // TODO the shade plugin seems to misunderstand this:
        // return map.computeIfAbsent(configPoint.name(), (c) -> new SystemPropertiesConfigValue(configPoint));
    }

    protected abstract T createConfigValueFor(ConfigurationPoint configPoint);
}
