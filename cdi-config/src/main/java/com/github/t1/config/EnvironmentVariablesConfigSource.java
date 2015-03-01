package com.github.t1.config;

import com.github.t1.config.ConfigurationPoint.GettableConfigValue;
import com.github.t1.config.EnvironmentVariablesConfigSource.EnvironmentVariableConfigValue;

public class EnvironmentVariablesConfigSource extends MapConfigSource<EnvironmentVariableConfigValue> {
    class EnvironmentVariableConfigValue extends GettableConfigValue {
        public EnvironmentVariableConfigValue(ConfigurationPoint configPoint) {
            configPoint.super();
        }

        @Override
        public void addConfigTartet(Object target) {
            configPoint().set(target, getValue());
        }

        @Override
        public void removeConfigTartet(Object target) {}

        @Override
        public Object getValue() {
            String value = stringValue();
            return convert(value);
        }

        private String stringValue() {
            String key = configPoint().name();
            return System.getenv(key);
        }

        @Override
        public String toString() {
            return super.toString() + " from environment variable";
        }
    }

    @Override
    protected EnvironmentVariableConfigValue createConfigValueFor(ConfigurationPoint configPoint) {
        return new EnvironmentVariableConfigValue(configPoint);
    }

    @Override
    public void shutdown() {}
}
