package com.github.t1.config;

import java.util.*;

import com.github.t1.config.ConfigurationPoint.UpdatableConfigValue;

public class SystemPropertiesConfigSource implements ConfigSource {
    private class SystemPropertiesConfigValue extends UpdatableConfigValue {
        public SystemPropertiesConfigValue(ConfigurationPoint configPoint) {
            configPoint.super();
        }

        @Override
        protected Object getValue() {
            String key = configPoint().name();
            String value = System.getProperty(key);
            return convert(value);
        }

        @Override
        public String toString() {
            return super.toString() + " from system properties";
        }
    }

    private final Map<String, SystemPropertiesConfigValue> map = new HashMap<>();

    @Override
    public void configure(ConfigurationPoint configPoint) {
        SystemPropertiesConfigValue configValue = configValueFor(configPoint);
        if (configValue.getValue() != null) {
            configPoint.setConfigValue(configValue);
        }
    }

    private SystemPropertiesConfigValue configValueFor(ConfigurationPoint configPoint) {
        SystemPropertiesConfigValue value = map.get(configPoint.name());
        if (value == null) {
            value = new SystemPropertiesConfigValue(configPoint);
            map.put(configPoint.name(), value);
        }
        return value;
        // TODO the shade plugin seems to misunderstand this:
        // return map.computeIfAbsent(configPoint.name(), (c) -> new SystemPropertiesConfigValue(configPoint));
    }

    @Override
    public void shutdown() {}
}
