package com.github.t1.config;

import java.lang.reflect.Field;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class StandardConfigPoint extends ConfigPoint {
    public StandardConfigPoint(Field field) {
        super(field);
    }

    @Override
    protected Class<?> type() {
        return getField().getType();
    }

    @Override
    public void set(Object target, Object value) {
        ConfigChangeDelayInterceptor.run(target, () -> {
            log.debug("configure {}", StandardConfigPoint.this);
            setField(target, value);
        });
    }
}
