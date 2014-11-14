package com.github.t1.config;

import java.lang.reflect.Field;
import java.util.*;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigCdiExtension implements Extension {
    private static class ConfiguringInjectionTarget<T> extends InjectionTargetWrapper<T> {
        private final List<ConfigurationPoint> configs;

        private ConfiguringInjectionTarget(InjectionTarget<T> delegate, List<ConfigurationPoint> configs) {
            super(delegate);
            this.configs = configs;
        }

        @Override
        public void inject(T instance, CreationalContext<T> ctx) {
            log.debug("add config targets in {}", instance);

            for (ConfigurationPoint configurationPoint : configs) {
                configurationPoint.addConfigTarget(instance);
            }

            super.inject(instance, ctx);
        }

        @Override
        public void preDestroy(T instance) {
            super.preDestroy(instance);

            log.debug("remove config target in {}", instance);

            for (ConfigurationPoint configurationPoint : configs) {
                configurationPoint.removeConfigTarget(instance);
            }
        }
    }

    private ConfigSource configSource;

    private ConfigSource configSource() {
        if (configSource == null)
            configSource = new ConfigSourceLoader().load();
        return configSource;
    }

    public <T> void processInjectionTarget(@Observes ProcessInjectionTarget<T> pit) {
        Class<T> type = pit.getAnnotatedType().getJavaClass();
        log.trace("scan {} for configuration points", type);

        List<ConfigurationPoint> configs = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            try {
                ConfigurationPoint configPoint = ConfigurationPoint.on(field);
                if (configPoint != null) {
                    configSource().configure(configPoint);
                    if (!configPoint.isConfigured()) {
                        log.error("can't configure {}", configPoint);
                        throw new DefinitionException("no config value found for " + configPoint);
                    }
                    configs.add(configPoint);
                }
            } catch (DefinitionException e) {
                pit.addDefinitionError(e);
            }
        }

        if (!configs.isEmpty()) {
            InjectionTarget<T> it = pit.getInjectionTarget();
            pit.setInjectionTarget(new ConfiguringInjectionTarget<>(it, configs));
        }
    }
}
