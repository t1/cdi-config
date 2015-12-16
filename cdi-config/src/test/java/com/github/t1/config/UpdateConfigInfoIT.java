package com.github.t1.config;

import static com.github.t1.config.ConfigInfo.*;
import static org.assertj.core.api.Assertions.*;

import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.*;
import org.junit.runner.RunWith;

import com.github.t1.testtools.*;

@RunWith(Arquillian.class)
public class UpdateConfigInfoIT extends AbstractIT {
    private static final String USER_LANGUAGE = "user.language";

    static class ToBeConfigured {
        @Config(name = USER_LANGUAGE)
        private AtomicReference<String> userLanguage;
        @Config(name = USER_LANGUAGE)
        private AtomicReference<String> secondUserLanguage;
        @Config(name = "alt-string", defaultValue = "default-value", description = "alt-string-description",
                meta = "{'meta-key':'meta-value'}")
        private AtomicReference<String> altString;
        @Config(name = "alt-string", defaultValue = "default-value", description = "alt-string-description",
                meta = "{'meta-key':'meta-value'}")
        private AtomicReference<String> secondAltString;
    }

    @Inject
    ToBeConfigured target;
    @Inject
    List<ConfigInfo> configs;

    @Rule
    public TestLoggerRule logger = new TestLoggerRule();
    @Rule
    public SystemPropertiesRule systemProperties = new SystemPropertiesRule();
    @Rule
    public FileMemento fileMemento = new FileMemento("target/test-classes/configuration-alt.properties");

    ConfigInfo userLanguage;
    ConfigInfo altString;

    @Before
    public void before() {
        this.userLanguage = config(USER_LANGUAGE);
        this.altString = config("alt-string");
    }

    private ConfigInfo config(String name) throws AssertionError {
        ConfigInfo config = configs.stream()
                .filter(byName(name))
                .findAny()
                .orElseThrow(() -> new AssertionError("expected to find a config " + name));
        assertThat(config.isUpdatable()).as(name + " is updatable").isTrue();
        return config;
    }

    @Test
    public void shouldHaveToString() {
        assertThat(userLanguage.toString()).isEqualTo("ConfigInfo[user.language, "
                + "value='" + System.getProperty(USER_LANGUAGE) + "', "
                + "type=java.lang.String, "
                + "container=" + ToBeConfigured.class.getName() + ", "
                + "updatable=true"
                + "]");
        assertThat(altString.toString()).isEqualTo(
                "ConfigInfo[alt-string, "
                        + "description='alt-string-description', "
                        + "defaultValue='default-value', "
                        + "value='alt-value', "
                        + "type=java.lang.String, "
                        + "container=" + ToBeConfigured.class.getName() + ", "
                        + "meta={'meta-key':'meta-value'}, "
                        + "updatable=true"
                        + "]");
    }

    @Test
    public void shouldUpdateSystemPropertyFromConfigInfo() throws Exception {
        systemProperties.given(USER_LANGUAGE, "bar");

        userLanguage.updateTo("foo");

        waitForValue("foo", target.userLanguage);
        assertThat(target.secondUserLanguage.get()).isEqualTo("foo");
    }

    @Test
    public void shouldUpdateConfigInfoFromSystemProperty() {
        assertThat(userLanguage.getValue()).isNotEqualTo("foo");

        systemProperties.given(USER_LANGUAGE, "foo");

        assertThat(userLanguage.getValue()).isEqualTo("foo");
    }

    @Test
    public void shouldUpdatePropertiesFileFromConfigInfo() throws Exception {
        altString.updateTo("alt-value2");

        assertThat(Files.readAllLines(fileMemento.getPath())).contains("alt-string=alt-value2");
        assertThat(target.altString.get()).isEqualTo("alt-value2");
        assertThat(target.secondAltString.get()).isEqualTo("alt-value2");
    }
}
