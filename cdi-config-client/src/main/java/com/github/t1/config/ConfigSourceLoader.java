package com.github.t1.config;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Properties;

import javax.enterprise.inject.spi.DefinitionException;

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
            Properties properties = loadProperties(uri);
            PropertiesConfigSource configSource = new PropertiesConfigSource(uri, properties);
            return resolveImports(properties, configSource);
        }
    }

    @SneakyThrows(ReflectiveOperationException.class)
    private ConfigSource insantiate(String className) {
        Class<?> type = Class.forName(className);
        return (ConfigSource) type.newInstance();
    }

    private Properties loadProperties(URI uri) {
        Path path = Paths.get(uri.getSchemeSpecificPart());
        if (path.startsWith(".")) {
            uri = subpath(path, 1).toUri();
        } else if (path.startsWith("~")) {
            Path home = Paths.get(System.getProperty("user.home"));
            uri = home.resolve(subpath(path, 1)).toUri();
        }
        try (InputStream stream = url(uri).openStream()) {
            Properties properties = new Properties();
            if (stream == null) {
                log.error("config source not found {}", uri);
            } else if (uri.getPath().endsWith(".properties")) {
                properties.load(stream);
            } else if (uri.getPath().endsWith(".xml")) {
                properties.loadFromXML(stream);
            } else {
                log.error("unknown uri suffix in {}", Paths.get(uri.getPath()).getFileName());
            }
            log.debug("loaded {} entries from {}", properties.size(), uri);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private URL url(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new DefinitionException(e);
        }
    }

    private static Path subpath(Path path, int beginIndex) {
        return path.subpath(beginIndex, path.getNameCount());
    }

    private ConfigSource resolveImports(Properties properties, ConfigSource result) {
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("*import*")) {
                String value = (String) properties.remove(key);
                log.debug("resolve import {}: {}", key, value);
                URI importUri = URI.create(value);
                ConfigSource resolvedSource = loadConfigSource(importUri);
                result = MultiConfigSource.of(result, resolvedSource);
            }
        }
        return result;
    }
}
