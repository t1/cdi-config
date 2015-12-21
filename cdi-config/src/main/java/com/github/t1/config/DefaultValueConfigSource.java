package com.github.t1.config;

import com.github.t1.config.ConfigPoint.ConfigValue;

public class DefaultValueConfigSource implements ConfigSource {
    public static class StaticConfigValue extends ConfigValue {
        private String value;

        private StaticConfigValue(String name, String value, ConfigPoint configPoint) {
            configPoint.super(name);
            this.value = value;
        }

        @Override
        protected <T> T getValue(Class<T> type) {
            return convert(value, type);
        }

        @Override
        public String toString() {
            return "default value " + getName();
        }
    }

    @Override
    public void configure(ConfigPoint configPoint) {
        configPoint.defaultValue().ifPresent(defaultValue -> {
            StaticConfigValue value = new StaticConfigValue(configPoint.name(), defaultValue, configPoint);
            configPoint.configValue(value);
        });
    }
}
