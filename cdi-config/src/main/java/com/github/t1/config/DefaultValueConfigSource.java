package com.github.t1.config;

public class DefaultValueConfigSource implements ConfigSource {
    public static class StaticConfigValue extends ConfigValue {
        private String value;

        private StaticConfigValue(String name, String value) {
            super(name);
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
            StaticConfigValue value = new StaticConfigValue(configPoint.name(), defaultValue);
            configPoint.configValue(value);
        });
    }
}
