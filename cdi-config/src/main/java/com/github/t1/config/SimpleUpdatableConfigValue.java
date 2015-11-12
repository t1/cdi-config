package com.github.t1.config;

import lombok.Getter;

import com.github.t1.config.ConfigPoint.UpdatableConfigValue;

public class SimpleUpdatableConfigValue extends UpdatableConfigValue {
    public SimpleUpdatableConfigValue(ConfigPoint configPoint, Object value) {
        configPoint.super();
        this.value = value;
    }

    @Getter
    private Object value;

    public void setValue(Object value) {
        this.value = value;
        updateAllConfigTargets();
    }
}
