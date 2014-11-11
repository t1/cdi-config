package com.github.t1.config;

import java.lang.reflect.Field;

public interface ConfigSource {
    public boolean canConfigure(Field field);

    public Object getValueFor(Field field);
}
