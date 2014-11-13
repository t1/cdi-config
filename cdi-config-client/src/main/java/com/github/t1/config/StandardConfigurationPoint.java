package com.github.t1.config;

import java.lang.reflect.Field;

class StandardConfigurationPoint extends ConfigurationPoint {
    public StandardConfigurationPoint(Field field) {
        super(field);
    }

    @Override
    protected Class<?> type() {
        return getField().getType();
    }

    @Override
    public void set(Object target, Object value) {
        setField(target, value);
    }
}
