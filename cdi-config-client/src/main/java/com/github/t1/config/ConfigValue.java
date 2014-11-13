package com.github.t1.config;

import lombok.AllArgsConstructor;

import org.joda.convert.StringConvert;

@AllArgsConstructor
public abstract class ConfigValue {
    private static final StringConvert STRING_CONVERT = StringConvert.INSTANCE;

    private final ConfigurationPoint configPoint;

    protected ConfigurationPoint configPoint() {
        return configPoint;
    }

    protected <T> T convert(Class<T> type, String value) {
        return STRING_CONVERT.convertFromString(type, value);
    }

    protected abstract Object getValue();

    /** do *not* log the value to configure... could be, e.g., a password */
    public abstract String getConfigSourceInfo();

    public void addConfigTartet(Object target) {
        configPoint.set(target, getValue());
    }
}