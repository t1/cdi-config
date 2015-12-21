package com.github.t1.config;

import com.github.t1.config.ConfigPoint.ConfigValue;
import com.github.t1.config.EnvironmentVariablesConfigSource.EnvironmentVariableConfigValue;

public class EnvironmentVariablesConfigSource extends MapConfigSource<EnvironmentVariableConfigValue> {
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
    protected EnvironmentVariableConfigValue createConfigValueFor(ConfigPoint configPoint) {
        return new EnvironmentVariableConfigValue(configPoint.name(), configPoint);
    }
}
