package com.github.t1.config;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConfigAnnotationMatchingIT extends AbstractIT {
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
    List<ConfigInfo> configs;

    @Test
    public void shouldProvideListOfConfigs() {
        ConfigInfo documented = configs.stream()
                .filter(config -> config.getName().equals("documented"))
                .findAny()
                .get();

        assertThat(documented.getName()).isEqualTo("documented");
        assertThat(documented.getDescription()).isEqualTo("desc");
        assertThat(documented.getDefaultValue()).isEqualTo("fallback");
    }
}
