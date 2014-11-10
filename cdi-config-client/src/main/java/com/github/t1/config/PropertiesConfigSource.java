package com.github.t1.config;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.inject.spi.DefinitionException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.joda.convert.StringConvert;

import com.github.t1.stereotypes.Annotations;

@Slf4j
@AllArgsConstructor
public class PropertiesConfigSource implements ConfigSource {
    private static final StringConvert STRING_CONVERT = StringConvert.INSTANCE;

    private final Properties properties;

    @Override
    public Optional<Object> getValueFor(Field field) {
        Config config = Annotations.on(field).getAnnotation(Config.class);
        if (config == null)
            return Optional.empty();
        String propertyName = propertyName(config, field);
        // do *not* log the value to configure... could be a password
        log.debug("get value for {} field '{}' in {} to property '{}' to property '{}'", field.getType()
                .getSimpleName(), field.getName(), field.getDeclaringClass(), propertyName);
        return Optional.of(convert(type(field), value(field, propertyName)));
    }

    private Class<?> type(Field field) {
        if (AtomicReference.class.isAssignableFrom(field.getType()))
            return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        return field.getType();
    }

    protected <T> T convert(Class<T> type, String value) {
        return STRING_CONVERT.convertFromString(type, value);
    }

    private String value(Field field, String propertyName) {
        String stringValue = properties.getProperty(propertyName);
        if (stringValue == null) {
            log.error("can't configure {}", field);
            throw new DefinitionException("no config value found for " + field);
        }
        return stringValue;
    }

    private String propertyName(Config config, Field field) {
        String name = config.name();
        return Config.USE_FIELD_NAME.equals(name) ? field.getName() : name;
    }
}
