package com.github.t1.config;

import lombok.*;

@Getter
@AllArgsConstructor
public class PropertyConfigValue implements ConfigValue {
    private final Object value;
    private final String propertyName;

    @Override
    public String getConfigSourceInfo() {
        return "property '" + propertyName + "' from properties config source";
    }
}
