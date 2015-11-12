package com.github.t1.config;

import java.util.*;

class MultiConfigSource implements ConfigSource {
    public static ConfigSource of(ConfigSource... sources) {
        List<ConfigSource> sourceList = new ArrayList<>(Arrays.asList(sources));

        MultiConfigSource multi = (sourceList.get(0) instanceof MultiConfigSource) //
                ? (MultiConfigSource) sourceList.remove(0) //
                : new MultiConfigSource();

        for (ConfigSource source : sourceList)
            multi.add(source);
        return multi;
    }

    private final List<ConfigSource> sources = new ArrayList<>();

    private void add(ConfigSource source) {
        if (source instanceof MultiConfigSource) {
            MultiConfigSource subMulti = (MultiConfigSource) source;
            this.sources.addAll(subMulti.sources);
        } else {
            this.sources.add(source);
        }
    }

    @Override
    public void configure(ConfigurationPoint configPoint) {
        for (ConfigSource source : sources) {
            if (configPoint.isConfigured())
                break;
            source.configure(configPoint);
        }
    }

    @Override
    public String toString() {
        return "multi:" + sources;
    }

    @Override
    public void shutdown() {
        for (ConfigSource source : sources) {
            source.shutdown();
        }
    }
}
