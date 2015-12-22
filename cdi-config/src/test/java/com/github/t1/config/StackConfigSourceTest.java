package com.github.t1.config;

import static org.junit.Assert.*;

import org.junit.Test;

import lombok.Value;

public class StackConfigSourceTest {
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
        ConfigSource m = StackConfigSource.of(A, B);

        assertEquals("stack:[A, B]", m.toString());
    }

    @Test
    public void shouldAddThree() {
        ConfigSource m = StackConfigSource.of(A, B, C);

        assertEquals("stack:[A, B, C]", m.toString());
    }

    @Test
    public void shouldAddOneAfterStack() {
        ConfigSource AB = StackConfigSource.of(A, B);

        ConfigSource m = StackConfigSource.of(AB, C);

        assertEquals("stack:[A, B, C]", m.toString());
    }

    @Test
    public void shouldAddOneBeforeStack() {
        ConfigSource BC = StackConfigSource.of(B, C);

        ConfigSource m = StackConfigSource.of(A, BC);

        assertEquals("stack:[A, B, C]", m.toString());
    }

    @Test
    public void shouldAddStackToStack() {
        ConfigSource AB = StackConfigSource.of(A, B);
        ConfigSource CD = StackConfigSource.of(C, D);

        ConfigSource m = StackConfigSource.of(AB, CD);

        assertEquals("stack:[A, B, C, D]", m.toString());
    }
}
