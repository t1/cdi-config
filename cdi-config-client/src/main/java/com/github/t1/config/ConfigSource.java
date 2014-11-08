package com.github.t1.config;

import java.lang.reflect.*;
import java.util.Properties;

import javax.enterprise.inject.spi.DefinitionException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.joda.convert.StringConvert;

import com.github.t1.stereotypes.Annotations;

@Slf4j
@AllArgsConstructor
public class ConfigSource {
    private static final StringConvert STRING_CONVERT = StringConvert.INSTANCE;

    private final Properties properties;

    public ConfigurationPoint getConfigPointFor(Field field) {
        AnnotatedElement annotations = Annotations.on(field);
        Config config = annotations.getAnnotation(Config.class);
        if (config == null)
            return null;
        String propertyName = propertyName(config, field);
        String stringValue = properties.getProperty(propertyName);
        if (stringValue == null) {
            log.error("can't configure {}", field);
            throw new DefinitionException("no config value found for " + field);
        }

        // do *not* log the value to configure... could be a password
        log.debug("create ConfigurationPoint for {} field '{}' in {} to property '{}'",
                field.getType().getSimpleName(), field.getName(), field.getDeclaringClass(), propertyName);
        Object convertedValue = STRING_CONVERT.convertFromString(field.getType(), stringValue);
        field.setAccessible(true);
        return new ConfigurationPoint(field, convertedValue);
    }

    private String propertyName(Config config, Field field) {
        String name = config.name();
        return Config.USE_FIELD_NAME.equals(name) ? field.getName() : name;
    }
}
