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
public class PushConfigTest extends AbstractTest {
    private static final Path PATH = Paths.get("target/test-classes/configuration-alt.properties");
    private static final String CONFIG1 = "alt-string=alt-value\n";
    private static final String CONFIG2 = "alt-string=alt-value2\n";

    @ToString
    static class ToBeConfigured {
        @Config(name = "alt-string")
        private AtomicReference<String> string;
    }

    @Inject
    ToBeConfigured tbc;

    @Test
    @Ignore("not yet implemented")
    public void shouldConfigureString() throws Exception {
        assertEquals("alt-value", tbc.string.get());
        assertEquals(CONFIG1, readFile(PATH));

        try {
            Files.write(PATH, CONFIG2.getBytes());

            Thread.sleep(200);

            assertEquals("alt-value2", tbc.string);
        } finally {
            Files.write(PATH, CONFIG1.getBytes());
        }
    }
}
