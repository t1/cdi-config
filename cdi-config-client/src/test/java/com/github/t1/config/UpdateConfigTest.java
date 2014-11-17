package com.github.t1.config;

import static org.junit.Assert.*;

import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import lombok.ToString;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class UpdateConfigTest extends AbstractTest {
    public static class UpdatingConfigSource implements ConfigSource {
        @Override
        public void configure(ConfigurationPoint configPoint) {
            if ("java-config-string".equals(configPoint.name())) {
                assertNull(configValue);
                configValue = new SimpleUpdatableConfigValue(configPoint, "initial-value");
                configPoint.setConfigValue(configValue);
            }
        }

        @Override
        public void shutdown() {}
    }

    private static final Path PATH = Paths.get("target/test-classes/configuration-alt.properties");
    private static final String CONFIG1 = "alt-string=alt-value\n";
    private static final String CONFIG2 = "alt-string=alt-value2\n";

    private static SimpleUpdatableConfigValue configValue;

    @ToString
    static class ToBeConfigured {
        @Config(name = "java-config-string")
        private AtomicReference<String> javaConfigString;
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

        configValue.setValue("updated-value");

        assertEquals("updated-value", tbc.javaConfigString.get());
    }

    @Test
    public void shouldUpdateFromFileChange() throws Exception {
        assertEquals("alt-value", tbc.altString.get());
        assertEquals(CONFIG1, readFile(PATH));

        try {
            Files.write(PATH, CONFIG2.getBytes());

            for (int i = 0; i < 40; i++) {
                System.out.println("wait " + i);
                Thread.sleep(50);

                if ("alt-value2".equals(tbc.altString.get())) {
                    return;
                }
            }
            fail("expected altString to change to alt-value2, but it's still " + tbc.altString.get());
        } finally {
            Files.write(PATH, CONFIG1.getBytes());
        }
    }
}
