package com.github.t1.config;

import java.net.URI;
import java.nio.file.*;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestClient {
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("key", "initial value");

        Path tempFile = Files.createTempFile("", ".properties");
        tempFile.toFile().deleteOnExit();
        URI uri = tempFile.toUri();
        log.debug("write initial properties to {}", uri);
        properties.store(Files.newOutputStream(tempFile), null);

        PropertiesConfigSource configSource = new PropertiesConfigSource(uri);
        log.debug("started config source");

        Thread.sleep(2_000);

        log.debug("write new value");
        properties.setProperty("key", "new value");
        properties.store(Files.newOutputStream(tempFile), null);

        Thread.sleep(2_000);

        configSource.stop();
        log.debug("end");
    }
}
