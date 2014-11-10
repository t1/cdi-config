package com.github.t1.config;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.inject.InjectionException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class AtomicReferenceConfigurationPoint implements ConfigurationPoint {
    private final Field field;
    private final Object value;
    private Object oldValue;

    @Override
    public void configure(Object target) {
        try {
            oldValue = field.get(target);
            AtomicReference<Object> ref = ref(target);
            ref.set(value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InjectionException("can't set " + field + " to a " + value.getClass().getSimpleName(), e);
        }
    }

    private AtomicReference<Object> ref(Object target) throws IllegalAccessException {
        @SuppressWarnings("unchecked")
        AtomicReference<Object> ref = (AtomicReference<Object>) field.get(target);
        if (ref == null) {
            ref = new AtomicReference<>();
            field.set(target, ref);
        }
        return ref;
    }

    @Override
    public void deconfigure(Object target) {
        try {
            field.set(target, oldValue);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InjectionException("can't set " + field + " to old value", e);
        }
    }
}
