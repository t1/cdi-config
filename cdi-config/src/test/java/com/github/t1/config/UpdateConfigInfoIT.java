package com.github.t1.config;

import static com.github.t1.config.ConfigInfo.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class UpdateConfigInfoIT extends AbstractIT {
    private static final String USER_LANGUAGE = "user.language";

    static class ToBeConfigured {
        @Config(name = USER_LANGUAGE)
        private AtomicReference<String> userLanguage;
    }

    @Inject
    ToBeConfigured target;

    @Inject
    List<ConfigInfo> configs;

    @Rule
    public TestLoggerRule logger = new TestLoggerRule();

    private ConfigInfo userLanguageConfig() throws AssertionError {
        return configs.stream()
                .filter(byName(USER_LANGUAGE))
                .findAny()
                .orElseThrow(() -> new AssertionError("expected to find a config " + USER_LANGUAGE));
    }

    @Test
    @Ignore
    public void shouldUpdateSystemPropertyFromConfigInfo() throws Exception {
        ConfigInfo userLanguage = userLanguageConfig();

        String orig = System.getProperty(USER_LANGUAGE);
        assertEquals(orig, target.userLanguage.get());
        try {
            userLanguage.updateTo("foo");

            waitForValue("foo", target.userLanguage);
        } finally {
            System.setProperty(USER_LANGUAGE, orig);
        }
    }

    @Test
    public void shouldUpdateConfigInfoFromSystemProperty() throws Exception {
        ConfigInfo userLanguage = userLanguageConfig();

        String orig = System.getProperty(USER_LANGUAGE);
        assertEquals(orig, target.userLanguage.get());
        try {
            System.setProperty("user.language", "foo");

            waitForValue("foo", target.userLanguage);

            assertThat(userLanguage.getValue()).isEqualTo("foo");
        } finally {
            System.setProperty(USER_LANGUAGE, orig);
        }
    }
}
