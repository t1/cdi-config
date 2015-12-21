package com.github.t1.config;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import com.github.t1.config.ConfigPoint.UpdatableConfigValue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertiesFileConfigSource implements ConfigSource {
    private class PropertyConfigValue extends UpdatableConfigValue {
        public PropertyConfigValue(String name, ConfigPoint configPoint) {
            configPoint.super(name);
        }

        @Override
        protected <T> T getValue(Class<T> type) {
            Property property = properties.get(getName());
            String value = (property == null) ? null : property.value;
            return convert(value, type);
        }

        @Override
        public String toString() {
            return "property " + getName() + " from " + uri;
        }

        @Override
        public boolean isWritable() {
            return PropertiesFileConfigSource.this.isWritable();
        }

        @Override
        public void writeValue(String value) {
            PropertiesFileConfigSource.this.write(getName(), value);
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
            for (PropertyConfigValue config : configs)
                config.updateAllConfigTargets();
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

    public boolean isWritable() {
        return isFileScheme(uri) && Files.isWritable(path());
    }

    private boolean isFileScheme(URI uri) {
        return "file".equals(uri.getScheme());
    }

    private Path path() {
        return Paths.get(uri);
    }

    public void write(String name, String value) {
        properties.get(name).updateValue(value);
        save();
    }

    private void save() {
        Properties result = toProperties(this.properties);
        try {
            result.store(Files.newBufferedWriter(path()), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Properties toProperties(Map<String, Property> map) {
        Properties result = new Properties();
        map.forEach((key, value) -> result.put(key, value.value));
        return result;
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
            log.info("can't open properties uri " + uri + ": " + e + ". Fall back to empty properties.");
            return new Properties();
        }
    }

    private void initMonitor() {
        if (isFileScheme(uri)) {
            FILE_MONITOR.add(path(), new Runnable() {
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
    public void configure(ConfigPoint configPoint) {
        String name = configPoint.name();
        Property property = properties.get(name);
        if (property == null)
            return;
        PropertyConfigValue configValue = new PropertyConfigValue(name, configPoint);
        property.add(configValue);
        configPoint.configValue(configValue);
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
            FILE_MONITOR.remove(path());
        }
    }
}
