package com.github.t1.config;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

class AtomicReferenceConfigurationPoint extends ConfigurationPoint {
    public AtomicReferenceConfigurationPoint(Field field) {
        super(field);
    }

    @Override
    public void configure(Object target) {
        ref(target).set(value());
    }

    private AtomicReference<Object> ref(Object target) {
        @SuppressWarnings("unchecked")
        AtomicReference<Object> ref = (AtomicReference<Object>) get(target);
        if (ref == null) {
            ref = new AtomicReference<>();
            set(target, ref);
        }
        return ref;
    }

    @Override
    public void deconfigure(Object target) {
        set(target, null);
    }
}
