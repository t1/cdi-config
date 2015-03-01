package com.github.t1.config;

import java.io.IOException;
import java.nio.file.*;

import lombok.SneakyThrows;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

abstract class AbstractTest {
    @Deployment
    public static JavaArchive loggingInterceptorDeployment() {
        return ShrinkWrap.create(JavaArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @SneakyThrows(IOException.class)
    public static String readFile(Path path) {
        return new String(Files.readAllBytes(path));
    }
}
