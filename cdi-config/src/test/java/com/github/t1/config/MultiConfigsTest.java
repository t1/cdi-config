package com.github.t1.config;

import static org.assertj.core.api.Assertions.*;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MultiConfigsTest extends AbstractTest {
    public static class A {
        @Config
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
    public void shouldConfigureTwoWithTheSameName() {
        assertThat(a.string).isEqualTo("test-value");
        assertThat(b.string).isEqualTo("test-value");
    }

    @Test
    public void shouldConfigureTwoWithTheDefaultValue() {
        assertThat(a.unconfigured).isEqualTo("fallback");
        assertThat(b.unconfigured).isEqualTo("fallback");
    }
}
