package com.github.t1.config;

import static java.util.stream.Collectors.*;

import java.io.StringReader;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.json.*;

public class ConfigInfoProducer {
    @Inject
    ConfigCdiExtension extension;

    @Produces
    public List<ConfigInfo> produceConfigInfos() {
        return extension.configPoints()
                .values().stream()
                .map(configPoint -> toConfigInfo(configPoint))
                .collect(toList());
    }

    private ConfigInfo toConfigInfo(ConfigPoint configPoint) {
        return new ConfigInfo() {
            @Override
            public String getName() {
                return configPoint.name();
            }

            @Override
            public String getDescription() {
                return configPoint.description();
            }

            @Override
            public String getDefaultValue() {
                return configPoint.defaultValue().orElse(null);
            }

            @Override
            public Object getValue() {
                return configPoint.getValue();
            }

            @Override
            public Class<?> getType() {
                return configPoint.type();
            }

            @Override
            public Class<?> getContainer() {
                return configPoint.container();
            }

            @Override
            public JsonObject getMeta() {
                return toJson(configPoint.meta());
            }

            @Override
            public boolean isUpdatable() {
                return configPoint.isWritable();
            }

            @Override
            public void updateTo(String value) {
                configPoint.writeValue(value);
            }

            @Override
            public String toString() {
                return "ConfigInfo["
                        + getName()
                        + ((isEmpty(getDescription())) ? "" : ", description='" + getDescription() + "'")
                        + ((isEmpty(getDefaultValue())) ? "" : ", defaultValue='" + getDefaultValue() + "'")
                        + (", value='" + getValue() + "'")
                        + (", type=" + getType().getName())
                        + (", container=" + getContainer().getName())
                        + ((getMeta().isEmpty()) ? "" : ", meta=" + getMeta().toString().replace('\"', '\''))
                        + (", updatable=" + isUpdatable())
                        + "]";
            }

            private boolean isEmpty(String string) {
                return string == null || string.isEmpty();
            }
        };
    }

    public static JsonObject toJson(String meta) {
        return (meta.isEmpty()) ? Json.createObjectBuilder().build()
                : Json.createReader(new StringReader(meta.replace('\'', '\"'))).readObject();
    }
}
