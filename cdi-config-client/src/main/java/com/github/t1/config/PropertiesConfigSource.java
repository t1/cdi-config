package com.github.t1.config;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertiesConfigSource implements ConfigSource {
    private class PropertyConfigValue extends UpdatableConfigValue {
        public PropertyConfigValue(ConfigurationPoint configPoint) {
            super(configPoint);
        }

        @Override
        protected Object getValue() {
            String property = getProperty(configPoint());
            return convert(configPoint().type(), property);
        }

        @Override
        public String toString() {
            return "property '" + configPoint().name() + "'" + " from " + uri;
        }
    }

    private final URI uri;
    private final Properties properties;

    private static FileMonitor FILE_MONITOR = new FileMonitor();

    public PropertiesConfigSource(URI uri) {
        this.uri = resolve(uri);
        this.properties = load();

        initMonitor();
    }

    private URI resolve(URI uri) {
        if (isFileScheme(uri)) {
            Path path = Paths.get(uri.getSchemeSpecificPart());
            if (path.startsWith(".")) {
                return subpath(path, 1).toUri();
            } else if (path.startsWith("~")) {
                Path home = Paths.get(System.getProperty("user.home"));
                return home.resolve(subpath(path, 1)).toUri();
            }
        }
        return uri;
    }

    private boolean isFileScheme(URI uri) {
        return "file".equals(uri.getScheme());
    }

    @SneakyThrows(IOException.class)
    private Properties load() {
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
            return properties;
        }
    }

    private static Path subpath(Path path, int beginIndex) {
        return path.subpath(beginIndex, path.getNameCount());
    }

    private void initMonitor() {
        if (isFileScheme(uri)) {
            FILE_MONITOR.add(Paths.get(uri), new Runnable() {
                @Override
                public void run() {
                    log.debug("{} changed", uri);
                    // TODO reload properties and update all changed config values
                }
            });
        }
    }

    public void stop() {
        FILE_MONITOR.remove(Paths.get(uri));
    }

    @Override
    public void configure(ConfigurationPoint configPoint) {
        if (getProperty(configPoint) == null)
            return;
        configPoint.setConfigValue(new PropertyConfigValue(configPoint));
    }

    private String getProperty(ConfigurationPoint configPoint) {
        return properties.getProperty(configPoint.name());
    }

    @Override
    public String toString() {
        return properties.size() + " properties from " + uri;
    }

    public Set<String> propertyNames() {
        return properties.stringPropertyNames();
    }

    public String removeProperty(String key) {
        return (String) properties.remove(key);
    }
}
