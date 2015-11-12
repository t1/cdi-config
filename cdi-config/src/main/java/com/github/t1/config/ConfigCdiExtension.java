package com.github.t1.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
            log.trace("add config targets in {}", instance);
            for (ConfigurationPoint configurationPoint : configs) {
                log.trace("add target to config point {}", configurationPoint);
                configurationPoint.addConfigTarget(instance);
            }
            log.trace("done adding config targets in {}", instance);

            super.inject(instance, ctx);
        }

        @Override
        public void preDestroy(T instance) {
            super.preDestroy(instance);

            log.trace("remove config targets in {}", instance);
            for (ConfigurationPoint configurationPoint : configs) {
                log.trace("remove from config point {}", configurationPoint);
                configurationPoint.removeConfigTarget(instance);
            }
            log.trace("done removing config targets in {}", instance);
        }
    }

    private ConfigSource configSource;

    private ConfigSource configSource() {
        if (configSource == null)
            configSource = new ConfigSourceLoader().load();
        return configSource;
    }

    public <T> void processAnnotatedType(@Observes @WithAnnotations(Config.class) ProcessAnnotatedType<T> pat) {
        Class<T> javaClass = pat.getAnnotatedType().getJavaClass();
        if (javaClass.isInterface())
            return;
        if (!hasUnsafeConfig(javaClass)) {
            log.trace("all safe in {}", pat.getAnnotatedType().getBaseType());
            return;
        }
        addAnnotation(ConfiguredBean.class, pat);
    }

    private <T> void addAnnotation(Class<? extends Annotation> annotationType, ProcessAnnotatedType<T> pat) {
        log.debug("add {} annotation to {}", annotationType.getSimpleName(), pat.getAnnotatedType().getBaseType());
        Set<Annotation> annotations = new HashSet<>();
        annotations.addAll(pat.getAnnotatedType().getAnnotations());
        annotations.add(() -> annotationType);
        pat.setAnnotatedType(new AnnotatedTypeWrapper<T>(pat.getAnnotatedType()) {
            @Override
            public Set<Annotation> getAnnotations() {
                return annotations;
            }
        });
    }

    private boolean hasUnsafeConfig(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Config.class) && !isSafe(field)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSafe(Field field) {
        return Modifier.isVolatile(field.getModifiers()) || AtomicReference.class.equals(field.getType());
    }

    public <T> void processInjectionTarget(@Observes ProcessInjectionTarget<T> pit) {
        Class<T> type = pit.getAnnotatedType().getJavaClass();
        log.trace("scan {} for configuration points", type);

        List<ConfigurationPoint> configs = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            try {
                ConfigurationPoint configPoint = ConfigurationPoint.on(field);
                if (configPoint != null) {
                    log.debug("found config point {}", configPoint);
                    configSource().configure(configPoint);
                    if (!configPoint.isConfigured()) {
                        String message = "no config value found for " + configPoint + " and no default value specified";
                        if (!configPoint.description().isEmpty())
                            message += "\n  [" + configPoint.description() + "]";
                        log.error(message);
                        // TODO CDI 1.1: use DefinitionException
                        throw new RuntimeException(message);
                    }
                    configs.add(configPoint);
                }
            } catch (RuntimeException e) {
                pit.addDefinitionError(e);
            }
        }

        if (!configs.isEmpty()) {
            InjectionTarget<T> it = pit.getInjectionTarget();
            log.debug("found {} config points in {}", configs.size(), pit.getAnnotatedType());
            pit.setInjectionTarget(new ConfiguringInjectionTarget<>(it, configs));
        }
    }

    public void beforeShutdown(@SuppressWarnings("unused") @Observes BeforeShutdown beforeShutdown) {
        log.debug("shutdown start");
        if (configSource != null)
            configSource.shutdown();
        log.debug("shutdown done");
    }
}
