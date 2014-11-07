package com.github.t1.config;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.joda.convert.StringConvert;

import com.github.t1.stereotypes.Annotations;

@Slf4j
public class ConfigCdiExtension implements Extension {
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

    private Properties properties;

    private Properties properties() {
        if (properties == null)
            properties = loadProperties(configSource());
        return properties;
    }

    @SneakyThrows(URISyntaxException.class)
    private URI configSource() {
        String systemProperty = System.getProperty("cdi-config.config-source");
        if (systemProperty == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return classLoader.getResource("configuration.properties").toURI();
        } else {
            return toURI(systemProperty);
        }
    }

    private Properties loadProperties(URI configSource) {
        log.debug("load config source {}", configSource);
        try (InputStream stream = configSource.toURL().openStream()) {
            Properties properties = new Properties();
            if (stream == null) {
                log.error("config source not found {}", configSource);
            } else if (configSource.getPath().endsWith(".properties")) {
                properties.load(stream);
            } else if (configSource.getPath().endsWith(".xml")) {
                properties.loadFromXML(stream);
            } else {
                log.error("unknown uri suffix in {}", Paths.get(configSource.getPath()).getFileName());
            }
            log.debug("loaded {} entries from {}", properties.size(), configSource);
            resolveImports(properties);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void resolveImports(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("*import*")) {
                String value = (String) properties.remove(key);
                log.debug("resolve import {}: {}", key, value);
                URI importUri = toURI(value);
                Properties subProperties = loadProperties(importUri);
                properties.putAll(subProperties);
            }
        }
    }

    private URI toURI(String value) {
        URI uri = URI.create(value);
        if ("file".equals(uri.getScheme())) {
            Path path = Paths.get(uri.getSchemeSpecificPart());
            if (path.startsWith(".")) {
                uri = subpath(path, 1).toUri();
            } else if (path.startsWith("~")) {
                Path home = Paths.get(System.getProperty("user.home"));
                uri = home.resolve(subpath(path, 1)).toUri();
            }
        }
        return uri;
    }

    private Path subpath(Path path, int beginIndex) {
        return path.subpath(beginIndex, path.getNameCount());
    }

    public <T> void processInjectionTarget(@Observes ProcessInjectionTarget<T> pit) {
        if (properties().isEmpty())
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
                String stringValue = properties().getProperty(propertyName);
                if (stringValue == null) {
                    log.error("can't configure {}", field);
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
