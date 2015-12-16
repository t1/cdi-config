package com.github.t1.config;

import com.github.t1.config.ConfigPoint.UpdatableConfigValue;

import lombok.Getter;

public class InMemoryConfigValue extends UpdatableConfigValue {
    public InMemoryConfigValue(ConfigPoint configPoint, Object value) {
        configPoint.super();
        this.value = value;
    }

    @Getter
    private Object value;

    @Override
    public void writeValue(Object value) {
        this.value = value;
        updateAllConfigTargets();
    }

    @Override
    public boolean isWritable() {
        return true;
    }
}
