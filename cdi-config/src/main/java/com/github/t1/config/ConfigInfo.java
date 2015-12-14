package com.github.t1.config;

import static lombok.AccessLevel.*;

import javax.json.JsonObject;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor(access = PRIVATE)
public class ConfigInfo {
    private final String name;
    private final String description;
    private final String defaultValue;
    private Object value;
    private Class<?> type;
    private Class<?> container;
    private JsonObject meta;
}
