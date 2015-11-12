package com.github.t1.config;

import com.github.t1.config.ConfigPoint.ConfigValue;
import com.github.t1.config.EnvironmentVariablesConfigSource.EnvironmentVariableConfigValue;

public class EnvironmentVariablesConfigSource extends MapConfigSource<EnvironmentVariableConfigValue> {
    class EnvironmentVariableConfigValue extends ConfigValue {
        public EnvironmentVariableConfigValue(ConfigPoint configPoint) {
            configPoint.super();
        }

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
    protected EnvironmentVariableConfigValue createConfigValueFor(ConfigPoint configPoint) {
        return new EnvironmentVariableConfigValue(configPoint);
    }
}
