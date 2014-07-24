package com.github.t1.config;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.*;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class InjectionTargetWrapper<X> implements InjectionTarget<X> {
    private final InjectionTarget<X> delegate;

    @Override
    public X produce(CreationalContext<X> ctx) {
        return delegate.produce(ctx);
    }

    @Override
    public void dispose(X instance) {
        delegate.dispose(instance);
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return delegate.getInjectionPoints();
    }

    @Override
    public void inject(X instance, CreationalContext<X> ctx) {
        delegate.inject(instance, ctx);
    }

    @Override
    public void postConstruct(X instance) {
        delegate.postConstruct(instance);
    }

    @Override
    public void preDestroy(X instance) {
        delegate.preDestroy(instance);
    }
}
