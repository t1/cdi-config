package com.github.t1.config;

import lombok.Getter;

public class PropertyConfigValue extends ConfigValue {
    private final String stringValue;
    @Getter
    private final String configSourceInfo;

    public PropertyConfigValue(ConfigurationPoint configPoint, String stringValue, String configSourceInfo) {
        super(configPoint);
        this.stringValue = stringValue;
        this.configSourceInfo = configSourceInfo;
    }

    @Override
    public Object getValue() {
        return convert(configPoint().type(), stringValue);
    }
}
