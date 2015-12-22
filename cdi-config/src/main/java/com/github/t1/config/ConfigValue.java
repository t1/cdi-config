package com.github.t1.config;

import java.util.*;
import java.util.regex.*;

import org.joda.convert.StringConvert;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class ConfigValue {
    private static final Pattern EXPRESSION = Pattern.compile("\\{(?<key>[^}]*)\\}");
    private static final StringConvert STRING_CONVERT = StringConvert.INSTANCE;

    static String resolveExpressions(String value) {
        Matcher matcher = EXPRESSION.matcher(value);
        StringBuffer sb = new StringBuffer();
        boolean foundExpressions = false;
        while (matcher.find()) {
            foundExpressions = true;
            String key = matcher.group("key");
            String resolved = System.getProperty(key);
            if (resolved == null) {
                log.error("no system property for key '" + key + "'");
                resolved = "{" + key + "}";
            }
            matcher.appendReplacement(sb, resolved);
        }
        if (!foundExpressions)
            return value;
        matcher.appendTail(sb);
        String result = sb.toString();
        log.debug("resolved '{}' to '{}'", value, result);
        return result;
    }

    @Getter
    private final String name;
    private final List<Runnable> observers = new ArrayList<>();

    public void addObserver(Runnable observer) {
        this.observers.add(observer);
    }

    protected void updateAllConfigTargets() {
        for (Runnable observer : observers)
            observer.run();
    }

    protected <T> T convert(String value, Class<T> type) {
        return STRING_CONVERT.convertFromString(type, resolve(value));
    }

    private String resolve(String value) {
        if (value == null)
            return null;
        return resolveExpressions(value);
    }

    protected abstract <T> T getValue(Class<T> type);

    public boolean isWritable() {
        return false;
    }

    public void writeValue(@SuppressWarnings("unused") String value) {
        throw new UnsupportedOperationException("can't write a " + getClass().getName());
    }
}
