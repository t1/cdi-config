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
            return convert(System.getProperty(key));
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
        return map.computeIfAbsent(configPoint.name(), (c) -> new SystemPropertiesConfigValue(configPoint));
    }

    @Override
    public void shutdown() {}
}
