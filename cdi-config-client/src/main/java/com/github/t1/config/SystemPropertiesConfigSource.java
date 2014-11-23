package com.github.t1.config;

import static java.util.concurrent.TimeUnit.*;

import java.util.*;
import java.util.concurrent.*;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.github.t1.config.ConfigurationPoint.UpdatableConfigValue;

@Slf4j
public class SystemPropertiesConfigSource implements ConfigSource {
    private static int nextInstance = 0;

    private class SystemPropertiesConfigValue extends UpdatableConfigValue {
        private final String value;

        public SystemPropertiesConfigValue(ConfigurationPoint configPoint) {
            configPoint.super();
            this.value = stringValue();
        }

        @Override
        protected Object getValue() {
            String value = stringValue();
            return convert(value);
        }

        private String stringValue() {
            String key = configPoint().name();
            String value = System.getProperty(key);
            return value;
        }

        @Override
        public String toString() {
            return super.toString() + " from system properties";
        }

        public void checkForUpdate() {
            if (!Objects.equals(value, stringValue())) {
                updateAllConfigTargets();
            }
        }
    }

    private final int instance = nextInstance++;
    private final Map<String, SystemPropertiesConfigValue> map = new HashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public SystemPropertiesConfigSource() {
        log.debug("start executor service {}", instance);
        executor.scheduleWithFixedDelay(new Runnable() {
            private int count;

            @Override
            public void run() {
                log.trace("run {}-{}", instance, count++);

                for (SystemPropertiesConfigValue watcher : map.values()) {
                    watcher.checkForUpdate();
                }
            }
        }, 0, 1, SECONDS);
    }

    @Override
    public void configure(ConfigurationPoint configPoint) {
        SystemPropertiesConfigValue configValue = configValueFor(configPoint);
        if (configValue.getValue() != null) {
            configPoint.setConfigValue(configValue);
        }
    }

    private SystemPropertiesConfigValue configValueFor(ConfigurationPoint configPoint) {
        SystemPropertiesConfigValue value = map.get(configPoint.name());
        if (value == null) {
            value = new SystemPropertiesConfigValue(configPoint);
            map.put(configPoint.name(), value);
        }
        return value;
        // TODO the shade plugin seems to misunderstand this:
        // return map.computeIfAbsent(configPoint.name(), (c) -> new SystemPropertiesConfigValue(configPoint));
    }

    @Override
    @SneakyThrows(InterruptedException.class)
    public void shutdown() {
        log.debug("shutting down executor service {}", instance);
        executor.shutdown();
        executor.awaitTermination(10, SECONDS);
        log.debug("executor service {} is terminated", instance);
    }
}
