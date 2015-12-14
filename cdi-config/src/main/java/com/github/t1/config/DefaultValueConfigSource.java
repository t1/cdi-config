package com.github.t1.config;

import com.github.t1.config.ConfigPoint.ConfigValue;

public class DefaultValueConfigSource implements ConfigSource {
    public static class StaticConfigValue extends ConfigValue {
        private String value;

        private StaticConfigValue(ConfigPoint configPoint, String value) {
            configPoint.super();
            this.value = value;
        }

        @Override
        protected Object getValue() {
            return convert(value);
        }

        @Override
        public String toString() {
            return super.toString() + " from default value";
        }
    }

    @Override
    public void configure(ConfigPoint configPoint) {
        configPoint.defaultValue().ifPresent(defaultValue -> {
            StaticConfigValue value = new StaticConfigValue(configPoint, defaultValue);
            configPoint.configValue(value);
        });
    }
}
