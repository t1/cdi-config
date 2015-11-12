package com.github.t1.config;

import com.github.t1.config.ConfigurationPoint.ConfigValue;

public class DefaultValueConfigSource implements ConfigSource {
    public static class StaticConfigValue extends ConfigValue {
        private String value;

        private StaticConfigValue(ConfigurationPoint configPoint, String value) {
            configPoint.super();
            this.value = value;
        }

        @Override
        protected Object getValue() {
            return convert(value);
        }
    }

    @Override
    public void configure(ConfigurationPoint configPoint) {
        String defaultValue = configPoint.defaultValue();
        if (defaultValue == null)
            return;
        configPoint.setConfigValue(new StaticConfigValue(configPoint, defaultValue));
    }
}
