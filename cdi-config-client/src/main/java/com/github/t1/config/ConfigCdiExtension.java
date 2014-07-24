package com.github.t1.config;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.*;

import lombok.extern.slf4j.Slf4j;

import org.joda.convert.StringConvert;

import com.github.t1.stereotypes.Annotations;

@Slf4j
public class ConfigCdiExtension implements Extension {
    private static final String CONFIG_FILE = "configuration.properties";

    private static class ConfiguringInjectionTarget<T> extends InjectionTargetWrapper<T> {
        private final Map<Field, Object> configs;

        private ConfiguringInjectionTarget(InjectionTarget<T> delegate, Map<Field, Object> configs) {
            super(delegate);
            this.configs = configs;
        }

        @Override
        public void inject(T target, CreationalContext<T> context) {
            super.inject(target, context);
            for (Map.Entry<Field, Object> entry : configs.entrySet()) {
                Field field = entry.getKey();
                Object value = entry.getValue();
                try {
                    field.set(target, value);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new InjectionException("can't set " + field + " to \"" + value + "\"", e);
                }
            }
        }
    }

    private final StringConvert stringConvert = StringConvert.INSTANCE;

    private final Properties properties = loadProperties();

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream stream = classLoader().getResourceAsStream(CONFIG_FILE)) {
            if (stream == null) {
                log.debug("found no " + CONFIG_FILE);
            } else {
                log.debug("found a " + CONFIG_FILE);
                properties.load(stream);
            }
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public <T> void processInjectionTarget(@Observes ProcessInjectionTarget<T> pit) {
        if (properties.isEmpty())
            return;

        InjectionTarget<T> it = pit.getInjectionTarget();

        Map<Field, Object> configs = buildConfigMap(pit);

        if (configs != null) {
            pit.setInjectionTarget(new ConfiguringInjectionTarget<>(it, configs));
        }
    }

    private <T> Map<Field, Object> buildConfigMap(ProcessInjectionTarget<T> pit) {
        Class<T> type = pit.getAnnotatedType().getJavaClass();
        log.trace("scan {} for configuration points", type);

        return adaptConfigs(pit, type);
    }

    private <T> Map<Field, Object> adaptConfigs(ProcessInjectionTarget<T> pit, Class<T> type) {
        Map<Field, Object> configs = new HashMap<>();
        for (Field field : type.getDeclaredFields()) {
            AnnotatedElement annotations = Annotations.on(field);
            Config config = annotations.getAnnotation(Config.class);
            if (config != null) {
                String propertyName = propertyName(config, field);
                String stringValue = properties.getProperty(propertyName);
                if (stringValue == null) {
                    pit.addDefinitionError(new DefinitionException("no config value found for " + field));
                } else {
                    // do *not* log the value to configure... could be a password
                    log.debug("configure {} field {} in {} to property {}", field.getType().getSimpleName(),
                            field.getName(), type, propertyName);
                    Object convertedValue = stringConvert.convertFromString(field.getType(), stringValue);
                    field.setAccessible(true);
                    configs.put(field, convertedValue);
                }
            }
        }
        return configs;
    }

    private String propertyName(Config config, Field field) {
        String name = config.name();
        return Config.USE_FIELD_NAME.equals(name) ? field.getName() : name;
    }
}
