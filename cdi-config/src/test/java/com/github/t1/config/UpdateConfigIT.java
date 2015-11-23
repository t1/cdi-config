package com.github.t1.config;

import static org.junit.Assert.*;

import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.*;
import org.junit.runner.RunWith;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(Arquillian.class)
public class UpdateConfigIT extends AbstractIT {
    public static class UpdatingConfigSource implements ConfigSource {
        @Override
        public void configure(ConfigPoint configPoint) {
            if ("java-config-string".equals(configPoint.name())) {
                assertNull(configValue);
                configValue = new SimpleUpdatableConfigValue(configPoint, "initial-value");
                configPoint.setConfigValue(configValue);
            }
        }
    }

    private static final Path PATH = Paths.get("target/test-classes/configuration-alt.properties");
    private static final String CONFIG1 = "alt-string=alt-value\n";
    private static final String CONFIG2 = "alt-string=alt-value2\n";

    private static SimpleUpdatableConfigValue configValue;

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

    private void waitForValue(String expectedValue, AtomicReference<String> ref) throws InterruptedException {
        for (int i = 0; i < 40; i++) {
            log.debug("wait {}", i);
            Thread.sleep(50);

            if (expectedValue.equals(ref.get())) {
                return;
            }
        }
        fail("expected value to change to " + expectedValue + ", but it's still " + ref.get());
    }

    @Test
    public void shouldUpdateFromJavaClass() {
        assertEquals("initial-value", tbc.javaConfigString.get());
        assertEquals(CONFIG1, readFile(PATH));

        configValue.setValue("updated-value");

        assertEquals("updated-value", tbc.javaConfigString.get());
    }

    @Test
    public void shouldUpdateFromSystemProperty() throws Exception {
        String orig = System.getProperty("user.language");
        try {
            assertEquals(orig, tbc.systemPropertyString.get());
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
