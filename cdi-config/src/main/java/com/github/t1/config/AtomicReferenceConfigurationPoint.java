package com.github.t1.config;

import java.lang.reflect.*;
import java.util.concurrent.atomic.AtomicReference;

class AtomicReferenceConfigurationPoint extends ConfigurationPoint {
    public AtomicReferenceConfigurationPoint(Field field) {
        super(field);
    }

    @Override
    protected Class<?> type() {
        return (Class<?>) ((ParameterizedType) getField().getGenericType()).getActualTypeArguments()[0];
    }

    @Override
    public void set(Object target, Object value) {
        ref(target).set(value);
    }

    private AtomicReference<Object> ref(Object target) {
        @SuppressWarnings("unchecked")
        AtomicReference<Object> ref = (AtomicReference<Object>) getField(target);
        if (ref == null) {
            ref = new AtomicReference<>();
            setField(target, ref);
        }
        return ref;
    }

    @Override
    public void removeConfigTarget(Object target) {
        setField(target, null);
    }
}
