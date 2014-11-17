package com.github.t1.config;

import java.util.*;

class MultiConfigSource implements ConfigSource {
    public static ConfigSource of(ConfigSource... sources) {
        List<ConfigSource> sourceList = new ArrayList<>(Arrays.asList(sources));
        for (ConfigSource source : sources) {
            if (source instanceof MultiConfigSource) {
                sourceList.remove(source);
                ((MultiConfigSource) source).add(sourceList);
                return source;
            }
        }
        MultiConfigSource multi = new MultiConfigSource();
        multi.add(sourceList);
        return multi;
    }

    private final List<ConfigSource> sources = new ArrayList<>();

    private void add(List<ConfigSource> sources) {
        for (ConfigSource source : sources) {
            if (source instanceof MultiConfigSource) {
                MultiConfigSource subMulti = (MultiConfigSource) source;
                this.sources.addAll(subMulti.sources);
            } else {
                this.sources.add(source);
            }
        }
    }

    @Override
    public void configure(ConfigurationPoint configPoint) {
        for (ConfigSource source : sources) {
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
