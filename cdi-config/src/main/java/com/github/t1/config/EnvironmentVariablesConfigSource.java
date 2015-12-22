package com.github.t1.config;

public class EnvironmentVariablesConfigSource implements ConfigSource {
    class EnvironmentVariableConfigValue extends ConfigValue {
        public EnvironmentVariableConfigValue(String name) {
            super(name);
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
        configPoint.configureTo(new EnvironmentVariableConfigValue(configPoint.name()));
    }
}
