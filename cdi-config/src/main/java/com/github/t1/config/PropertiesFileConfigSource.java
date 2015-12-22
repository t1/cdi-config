package com.github.t1.config;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertiesFileConfigSource implements ConfigSource {
    private class PropertyConfigValue extends ConfigValue {
        private String value;

        public PropertyConfigValue(String name, String value) {
            super(name);
            this.value = value;
        }

        @Override
        protected <T> T getValue(Class<T> type) {
            return convert(value, type);
        }

        @Override
        public String toString() {
            return "property '" + getName() + "' from " + uri;
        }

        @Override
        public boolean isWritable() {
            return isFileScheme(uri) && Files.isWritable(path());
        }

        @Override
        public void writeValue(String value) {
            this.value = value;
            save();
            fireUpdate();
        }
    }

    private final URI uri;
    private final Map<String, PropertyConfigValue> properties;

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

    private Path path() {
        return Paths.get(uri);
    }

    private void save() {
        Properties result = toProperties(this.properties);
        try {
            result.store(Files.newBufferedWriter(path()), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Properties toProperties(Map<String, PropertyConfigValue> map) {
        Properties result = new Properties();
        map.forEach((key, value) -> result.put(key, value.value));
        return result;
    }

    private static Path subpath(Path path, int beginIndex) {
        return path.subpath(beginIndex, path.getNameCount());
    }

    private Map<String, PropertyConfigValue> toPropertyMap(Properties in) {
        Map<String, PropertyConfigValue> out = new LinkedHashMap<>();
        in.stringPropertyNames().forEach(
                name -> out.put(name, new PropertyConfigValue(name, in.getProperty(name))));
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
            PropertyConfigValue property = properties.get(key);
            if (property == null) {
                log.error("property '{}' was added to {}... ignoring", key, uri);
            } else {
                property.value = newValue;
                property.fireUpdate();
            }
        }
        for (String missingKey : newProperties.stringPropertyNames()) {
            log.error("property '{}' was removed from {}... ignoring", missingKey, uri);
        }
    }

    @Override
    public void configure(ConfigPoint configPoint) {
        String name = configPoint.name();
        PropertyConfigValue configValue = properties.get(name);
        if (configValue == null)
            return;
        configPoint.configureTo(configValue);
    }

    @Override
    public String toString() {
        return properties.size() + " properties from " + uri;
    }

    Set<String> propertyNames() {
        return properties.keySet();
    }

    String removeProperty(String key) {
        PropertyConfigValue removed = properties.remove(key);
        return (removed == null) ? null : removed.value;
    }

    @Override
    public void shutdown() {
        if (isFileScheme(uri)) {
            FILE_MONITOR.remove(path());
        }
    }
}
