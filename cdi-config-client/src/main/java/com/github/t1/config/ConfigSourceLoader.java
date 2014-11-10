package com.github.t1.config;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Properties;

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
            return toURI(systemProperty);
        }
    }

    private static URI toURI(String value) {
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

    private static Path subpath(Path path, int beginIndex) {
        return path.subpath(beginIndex, path.getNameCount());
    }

    public ConfigSource load() {
        return new PropertiesConfigSource(loadProperties(uri));
    }

    private Properties loadProperties(URI uri) {
        log.debug("load config source {}", uri);
        try (InputStream stream = uri.toURL().openStream()) {
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
}
