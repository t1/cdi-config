package com.github.t1.config;

import static org.assertj.core.api.Assertions.*;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DefaultValueTest extends AbstractTest {
    public static class A {
        @Config(defaultValue = "fallback")
        String string;

        @Config(defaultValue = "fallback")
        String unconfigured;
    }

    public static class B {
        @Config
        String string;

        @Config(defaultValue = "fallback")
        String unconfigured;
    }

    @Inject
    A a;
    @Inject
    B b;

    @Test
    public void shouldNotUseDefaultValueWhenConfigured() {
        assertThat(a.string).isEqualTo("test-value"); // not the fallback
        assertThat(b.string).isEqualTo("test-value"); // no fallback available
    }

    @Test
    public void shouldUseDefaultValueWhenUnconfigured() {
        assertThat(a.unconfigured).isEqualTo("fallback"); // use fallback
        assertThat(b.unconfigured).isEqualTo("fallback"); // and here as well
    }
}
