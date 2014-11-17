package com.github.t1.config;

import java.net.*;
import java.util.ArrayList;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ConfigSourceLoader {
    private final URI uri;

    public ConfigSourceLoader() {
        this(rootConfigSourceUri());
    }

    @SneakyThrows(URISyntaxException.class)
    private static URI rootConfigSourceUri() {
        String systemProperty = System.getProperty("cdi-config.config-source");
        if (systemProperty == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return classLoader.getResource("configuration.properties").toURI();
        } else {
            return URI.create(systemProperty);
        }
    }

    public ConfigSource load() {
        return loadConfigSource(uri);
    }

    private ConfigSource loadConfigSource(URI uri) {
        log.debug("load config source {}", uri);
        if ("java".equals(uri.getScheme())) {
            return insantiate(uri.getSchemeSpecificPart());
        } else {
            PropertiesConfigSource configSource = new PropertiesConfigSource(uri);
            return resolveImports(configSource);
        }
    }

    @SneakyThrows(ReflectiveOperationException.class)
    private ConfigSource insantiate(String className) {
        Class<?> type = Class.forName(className);
        return (ConfigSource) type.newInstance();
    }

    private ConfigSource resolveImports(PropertiesConfigSource properties) {
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
