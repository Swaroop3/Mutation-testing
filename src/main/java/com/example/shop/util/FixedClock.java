package com.example.shop.util;

import java.time.Instant;
import java.time.ZoneId;

public final class FixedClock implements Clock {
    private final Instant fixed;
    private final ZoneId zone;

    public FixedClock(Instant fixed, ZoneId zone) {
        this.fixed = fixed;
        this.zone = zone;
    }

    @Override
    public Instant now() {
        return fixed;
    }

    @Override
    public ZoneId zone() {
        return zone;
    }
}
