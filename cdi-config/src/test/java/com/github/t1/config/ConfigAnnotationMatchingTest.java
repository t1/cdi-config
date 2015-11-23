package com.github.t1.config;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConfigAnnotationMatchingTest extends AbstractTest {
    public static class A {
        @Config(defaultValue = "fallback")
        String string;

        @Config(defaultValue = "fallback")
        String unconfigured;

        @Config(name = "documented", description = "desc", defaultValue = "fallback")
        String documentedConfig;
    }

    public static class B {
        @Config(defaultValue = "fallback")
        String string;

        @Config(defaultValue = "fallback")
        String unconfigured;

        @Config(name = "documented", description = "desc", defaultValue = "fallback")
        String documentedConfig;
    }

    @Inject
    A a;
    @Inject
    B b;

    @Test
    public void shouldNotUseDefaultValueWhenConfigured() {
        assertThat(a.string).isEqualTo("test-value");
        assertThat(b.string).isEqualTo("test-value");
    }

    @Test
    public void shouldUseDefaultValueWhenUnconfigured() {
        assertThat(a.unconfigured).isEqualTo("fallback");
        assertThat(b.unconfigured).isEqualTo("fallback");
    }

    @Inject
    List<Config> configs;

    @Test
    public void shouldProvideListOfConfigs() {
        Config documented = configs.stream().filter(config -> config.name().equals("documented")).findAny().get();

        assertThat(documented.name()).isEqualTo("documented");
        assertThat(documented.description()).isEqualTo("desc");
        assertThat(documented.defaultValue()).isEqualTo("fallback");
    }
}
