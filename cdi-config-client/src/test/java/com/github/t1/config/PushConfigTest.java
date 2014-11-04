package com.github.t1.config;

import static org.junit.Assert.*;

import java.io.Serializable;

import javax.inject.Inject;

import lombok.ToString;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class PushConfigTest extends AbstractTest {
    @ToString
    static class ToBeConfigured implements Serializable {
        private static final long serialVersionUID = 1L;

        @Config
        private String string;
    }

    @Inject
    ToBeConfigured tbc;

    @Test
    public void shouldConfigureString() {
        assertEquals("test-value", tbc.string);
    }
}
