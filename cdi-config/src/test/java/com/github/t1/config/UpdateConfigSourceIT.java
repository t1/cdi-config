package com.github.t1.config;

import static org.junit.Assert.*;

import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.*;
import org.junit.runner.RunWith;

import lombok.ToString;

@RunWith(Arquillian.class)
public class UpdateConfigSourceIT extends AbstractIT {
    public static class UpdatingConfigSource implements ConfigSource {
        @Override
        public void configure(ConfigPoint configPoint) {
            if ("java-config-string".equals(configPoint.name())) {
                assertNull(configValue);
                configValue = new InMemoryConfigValue(configPoint, "initial-value");
                configPoint.configValue(configValue);
            }
        }
    }

    private static final Path PATH = Paths.get("target/test-classes/configuration-alt.properties");
    private static final String CONFIG1 = "alt-string=alt-value\n";
    private static final String CONFIG2 = "alt-string=alt-value2\n";

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

    @Test
    public void shouldUpdateFromJavaClass() {
        assertEquals("initial-value", tbc.javaConfigString.get());
        assertEquals(CONFIG1, readFile(PATH));

        configValue.writeValue("updated-value");

        assertEquals("updated-value", tbc.javaConfigString.get());
    }

    @Test
    public void shouldUpdateFromSystemProperty() throws Exception {
        String orig = System.getProperty("user.language");
        assertEquals(orig, tbc.systemPropertyString.get());
        try {
            System.setProperty("user.language", "foo");

            waitForValue("foo", tbc.systemPropertyString);
        } finally {
            System.setProperty("user.language", orig);
        }
    }

    @Test
    public void shouldUpdateFromFileChange() throws Exception {
        assertEquals("alt-value", tbc.altString.get());
        assertEquals(CONFIG1, readFile(PATH));

        try {
            Files.write(PATH, CONFIG2.getBytes());

            waitForValue("alt-value2", tbc.altString);
        } finally {
            Files.write(PATH, CONFIG1.getBytes());
        }
    }
}
