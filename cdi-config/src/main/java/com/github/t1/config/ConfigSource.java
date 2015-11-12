package com.github.t1.config;

/** The source of configs, i.e. files, system properties, etc. */
public interface ConfigSource {
    /** Apply the config for that {@link ConfigPoint}. */
    public void configure(ConfigPoint configPoint);

    /** Do cleanup necessary for this {@link ConfigSource}, e.g. stop watching for file changes. */
    public default void shutdown() {}
}
