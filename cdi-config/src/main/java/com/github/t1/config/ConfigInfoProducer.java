package com.github.t1.config;

import static com.github.t1.config.ConfigInfo.*;
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
        return ConfigInfo.builder()
                .name(configPoint.name())
                .description(configPoint.description())
                .defaultValue(configPoint.defaultValue().orElse(null))
                .value(configPoint.configValue().getValue())
                .type(configPoint.type())
                .container(configPoint.container())
                .meta(toJson(configPoint.meta()))
                .build();
    }

    public static JsonObject toJson(String meta) {
        return (meta.isEmpty()) ? EMPTY_JSON_OBJECT
                : Json.createReader(new StringReader(meta.replace('\'', '\"'))).readObject();
    }
}
