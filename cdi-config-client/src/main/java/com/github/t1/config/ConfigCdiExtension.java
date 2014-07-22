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

    StringConvert stringConvert = StringConvert.INSTANCE;

    public <T> void processInjectionTarget(@Observes ProcessInjectionTarget<T> pit) {
        InjectionTarget<T> it = pit.getInjectionTarget();

        Map<Field, Object> configs = buildConfigMap(pit);

        if (configs != null) {
            pit.setInjectionTarget(new ConfiguringInjectionTarget<>(it, configs));
        }
    }

    private <T> Map<Field, Object> buildConfigMap(ProcessInjectionTarget<T> pit) {
        Class<T> type = pit.getAnnotatedType().getJavaClass();
        log.debug("scan {} for configuration points", type);

        Properties properties = loadProperties(type);
        if (properties.isEmpty())
            return null;

        return adaptConfigs(type, properties);
    }

    private <T> Properties loadProperties(Class<T> type) {
        Properties properties = new Properties();
        try (InputStream stream = type.getResourceAsStream(type.getSimpleName() + ".properties")) {
            if (stream != null) {
                properties.load(stream);
            }
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> Map<Field, Object> adaptConfigs(Class<T> type, Properties properties) {
        Map<Field, Object> configs = new HashMap<>();
        for (Field field : type.getDeclaredFields()) {
            AnnotatedElement annotations = Annotations.on(field);
            Config config = annotations.getAnnotation(Config.class);
            if (config != null) {
                log.debug("  configure field {}: {}", field.getName(), field.getType().getSimpleName());
                String stringValue = (String) properties.get(field.getName());
                Object convertedValue = stringConvert.convertFromString(field.getType(), stringValue);
                configs.put(field, convertedValue);
            }
        }
        return configs;
    }
}
