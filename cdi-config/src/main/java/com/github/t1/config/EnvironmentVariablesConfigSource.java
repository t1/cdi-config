package com.github.t1.config;

import com.github.t1.config.ConfigPoint.ConfigValue;

public class EnvironmentVariablesConfigSource implements ConfigSource {
    class EnvironmentVariableConfigValue extends ConfigValue {
        public EnvironmentVariableConfigValue(String name, ConfigPoint configPoint) {
            configPoint.super(name);
        }

        @Override
        public <T> T getValue(Class<T> type) {
            String value = stringValue();
            return convert(value, type);
        }

        private String stringValue() {
            return System.getenv(getName());
        }

        @Override
        public String toString() {
            return "environment variable " + getName();
        }
    }

    @Override
    public void configure(ConfigPoint configPoint) {
        ConfigValue configValue = new EnvironmentVariableConfigValue(configPoint.name(), configPoint);
        if (configValue.getValue(configPoint.type()) == null)
            return;
        configPoint.configValue(configValue);
    }
}
