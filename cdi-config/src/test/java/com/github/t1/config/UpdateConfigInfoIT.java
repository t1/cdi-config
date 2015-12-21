package com.github.t1.config;

import static com.github.t1.config.ConfigInfo.*;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;

import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

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
        @Config
        private boolean bool;
        @Config(name = "char")
        private char char_;
        @Config(name = "byte")
        private byte byte_;
        @Config(name = "short")
        private short short_;
        @Config(name = "int")
        private int int_;
        @Config(name = "long")
        private long long_;
        @Config(name = "float")
        private float float_;
        @Config(name = "double")
        private double double_;
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
    Map<String, String> originalValues;

    @Before
    public void before() {
        this.userLanguage = config(USER_LANGUAGE);
        this.altString = config("alt-string");
        this.originalValues = asMap(configs);
    }

    private Map<String, String> asMap(List<ConfigInfo> configs) {
        return configs.stream().collect(toMap(
                (Function<ConfigInfo, String>) config -> config.getName(),
                (Function<ConfigInfo, String>) config -> config.getValue().toString()));
    }

    @After
    public void after() {
        configs.forEach(config -> config.updateTo(originalValues.get(config.getName())));
    }

    private ConfigInfo config(String name) throws AssertionError {
        ConfigInfo config = configs.stream()
                .filter(withName(name))
                .findAny()
                .orElseThrow(() -> new AssertionError("expected to find a config " + name));
        assertThat(config.isUpdatable()).as(name + " is updatable").isTrue();
        return config;
    }

    @Test
    public void shouldHaveNiceToString() {
        assertThat(userLanguage.toString()).isEqualTo("ConfigInfo[user.language, "
                + "value='" + System.getProperty(USER_LANGUAGE) + "', "
                + "type=java.lang.String, "
                + "container=" + ToBeConfigured.class.getName() + ", "
                + "updatable=true"
                + "]");
        assertThat(altString.toString()).isEqualTo("ConfigInfo[alt-string, "
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
        assertThat(target.secondUserLanguage.get()).as("second user language").isEqualTo("foo");
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

    @Test
    public void shouldUpdateBooleanPrimitiveToNull() throws Exception {
        config("bool").updateTo(null);

        assertThat(Files.readAllLines(fileMemento.getPath())).contains("bool=false");
        assertThat(target.bool).isEqualTo(false);
    }

    @Test
    public void shouldUpdateCharPrimitiveToNull() throws Exception {
        config("char").updateTo(null);

        assertThat(Files.readAllLines(fileMemento.getPath())).contains("char=\0");
        assertThat(target.char_).isEqualTo((char) 0);
    }

    @Test
    public void shouldUpdateBytePrimitiveToNull() throws Exception {
        config("byte").updateTo(null);

        assertThat(Files.readAllLines(fileMemento.getPath())).contains("byte=0");
        assertThat(target.byte_).isEqualTo((byte) 0);
    }

    @Test
    public void shouldUpdateShortPrimitiveToNull() throws Exception {
        config("short").updateTo(null);

        assertThat(Files.readAllLines(fileMemento.getPath())).contains("short=0");
        assertThat(target.short_).isEqualTo((short) 0);
    }

    @Test
    public void shouldUpdateIntPrimitiveToNull() throws Exception {
        config("int").updateTo(null);

        assertThat(Files.readAllLines(fileMemento.getPath())).contains("int=0");
        assertThat(target.int_).isEqualTo(0);
    }

    @Test
    public void shouldUpdateLongPrimitiveToNull() throws Exception {
        config("long").updateTo(null);

        assertThat(Files.readAllLines(fileMemento.getPath())).contains("long=0");
        assertThat(target.long_).isEqualTo(0);
    }

    @Test
    public void shouldUpdateFloatPrimitiveToNull() throws Exception {
        config("float").updateTo(null);

        assertThat(Files.readAllLines(fileMemento.getPath())).contains("float=0");
        assertThat(target.float_).isEqualTo(0f);
    }

    @Test
    public void shouldUpdateDoublePrimitiveToNull() throws Exception {
        config("double").updateTo(null);

        assertThat(Files.readAllLines(fileMemento.getPath())).contains("double=0");
        assertThat(target.double_).isEqualTo(0d);
    }

}
