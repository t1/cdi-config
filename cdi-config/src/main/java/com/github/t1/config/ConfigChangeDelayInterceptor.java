package com.github.t1.config;

import static javax.interceptor.Interceptor.Priority.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import javax.annotation.Priority;
import javax.interceptor.*;

import lombok.extern.slf4j.Slf4j;

/** Takes care of delaying config updates to a bean until it's not being executed */
@Slf4j
@Interceptor
@ConfiguredBean
@Priority(LIBRARY_BEFORE)
public class ConfigChangeDelayInterceptor {
    private static final Map<Object, ConfigChangeDelayInterceptor> INTERCEPTORS = new WeakHashMap<>();

    public static void run(Object target, Runnable runnable) {
        ConfigChangeDelayInterceptor interceptor = INTERCEPTORS.get(target);
        if (interceptor == null) {
            log.debug("direct apply");
            runnable.run();
        } else {
            interceptor.run(runnable);
        }
    }

    private final AtomicInteger counter = new AtomicInteger();
    private final AtomicReference<Runnable> action = new AtomicReference<>();

    @AroundInvoke
    Object aroundInvoke(InvocationContext context) throws Exception {
        acquire(context.getTarget());
        try {
            return context.proceed();
        } finally {
            release();
        }
    }

    private void acquire(Object target) {
        INTERCEPTORS.put(target, this); // it's probably cheaper to just overwrite
        counter.incrementAndGet();
    }

    private void release() {
        if (counter.decrementAndGet() <= 0) {
            Runnable runnable = action.getAndSet(null);
            if (runnable != null) {
                log.debug("delayed apply");
                runnable.run();
            }
        }
    }

    private void run(Runnable runnable) {
        if (counter.get() > 0) {
            log.debug("delay apply");
            this.action.set(runnable); // only the latest
        } else {
            log.debug("immediate apply");
            runnable.run();
        }
    }
}
