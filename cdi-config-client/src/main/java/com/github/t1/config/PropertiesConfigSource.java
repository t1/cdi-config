package com.github.t1.config;

import java.net.URI;
import java.util.Properties;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PropertiesConfigSource implements ConfigSource {
    private class PropertyConfigValue extends UpdatableConfigValue {
        public PropertyConfigValue(ConfigurationPoint configPoint) {
            super(configPoint);
        }

        @Override
        protected Object getValue() {
            String property = getProperty(configPoint());
            return convert(configPoint().type(), property);
        }

        @Override
        public String toString() {
            return "property '" + configPoint().name() + "'" + " from " + uri;
        }
    }

    private final URI uri;
    private final Properties properties;

    @Override
    public void configure(ConfigurationPoint configPoint) {
        if (getProperty(configPoint) == null)
            return;
        configPoint.setConfigValue(new PropertyConfigValue(configPoint));
    }

    private String getProperty(ConfigurationPoint configPoint) {
        return properties.getProperty(configPoint.name());
    }

    @Override
    public String toString() {
        return properties.size() + " properties from " + uri;
    }
}
