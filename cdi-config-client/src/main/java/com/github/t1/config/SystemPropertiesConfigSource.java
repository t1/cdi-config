package com.github.t1.config;

public class SystemPropertiesConfigSource implements ConfigSource {
    private static class SystemPropertiesConfigValue extends UpdatableConfigValue {
        public SystemPropertiesConfigValue(ConfigurationPoint configPoint) {
            super(configPoint);
        }

        @Override
        protected Object getValue() {
            String key = configPoint().name();
            return convert(System.getProperty(key));
        }
    }

    @Override
    public void configure(ConfigurationPoint configPoint) {
        SystemPropertiesConfigValue configValue = new SystemPropertiesConfigValue(configPoint);
        if (configValue.getValue() != null) {
            configPoint.setConfigValue(configValue);
        }
    }

    @Override
    public void shutdown() {}
}
