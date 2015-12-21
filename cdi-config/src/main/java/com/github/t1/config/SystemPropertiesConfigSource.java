package com.github.t1.config;

import static java.util.concurrent.TimeUnit.*;

import java.util.*;
import java.util.concurrent.*;

import com.github.t1.config.ConfigPoint.UpdatableConfigValue;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemPropertiesConfigSource implements ConfigSource {
    private static int nextInstance = 0;

    class SystemPropertiesConfigValue extends UpdatableConfigValue {
        private String lastStringValue;

        public SystemPropertiesConfigValue(String name, ConfigPoint configPoint) {
            configPoint.super(name);
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
        public synchronized void writeValue(String value) {
            // FIXME this.lastStringValue = value;
            System.setProperty(getName(), value);
            // FIXME super.updateAllConfigTargets();
        }
    }

    private final int instance = nextInstance++;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private List<SystemPropertiesConfigValue> mapValues = new ArrayList<>();

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
                for (SystemPropertiesConfigValue watcher : mapValues)
                    if (watcher.hasChanged())
                        watcher.updateAllConfigTargets();
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
        return new SystemPropertiesConfigValue(configPoint.name(), configPoint);
    }

    @Override
    public void configure(ConfigPoint configPoint) {
        SystemPropertiesConfigValue configValue = new SystemPropertiesConfigValue(configPoint.name(), configPoint);
        if (configValue.getValue(configPoint.type()) == null)
            return;
        mapValues.add(configValue);
        configPoint.configValue(configValue);
    }
}
