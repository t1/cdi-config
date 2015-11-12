package com.github.t1.config;

import static org.junit.Assert.*;

import org.junit.Test;

import lombok.Value;

public class MultiConfigSourceTest {
    @Value
    private static class DummyConfigSource implements ConfigSource {
        String text;

        @Override
        public String toString() {
            return text;
        }

        @Override
        public void configure(ConfigPoint configPoint) {}
    }

    private static final ConfigSource A = new DummyConfigSource("A");
    private static final ConfigSource B = new DummyConfigSource("B");
    private static final ConfigSource C = new DummyConfigSource("C");
    private static final ConfigSource D = new DummyConfigSource("D");

    @Test
    public void shouldAddTwo() {
        ConfigSource m = MultiConfigSource.of(A, B);

        assertEquals("multi:[A, B]", m.toString());
    }

    @Test
    public void shouldAddThree() {
        ConfigSource m = MultiConfigSource.of(A, B, C);

        assertEquals("multi:[A, B, C]", m.toString());
    }

    @Test
    public void shouldAddOneAfterMulti() {
        ConfigSource AB = MultiConfigSource.of(A, B);

        ConfigSource m = MultiConfigSource.of(AB, C);

        assertEquals("multi:[A, B, C]", m.toString());
    }

    @Test
    public void shouldAddOneBeforeMulti() {
        ConfigSource BC = MultiConfigSource.of(B, C);

        ConfigSource m = MultiConfigSource.of(A, BC);

        assertEquals("multi:[A, B, C]", m.toString());
    }

    @Test
    public void shouldAddMultiToMulti() {
        ConfigSource AB = MultiConfigSource.of(A, B);
        ConfigSource CD = MultiConfigSource.of(C, D);

        ConfigSource m = MultiConfigSource.of(AB, CD);

        assertEquals("multi:[A, B, C, D]", m.toString());
    }
}
