package com.github.t1.config;

import java.util.Properties;

import javax.enterprise.inject.spi.DefinitionException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class PropertiesConfigSource implements ConfigSource {
    private class PropertyConfigValue extends ConfigValue {
        public PropertyConfigValue(ConfigurationPoint configPoint) {
            super(configPoint);
        }

        @Override
        public void addConfigTartet(Object target) {
            configPoint().set(target, getValue());
        }

        private Object getValue() {
            String property = getProperty(configPoint());
            return convert(configPoint().type(), property);
        }

        @Override
        public String getConfigSourceInfo() {
            return null;
        }
    }

    private final Properties properties;

    @Override
    public void configure(ConfigurationPoint configPoint) {
        configPoint.setConfigValue(new PropertyConfigValue(configPoint));
    }

    private String getProperty(ConfigurationPoint configPoint) {
        String stringValue = properties.getProperty(configPoint.name());
        if (stringValue == null) {
            log.error("can't configure {}", configPoint);
            throw new DefinitionException("no config value found for " + configPoint);
        }
        return stringValue;
    }
}
