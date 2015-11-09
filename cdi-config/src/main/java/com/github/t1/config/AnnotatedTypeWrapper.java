package com.github.t1.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.*;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AnnotatedTypeWrapper<T> implements AnnotatedType<T> {
    private final AnnotatedType<T> delegate;

    @Override
    public Type getBaseType() {
        return delegate.getBaseType();
    }

    @Override
    public Set<Type> getTypeClosure() {
        return delegate.getTypeClosure();
    }

    @Override
    public <U extends Annotation> U getAnnotation(Class<U> annotationType) {
        for (Annotation annotation : getAnnotations()) {
            if (annotation.annotationType().equals(annotationType)) {
                return annotationType.cast(annotation);
            }
        }
        return null;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return delegate.getAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }

    @Override
    public Class<T> getJavaClass() {
        return delegate.getJavaClass();
    }

    @Override
    public Set<AnnotatedConstructor<T>> getConstructors() {
        return delegate.getConstructors();
    }

    @Override
    public Set<AnnotatedMethod<? super T>> getMethods() {
        return delegate.getMethods();
    }

    @Override
    public Set<AnnotatedField<? super T>> getFields() {
        return delegate.getFields();
    }
}
