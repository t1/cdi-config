package com.github.t1.config;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractIT {
    @Deployment
    public static JavaArchive createArquillianDeployment() {
        return ShrinkWrap.create(JavaArchive.class) //
                .addClasses(ConfigInfoProducer.class) //
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @SneakyThrows(IOException.class)
    public static String readFile(Path path) {
        return new String(Files.readAllBytes(path));
    }

    protected void waitForValue(String expectedValue, AtomicReference<String> ref) throws InterruptedException {
        for (int i = 0; i < 40; i++) {
            log.debug("wait {}", i);
            Thread.sleep(50);

            if (expectedValue.equals(ref.get())) {
                return;
            }
        }
        fail("expected value to change to " + expectedValue + ", but it's still " + ref.get());
    }
}
