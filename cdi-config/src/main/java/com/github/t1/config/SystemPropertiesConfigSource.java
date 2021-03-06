package com.github.t1.config;

import static java.util.concurrent.TimeUnit.*;

import java.util.*;
import java.util.concurrent.*;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemPropertiesConfigSource implements ConfigSource {
    private static int nextInstance = 0;

    class SystemPropertiesConfigValue extends ConfigValue {
        private String lastStringValue;

        public SystemPropertiesConfigValue(String name) {
            super(name);
            this.lastStringValue = stringValue();
        }

        @Override
        protected <T> T getValue(Class<T> type) {
            String value = stringValue();
            return convert(value, type);
        }

        private String stringValue() {
            return System.getProperty(getName());
        }

        @Override
        public String toString() {
            return "system property " + getName();
        }

        public boolean hasChanged() {
            return !Objects.equals(lastStringValue, stringValue());
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        @Override
        public void writeValue(String value) {
            System.setProperty(getName(), value);
        }
    }

    private final int instance = nextInstance++;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private List<SystemPropertiesConfigValue> systemPropertiesConfigValues = new ArrayList<>();

    public SystemPropertiesConfigSource() {
        log.debug("start executor service {}", instance);
        executor.scheduleWithFixedDelay(new Runnable() {
            private int count;

            @Override
            public void run() {
                Thread currentThread = Thread.currentThread();
                String origThreadName = currentThread.getName();
                try {
                    currentThread.setName("system-property-update-checker-" + instance);
                    log.trace("run {}-{}", instance, count++);
                    checkSystemPropertyChanges();
                } finally {
                    currentThread.setName(origThreadName);
                }
            }

            private void checkSystemPropertyChanges() {
                for (SystemPropertiesConfigValue systemPropertiesConfigValue : systemPropertiesConfigValues)
                    if (systemPropertiesConfigValue.hasChanged())
                        systemPropertiesConfigValue.fireUpdate();
            }
        }, 0, 1, SECONDS);
    }

    @Override
    @SneakyThrows(InterruptedException.class)
    public void shutdown() {
        log.debug("shutting down executor service {}", instance);
        executor.shutdown();
        executor.awaitTermination(10, SECONDS);
        log.debug("executor service {} is terminated", instance);
    }

    protected SystemPropertiesConfigValue createConfigValueFor(ConfigPoint configPoint) {
        return new SystemPropertiesConfigValue(configPoint.name());
    }

    @Override
    public void configure(ConfigPoint configPoint) {
        SystemPropertiesConfigValue configValue = new SystemPropertiesConfigValue(configPoint.name());
        systemPropertiesConfigValues.add(configValue);
        configPoint.configureTo(configValue);
    }
}
