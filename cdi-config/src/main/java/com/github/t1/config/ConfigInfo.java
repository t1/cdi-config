package com.github.t1.config;

import static lombok.AccessLevel.*;

import javax.json.*;

import lombok.*;

@Getter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor(access = PRIVATE)
public class ConfigInfo {
    public static final JsonObject EMPTY_JSON_OBJECT = Json.createObjectBuilder().build();

    public static ConfigInfoBuilder config(String name) {
        return builder().name(name);
    }

    public static class ConfigInfoBuilder {
        private JsonObject meta = EMPTY_JSON_OBJECT;
    }

    @NonNull
    private final String name;

    private String description;
    private String defaultValue;
    private Object value;
    private Class<?> type;
    private Class<?> container;
    @NonNull
    private JsonObject meta;

    public void updateTo(String value) {
        this.value = value;
    }
}
