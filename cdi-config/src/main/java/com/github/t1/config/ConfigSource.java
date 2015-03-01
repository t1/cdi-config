package com.github.t1.config;

/**
 * TODO document
 */
public interface ConfigSource {
    /**
     * TODO document
     */
    public void configure(ConfigurationPoint configPoint);

    /**
     * TODO document
     */
    public default void shutdown() {}
}
