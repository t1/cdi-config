package com.github.t1.config;

import static com.github.t1.config.Config.*;

import java.lang.reflect.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class StandardConfigurationPoint extends ConfigurationPoint {
    public StandardConfigurationPoint(Field field) {
        super(field);
        if (!Modifier.isVolatile(field.getModifiers())) {
            log.warn("the field {} is not volatile. see " + WIKI.resolve("Field-Not-Volatile"), field);
        }
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
