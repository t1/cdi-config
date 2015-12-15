package com.github.t1.config;

import java.util.function.Predicate;

import javax.json.JsonObject;

import lombok.NonNull;

public interface ConfigInfo {
    public static Predicate<? super ConfigInfo> byName(String key) {
        return config -> config.getName().equals(key);
    }

    String getName();

    String getDescription();

    String getDefaultValue();

    Object getValue();

    Class<?> getType();

    Class<?> getContainer();

    @NonNull
    JsonObject getMeta();

    public void updateTo(String value);
}
