package com.github.t1.config;

import java.lang.reflect.Field;
import java.util.Optional;

public interface ConfigSource {
    public Optional<Object> getValueFor(Field field);
}
