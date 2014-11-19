package com.github.t1.config;

import java.net.*;
import java.util.ArrayList;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigSourceLoader {
    private final URI uri;

    public ConfigSourceLoader() {
        this.uri = rootConfigSourceUri();
    }

    @SneakyThrows(URISyntaxException.class)
    private static URI rootConfigSourceUri() {
        String systemProperty = System.getProperty("cdi-config.config-source");
        if (systemProperty == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null)
                classLoader = ClassLoader.getSystemClassLoader();
            URL resource = classLoader.getResource("configuration.properties");
            if (resource == null)
                throw new RuntimeException("no file configuration.properties found");
            return resource.toURI();
        } else {
            return URI.create(systemProperty);
        }
    }

    public ConfigSource load() {
        return MultiConfigSource.of( //
                new SystemPropertiesConfigSource(), //
                loadConfigSource(uri));
    }

    private ConfigSource loadConfigSource(URI uri) {
        log.debug("load config source {}", uri);
        if ("java".equals(uri.getScheme())) {
            return insantiate(uri.getSchemeSpecificPart());
        } else {
            PropertiesFileConfigSource configSource = new PropertiesFileConfigSource(uri);
            return resolveImports(configSource);
        }
    }

    @SneakyThrows(ReflectiveOperationException.class)
    private ConfigSource insantiate(String className) {
        Class<?> type = Class.forName(className);
        return (ConfigSource) type.newInstance();
    }

    private ConfigSource resolveImports(PropertiesFileConfigSource properties) {
        ConfigSource result = properties;
        for (String key : new ArrayList<>(properties.propertyNames())) {
            if (key.startsWith("*import*")) {
                String value = properties.removeProperty(key);
                log.debug("resolve import {}: {}", key, value);
                URI importUri = URI.create(value);
                ConfigSource resolvedSource = loadConfigSource(importUri);
                result = MultiConfigSource.of(result, resolvedSource);
            }
        }
        return result;
    }
}
