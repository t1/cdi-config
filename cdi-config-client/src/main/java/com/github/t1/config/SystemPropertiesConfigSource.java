package com.github.t1.config;

import static java.util.concurrent.TimeUnit.*;

import java.util.Objects;
import java.util.concurrent.*;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import com.github.t1.config.ConfigurationPoint.UpdatableConfigValue;
import com.github.t1.config.SystemPropertiesConfigSource.SystemPropertiesConfigValue;

@Slf4j
public class SystemPropertiesConfigSource extends MapConfigSource<SystemPropertiesConfigValue> {
    private static int nextInstance = 0;

    class SystemPropertiesConfigValue extends UpdatableConfigValue {
        private final String lastStringValue;

        public SystemPropertiesConfigValue(ConfigurationPoint configPoint) {
            configPoint.super();
            this.lastStringValue = stringValue();
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
            if (!Objects.equals(lastStringValue, stringValue())) {
                updateAllConfigTargets();
            }
        }
    }

    private final int instance = nextInstance++;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public SystemPropertiesConfigSource() {
        log.debug("start executor service {}", instance);
        executor.scheduleWithFixedDelay(new Runnable() {
            private int count;

            @Override
            public void run() {
                log.trace("run {}-{}", instance, count++);

                for (SystemPropertiesConfigValue watcher : mapValues()) {
                    watcher.checkForUpdate();
                }
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

    @Override
    protected SystemPropertiesConfigValue createConfigValueFor(ConfigurationPoint configPoint) {
        return new SystemPropertiesConfigValue(configPoint);
    }
}
