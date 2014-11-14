package com.github.t1.config;

import lombok.Getter;

public class SimpleUpdatableConfigValue extends UpdatableConfigValue {
    public SimpleUpdatableConfigValue(ConfigurationPoint configPoint, Object value) {
        super(configPoint);
        this.value = value;
    }

    @Getter
    private Object value;

    public void setValue(Object value) {
        this.value = value;
        updateAllConfigTargets();
    }
}