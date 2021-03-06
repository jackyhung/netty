package io.netty.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class AttributeKey<T> extends UniqueKey<T> {

    private static final ConcurrentMap<String, Boolean> names = new ConcurrentHashMap<String, Boolean>();

    public AttributeKey(String name, Class<T> valueType) {
        super(names, name, valueType);
    }
}
