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
        private final List<ConfigPoint> configs;

        private ConfiguringInjectionTarget(InjectionTarget<T> delegate, List<ConfigPoint> configs) {
            super(delegate);
            this.configs = configs;
        }

        @Override
        public void inject(T instance, CreationalContext<T> ctx) {
            log.trace("add config targets in {}", instance);
            for (ConfigPoint configPoint : configs) {
                log.trace("add target to config point {}", configPoint);
                configPoint.addConfigTarget(instance);
            }
            log.trace("done adding config targets in {}", instance);

            super.inject(instance, ctx);
        }

        @Override
        public void preDestroy(T instance) {
            super.preDestroy(instance);

            log.trace("remove config targets in {}", instance);
            for (ConfigPoint configPoint : configs) {
                log.trace("remove from config point {}", configPoint);
                configPoint.removeConfigTarget(instance);
            }
            log.trace("done removing config targets in {}", instance);
        }
    }

    private final Map<String, ConfigPoint> configPoints = new HashMap<>();

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
        log.trace("scan {} for config points", type);

        List<ConfigPoint> configs = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            try {
                ConfigPoint configPoint = ConfigPoint.on(field);
                if (configPoint != null) {
                    log.debug("found config point {}", configPoint);
                    check(configPoint);
                    configSource().configure(configPoint);
                    if (!configPoint.isConfigured()) {
                        String message = "no config value found for " + configPoint + " and no default value specified";
                        if (!configPoint.description().isEmpty())
                            message += "\n  [" + configPoint.description() + "]";
                        log.error(message);
                        // TODO CDI 1.1: use DefinitionException. here and in the catch a few lines down
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

    /**
     * Checks that all {@link Config#defaultValue}s are the same. This has to be done before the first configSource is
     * consulted, so even if a value is configured, a mismatch in the default value is reported.
     */
    public void check(ConfigPoint configPoint) {
        String configValue = configPoint.defaultValue().orElse("");
        String name = configPoint.name();
        ConfigPoint existingConfigPoint = configPoints.get(name);
        if (existingConfigPoint == null) {
            configPoints.put(name, configPoint);
        } else {
            String existingValue = existingConfigPoint.defaultValue().orElse("");
            if (!existingValue.equals(configValue))
                // TODO CDI 1.1: use DefinitionException
                throw new RuntimeException("default value mismatch:\n" //
                        + ": " + existingConfigPoint + " -> '" + existingValue + "'\n" //
                        + ": " + configPoint + " -> '" + configValue + "'");
        }
    }

    public void beforeShutdown(@SuppressWarnings("unused") @Observes BeforeShutdown beforeShutdown) {
        log.debug("shutdown start");
        if (configSource != null)
            configSource.shutdown();
        log.debug("shutdown done");
    }
}
