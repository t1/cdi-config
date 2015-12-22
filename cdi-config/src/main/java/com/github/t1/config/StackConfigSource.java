package com.github.t1.config;

import java.util.*;

class StackConfigSource implements ConfigSource {
    public static ConfigSource of(ConfigSource... sources) {
        List<ConfigSource> sourceList = new ArrayList<>(Arrays.asList(sources));

        StackConfigSource stack = (sourceList.get(0) instanceof StackConfigSource) //
                ? (StackConfigSource) sourceList.remove(0) //
                : new StackConfigSource();

        for (ConfigSource source : sourceList)
            stack.add(source);
        return stack;
    }

    private final List<ConfigSource> sources = new ArrayList<>();

    private void add(ConfigSource source) {
        if (source instanceof StackConfigSource) {
            StackConfigSource subStack = (StackConfigSource) source;
            this.sources.addAll(subStack.sources);
        } else {
            this.sources.add(source);
        }
    }

    @Override
    public void configure(ConfigPoint configPoint) {
        for (ConfigSource source : sources) {
            if (configPoint.isConfigured())
                break;
            source.configure(configPoint);
        }
    }

    @Override
    public String toString() {
        return "stack:" + sources;
    }

    @Override
    public void shutdown() {
        for (ConfigSource source : sources) {
            source.shutdown();
        }
    }
}
