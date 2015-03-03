package com.github.t1.config;

/** The source of configurations, i.e. files, system properties, etc. */
public interface ConfigSource {
    /** Apply the configuration for that {@link ConfigurationPoint}. */
    public void configure(ConfigurationPoint configPoint);

    /** Do cleanup necessary for this {@link ConfigSource}, e.g. stop watching for file changes. */
    public default void shutdown() {}
}
