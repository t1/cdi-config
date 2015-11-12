package com.github.t1.config;

import static javax.interceptor.Interceptor.Priority.*;

import java.util.*;

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
        synchronized (target) {
            ConfigChangeDelayInterceptor interceptor = INTERCEPTORS.get(target);
            if (interceptor == null) {
                log.debug("direct apply");
                runnable.run();
            } else {
                interceptor.run(runnable);
            }
        }
    }

    private int counter = 0;
    private Runnable action = null;

    private void run(Runnable runnable) {
        if (counter > 0) {
            log.debug("delay apply");
            this.action = runnable; // only the latest
        } else {
            log.debug("immediate apply");
            runnable.run();
        }
    }

    @AroundInvoke
    public Object aroundInvoke(InvocationContext context) throws Exception {
        acquire(context.getTarget());
        try {
            return context.proceed();
        } finally {
            release(context.getTarget());
        }
    }

    private void acquire(Object target) {
        synchronized (target) {
            INTERCEPTORS.put(target, this); // it's probably cheaper to just overwrite
            ++counter;
        }
    }

    private synchronized void release(Object target) {
        synchronized (target) {
            if (--counter <= 0) {
                Runnable runnable = this.action;
                this.action = null;
                if (runnable != null) {
                    log.debug("delayed apply");
                    runnable.run();
                }
            }
        }
    }
}
