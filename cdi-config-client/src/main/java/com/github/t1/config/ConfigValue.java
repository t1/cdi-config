package com.github.t1.config;

import lombok.RequiredArgsConstructor;

import org.joda.convert.StringConvert;

@RequiredArgsConstructor
public abstract class ConfigValue {
    private static final StringConvert STRING_CONVERT = StringConvert.INSTANCE;

    private final ConfigurationPoint configPoint;

    protected ConfigurationPoint configPoint() {
        return configPoint;
    }

    protected Object convert(String value) {
        return STRING_CONVERT.convertFromString(configPoint.type(), value);
    }

    public abstract void addConfigTartet(Object target);

    public abstract void removeConfigTartet(Object target);

    /**
     * Do <em>not</em> produce the actual value... could be, e.g., a password. <br/>
     * And take care to not recurse into {@link ConfigurationPoint#toString()}
     */
    @Override
    public String toString() {
        return "config value for '" + configPoint.name() + "'";
    }
}
