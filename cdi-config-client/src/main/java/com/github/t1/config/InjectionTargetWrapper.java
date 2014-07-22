package com.github.t1.config;

import javax.enterprise.inject.spi.InjectionTarget;

import lombok.*;

@AllArgsConstructor
public abstract class InjectionTargetWrapper<X> implements InjectionTarget<X> {
    @Delegate
    private final InjectionTarget<X> delegate;
}
