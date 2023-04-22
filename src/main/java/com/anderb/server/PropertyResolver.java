package com.anderb.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class PropertyResolver {
    private final Map<String, String> props = new HashMap<>();

    public PropertyResolver(String [] properties) {
        parseProperties(properties);
    }

    public void parseProperties(String[] values) {
        try {
            Map<String, String> newProperties = Arrays.stream(values)
                    .map(this::parseProperty)
                    .collect(Collectors.toMap(Pair::getLeft, Pair::getRight, (o, n) -> n));
            props.putAll(newProperties);
        } catch (Exception e) {
            log.error("Exception during parsing properties", e);
        }
    }

    public String getString(String key, String defaultValue) {
        return props.getOrDefault(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String val = props.get(key);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }

    public long getLong(String key, long defaultValue) {
        String val = props.get(key);
        return val != null ? Long.parseLong(val) : defaultValue;
    }

    public void setProperty(String key, String value) {
        props.put(key, value);
    }

    private Pair<String, String> parseProperty(String prop) {
        int index = 0;
        if (prop.startsWith("--")) {
            index = 2;
        }
        String key = prop.substring(index, prop.indexOf("=")); //skip first two chars (--)
        String value = prop.substring(prop.indexOf("=") + 1);
        return new Pair<>(key, value);
    }

    @Getter
    @AllArgsConstructor
    private static class Pair<L, R> {
        L left;
        R right;
    }
}
