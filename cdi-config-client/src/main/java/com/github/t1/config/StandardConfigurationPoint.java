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
    public void configure(Object target) {
        set(target, value());
    }

    @Override
    public void deconfigure(Object target) {
        set(target, nullValue());
    }

    private Object nullValue() {
        if (Boolean.class.equals(type()))
            return false;
        if (isInteger())
            return 0;
        if (isFloating())
            return 0.0;
        return null;
    }

    private boolean isInteger() {
        return Byte.class.equals(type()) || Character.class.equals(type()) //
                || Short.class.equals(type()) || Integer.class.equals(type()) || Long.class.equals(type());
    }

    private boolean isFloating() {
        return Float.class.equals(type()) || Double.class.equals(type());
    }
}
