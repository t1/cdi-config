package com.github.t1.config;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.*;
import org.junit.runner.RunWith;

import com.github.t1.config.ConfigPoint.UpdatableConfigValue;
import com.github.t1.testtools.*;

import lombok.ToString;

@RunWith(Arquillian.class)
public class UpdateConfigSourceIT extends AbstractIT {
    public static class UpdatingConfigSource implements ConfigSource {
        @Override
        public void configure(ConfigPoint configPoint) {
            String name = configPoint.name();
            if ("java-config-string".equals(name)) {
                assertNull(configValue);
                configValue = new InMemoryConfigValue(name, "initial-value", configPoint);
                configPoint.configValue(configValue);
            }
        }
    }

    public static class InMemoryConfigValue extends UpdatableConfigValue {
        private String value;

        public InMemoryConfigValue(String name, String value, ConfigPoint configPoint) {
            configPoint.super(name);
            this.value = value;
        }

        @Override
        protected <T> T getValue(Class<T> type) {
            return convert(value, type);
        }

        @Override
        public void writeValue(String value) {
            this.value = value;
            updateAllConfigTargets();
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        @Override
        public String toString() {
            return "in memory value " + getName();
        }
    }


    private static InMemoryConfigValue configValue;

    @ToString
    static class ToBeConfigured {
        @Config(name = "java-config-string")
        private AtomicReference<String> javaConfigString;
        @Config(name = "user.language")
        private AtomicReference<String> systemPropertyString;
        @Config(name = "alt-string")
        private AtomicReference<String> altString;
    }

    @Inject
    ToBeConfigured tbc;

    @Rule
    public TestLoggerRule logger = new TestLoggerRule();
    @Rule
    public SystemPropertiesRule systemProperties = new SystemPropertiesRule();
    @Rule
    public FileMemento fileMemento = new FileMemento("target/test-classes/configuration-alt.properties");

    @Test
    public void shouldUpdateFromJavaClass() {
        assertEquals("initial-value", tbc.javaConfigString.get());

        configValue.writeValue("updated-value");

        assertEquals("updated-value", tbc.javaConfigString.get());
        configValue.writeValue("initial-value");
    }

    @Test
    public void shouldUpdateFromSystemProperty() throws Exception {
        String orig = System.getProperty("user.language");
        assertEquals(orig, tbc.systemPropertyString.get());

        systemProperties.given("user.language", "foo");

        waitForValue("foo", tbc.systemPropertyString);
    }

    @Test
    public void shouldUpdateFromFileChange() throws Exception {
        assertEquals("alt-value", tbc.altString.get());

        fileMemento.write("alt-string=alt-value2\n");

        waitForValue("alt-value2", tbc.altString);
    }
}
