package com.github.t1.config;

import java.util.Properties;

import javax.enterprise.inject.spi.DefinitionException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.joda.convert.StringConvert;

@Slf4j
@AllArgsConstructor
public class PropertiesConfigSource implements ConfigSource {
    private static final StringConvert STRING_CONVERT = StringConvert.INSTANCE;

    private final Properties properties;

    @Override
    public void configure(ConfigurationPoint configPoint) {
        String value = value(configPoint);
        Object converted = convert(configPoint.type(), value);
        configPoint.setValue(new ConfigValue(converted));
    }

    private String value(ConfigurationPoint configPoint) {
        String propertyName = configPoint.propertyName();
        String stringValue = properties.getProperty(propertyName);
        if (stringValue == null) {
            log.error("can't configure {}", configPoint);
            throw new DefinitionException("no config value found for " + configPoint);
        }
        return stringValue;
    }

    protected <T> T convert(Class<T> type, String value) {
        return STRING_CONVERT.convertFromString(type, value);
    }
}
