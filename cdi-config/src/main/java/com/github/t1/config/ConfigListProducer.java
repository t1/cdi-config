package com.github.t1.config;

import static java.util.stream.Collectors.*;

import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class ConfigListProducer {
    @Inject
    ConfigCdiExtension extension;

    @Produces
    public List<Config> produceConfigs() {
        return extension.configPoints()
                .values().stream()
                .map(configPoint -> configPoint.config())
                .collect(toList());
    }
}
