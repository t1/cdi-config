package com.github.t1.config;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TestLoggerRule extends TestWatcher {
    private long startTime;

    @Override
    protected void starting(Description description) {
        log("> " + toString(description));
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void finished(Description description) {
        log("< " + toString(description) + " [took " + (System.currentTimeMillis() - startTime) + "ms]");
    }

    private String toString(Description description) {
        StringBuilder out = new StringBuilder();
        out.append(description.getClassName());
        if (description.getMethodName() != null) {
            out.append(" # ");
            out.append(camelToSpaces(description.getMethodName()));
        }
        return out.toString();
    }

    private String camelToSpaces(String string) {
        StringBuilder out = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (Character.isUpperCase(c)) {
                out.append(' ');
                out.append(Character.toLowerCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public void log(String message) {
        System.out.println("-------------------- " + message);
    }
}
