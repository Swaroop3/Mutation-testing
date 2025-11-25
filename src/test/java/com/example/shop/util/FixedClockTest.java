package com.example.shop.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FixedClockTest {
    @Test
    void returnsFixedInstant() {
        Instant instant = Instant.parse("2025-01-01T00:00:00Z");
        FixedClock clock = new FixedClock(instant, ZoneId.of("UTC"));
        assertEquals(instant, clock.now());
        assertEquals(ZoneId.of("UTC"), clock.zone());
    }
}
