package com.github.t1.config;

import java.util.*;

public abstract class UpdatableConfigValue extends ConfigValue {
    private final List<Object> targets = new ArrayList<>();

    public UpdatableConfigValue(ConfigurationPoint configPoint) {
        super(configPoint);
    }

    @Override
    public void addConfigTartet(Object target) {
        this.targets.add(target);
        configPoint().set(target, getValue());
    }

    @Override
    public void removeConfigTartet(Object target) {
        targets.remove(target);
    }

    protected abstract Object getValue();

    public void updateAllConfigTargets() {
        Object value = getValue();
        for (Object target : targets) {
            configPoint().set(target, value);
        }
    }
}
