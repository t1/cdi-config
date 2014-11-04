package com.github.t1.config;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;

import lombok.extern.slf4j.Slf4j;

import org.joda.convert.StringConvert;

import com.github.t1.stereotypes.Annotations;

@Slf4j
public class ConfigCdiExtension implements Extension {
    private static final String CONFIG_FILE = "configuration.properties";

    private static class ConfiguringInjectionTarget<T> extends InjectionTargetWrapper<T> {
        private final List<ConfigurationPoint> configs;

        private ConfiguringInjectionTarget(InjectionTarget<T> delegate, List<ConfigurationPoint> configs) {
            super(delegate);
            this.configs = configs;
        }

        @Override
        public void inject(T instance, CreationalContext<T> ctx) {
            for (ConfigurationPoint configurationPoint : configs) {
                configurationPoint.configure(instance);
            }

            log.debug("configured {}", instance);

            super.inject(instance, ctx);
        }

        @Override
        public void preDestroy(T instance) {
            super.preDestroy(instance);

            log.debug("deconfigure {}", instance);

            for (ConfigurationPoint configurationPoint : configs) {
                configurationPoint.deconfigure(instance);
            }
        }
    }

    private final StringConvert stringConvert = StringConvert.INSTANCE;

    private final Properties properties = loadProperties();

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream stream = classLoader().getResourceAsStream(CONFIG_FILE)) {
            if (stream == null) {
                log.debug("found no {}", CONFIG_FILE);
            } else {
                properties.load(stream);
                log.debug("found {} with {} entries", CONFIG_FILE, properties.size());
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

        List<ConfigurationPoint> configs = buildConfigs(pit);

        if (!configs.isEmpty()) {
            pit.setInjectionTarget(new ConfiguringInjectionTarget<>(it, configs));
        }
    }

    private <T> List<ConfigurationPoint> buildConfigs(ProcessInjectionTarget<T> pit) {
        Class<T> type = pit.getAnnotatedType().getJavaClass();
        log.trace("scan {} for configuration points", type);

        return adaptConfigs(pit, type);
    }

    private <T> List<ConfigurationPoint> adaptConfigs(ProcessInjectionTarget<T> pit, Class<T> type) {
        List<ConfigurationPoint> configs = new ArrayList<>();
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
                    log.debug("create ConfigurationPoint for {} field '{}' in {} to property '{}'", field.getType()
                            .getSimpleName(), field.getName(), type, propertyName);
                    Object convertedValue = stringConvert.convertFromString(field.getType(), stringValue);
                    field.setAccessible(true);
                    configs.add(new ConfigurationPoint(field, convertedValue));
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
