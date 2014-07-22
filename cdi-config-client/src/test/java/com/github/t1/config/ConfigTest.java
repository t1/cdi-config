package com.github.t1.config;

import static org.junit.Assert.*;

import java.time.LocalDate;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConfigTest {
    @Deployment
    public static JavaArchive loggingInterceptorDeployment() {
        return ShrinkWrap.create(JavaArchive.class) //
                .addPackage(Config.class.getPackage()) //
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml") //
        ;
    }

    static class ToBeConfigured {
        @Config
        String string;
        @Config
        int i;
        @Config
        boolean bool;
        @Config
        LocalDate date;
    }

    @Inject
    ToBeConfigured tbc;

    @Test
    public void shouldConfigureString() {
        assertEquals("test-value", tbc.string);
    }

    @Test
    public void shouldConfigureInt() {
        assertEquals(123, tbc.i);
    }

    @Test
    public void shouldConfigureBoolean() {
        assertEquals(true, tbc.bool);
    }

    @Test
    public void shouldConfigureLocalDate() {
        assertEquals(LocalDate.of(2014, 12, 31), tbc.date);
    }
}
