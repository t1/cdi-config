package com.github.t1.config;

import static com.github.t1.config.ConfigPoint.*;

import java.net.*;
import java.util.ArrayList;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigSourceLoader {
    private static final String DEFAULT_CONFIG_RESOURCE = "configuration.properties";

    private final URI uri;

    public ConfigSourceLoader() {
        this.uri = rootConfigSourceUri();
    }

    private static URI rootConfigSourceUri() {
        String result = System.getProperty("cdi-config.config-source");
        if (result == null)
            result = "classpath:" + DEFAULT_CONFIG_RESOURCE;
        return URI.create(result);
    }

    public ConfigSource load() {
        return MultiConfigSource.of( //
                new SystemPropertiesConfigSource(), //
                new EnvironmentVariablesConfigSource(), //
                loadConfigSource(uri), //
                new DefaultValueConfigSource());
    }

    private ConfigSource loadConfigSource(URI uri) {
        log.debug("load config source {}", uri);
        switch (uri.getScheme()) {
        case "java":
            return insantiate(uri.getSchemeSpecificPart());
        case "classpath":
            uri = resolveClasspath(uri);
            // fall through:
        default:
            PropertiesFileConfigSource configSource = new PropertiesFileConfigSource(uri);
            return resolveImports(configSource);
        }
    }

    @SneakyThrows(ReflectiveOperationException.class)
    private ConfigSource insantiate(String className) {
        Class<?> type = Class.forName(className);
        return (ConfigSource) type.newInstance();
    }

    @SneakyThrows(URISyntaxException.class)
    private URI resolveClasspath(URI uri) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null)
            classLoader = ClassLoader.getSystemClassLoader();
        URL resource = classLoader.getResource(uri.getSchemeSpecificPart());
        if (resource == null)
            throw new RuntimeException("no file '" + DEFAULT_CONFIG_RESOURCE + "' found");
        return resource.toURI();
    }

    private ConfigSource resolveImports(PropertiesFileConfigSource properties) {
        ConfigSource result = properties;
        for (String key : new ArrayList<>(properties.propertyNames())) {
            if (key.startsWith("*import*")) {
                String value = properties.removeProperty(key);
                log.debug("resolve import {}: {}", key, value);
                value = resolveExpressions(value);
                URI importUri = URI.create(value);
                ConfigSource resolvedSource = loadConfigSource(importUri);
                result = MultiConfigSource.of(result, resolvedSource);
            }
        }
        return result;
    }
}
