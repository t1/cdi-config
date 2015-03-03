package com.github.t1.config;

import java.lang.reflect.Field;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        ConfigChangeDelayInterceptor.run(target, new Runnable() {
            @Override
            public void run() {
                log.debug("configure {}", StandardConfigurationPoint.this);
                setField(target, value);
            }
        });
    }
}
