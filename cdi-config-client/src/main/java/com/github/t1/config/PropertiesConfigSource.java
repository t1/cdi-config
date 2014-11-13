package com.github.t1.config;

import java.util.Properties;

import javax.enterprise.inject.spi.DefinitionException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class PropertiesConfigSource implements ConfigSource {
    private final Properties properties;

    @Override
    public void configure(ConfigurationPoint configPoint) {
        String stringValue = getProperty(configPoint);
        String info = "property '" + configPoint.name() + "' from properties config source";
        configPoint.setConfigValue(new PropertyConfigValue(configPoint, stringValue, info));
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
