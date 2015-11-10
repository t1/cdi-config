package com.github.t1.config;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import com.github.t1.config.ConfigurationPoint.UpdatableConfigValue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertiesFileConfigSource implements ConfigSource {
    private class PropertyConfigValue extends UpdatableConfigValue {
        public PropertyConfigValue(ConfigurationPoint configPoint) {
            configPoint.super();
        }

        @Override
        protected Object getValue() {
            Property property = getProperty(configPoint());
            String value = (property == null) ? null : property.value;
            return convert(value);
        }

        @Override
        public String toString() {
            return super.toString() + " from " + uri;
        }
    }

    private static class Property {
        private String value;
        private final List<PropertyConfigValue> configs = new ArrayList<>();

        public Property(String value) {
            this.value = value;
        }

        public void add(PropertyConfigValue configValue) {
            configs.add(configValue);
        }

        public boolean updateValue(String newValue) {
            if (value.equals(newValue))
                return false;
            value = newValue;
            for (PropertyConfigValue config : configs) {
                log.debug("update {}", config.configPoint());
                config.updateAllConfigTargets();
            }
            return true;
        }
    }

    private final URI uri;
    private final Map<String, Property> properties;

    private static FileMonitor FILE_MONITOR = new FileMonitor();

    public PropertiesFileConfigSource(URI uri) {
        this.uri = resolve(uri);
        this.properties = toPropertyMap(load());

        initMonitor();
    }

    private URI resolve(URI uri) {
        if (isFileScheme(uri) && uri.isOpaque()) {
            try {
                Path path = Paths.get(uri.getSchemeSpecificPart());
                if (path.startsWith("~")) {
                    Path home = Paths.get(System.getProperty("user.home"));
                    return home.resolve(subpath(path, 1)).toUri();
                } else {
                    return path.toUri();
                }
            } catch (Throwable e) {
                /**
                 * If the current working directory (user.dir) resp. home dir (user.home) doesn't exist, then
                 * Paths.get(uri) throws: java.lang.NoClassDefFoundError: Could not initialize class
                 * java.nio.file.FileSystems$DefaultFileSystemHolder
                 */
                log.error("can't resolve opaque file uri " + uri);
                throw e;
            }
        }
        return uri;
    }

    private boolean isFileScheme(URI uri) {
        return "file".equals(uri.getScheme());
    }

    private static Path subpath(Path path, int beginIndex) {
        return path.subpath(beginIndex, path.getNameCount());
    }

    private Map<String, Property> toPropertyMap(Properties in) {
        Map<String, Property> out = new LinkedHashMap<>();
        for (String name : in.stringPropertyNames()) {
            out.put(name, new Property(in.getProperty(name)));
        }
        return out;
    }

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
        } catch (IOException e) {
            log.warn("can't open properties uri " + uri + ": " + e + ". Fall back to empty properties.");
            return new Properties();
        }
    }

    private void initMonitor() {
        if (isFileScheme(uri)) {
            FILE_MONITOR.add(Paths.get(uri), new Runnable() {
                @Override
                public void run() {
                    log.debug("{} changed", uri);
                    update();
                }
            });
        }
    }

    private void update() {
        Properties newProperties = load();
        ArrayList<String> keys = new ArrayList<>(newProperties.stringPropertyNames());
        for (String key : keys) {
            String newValue = (String) newProperties.remove(key);
            Property property = properties.get(key);
            if (property == null) {
                log.error("property '{}' was added to {}... ignoring", key, uri);
            } else {
                property.updateValue(newValue);
            }
        }
        for (String missingKey : newProperties.stringPropertyNames()) {
            log.error("property '{}' was removed from {}... ignoring", missingKey, uri);
        }
    }

    @Override
    public void configure(ConfigurationPoint configPoint) {
        Property property = getProperty(configPoint);
        if (property == null)
            return;
        PropertyConfigValue configValue = new PropertyConfigValue(configPoint);
        property.add(configValue);
        configPoint.setConfigValue(configValue);
    }

    private Property getProperty(ConfigurationPoint configPoint) {
        return properties.get(configPoint.name());
    }

    @Override
    public String toString() {
        return properties.size() + " properties from " + uri;
    }

    Set<String> propertyNames() {
        return properties.keySet();
    }

    String removeProperty(String key) {
        Property removed = properties.remove(key);
        return (removed == null) ? null : removed.value;
    }

    @Override
    public void shutdown() {
        if (isFileScheme(uri)) {
            FILE_MONITOR.remove(Paths.get(uri));
        }
    }
}
