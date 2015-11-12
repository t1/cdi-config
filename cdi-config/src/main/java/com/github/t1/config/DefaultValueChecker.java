package com.github.t1.config;

import java.util.*;

/**
 * Checks that all {@link Config#defaultValue}s are the same. This has to be done first, so even if a value is
 * configured, a mismatch in the default value is reported.
 */
public class DefaultValueChecker {
    private final Map<String, ConfigurationPoint> defaultValues = new HashMap<>();

    public void check(ConfigurationPoint configPoint) {
        String configValue = configPoint.defaultValue().orElse("");
        String name = configPoint.name();
        ConfigurationPoint existingConfigPoint = defaultValues.get(name);
        if (existingConfigPoint == null) {
            defaultValues.put(name, configPoint);
        } else {
            String existingValue = existingConfigPoint.defaultValue().orElse("");
            if (!existingValue.equals(configValue))
                // TODO CDI 1.1: use DefinitionException
                throw new RuntimeException("default value mismatch:\n" //
                        + ": " + existingConfigPoint + " -> '" + existingValue + "'\n" //
                        + ": " + configPoint + " -> '" + configValue + "'");
        }
    }
}
