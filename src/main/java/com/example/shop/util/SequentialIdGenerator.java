package com.example.shop.util;

import java.util.concurrent.atomic.AtomicLong;

public final class SequentialIdGenerator implements IdGenerator {
    private final AtomicLong counter = new AtomicLong();
    private final String prefix;

    public SequentialIdGenerator(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String nextId() {
        return prefix + counter.incrementAndGet();
    }
}
