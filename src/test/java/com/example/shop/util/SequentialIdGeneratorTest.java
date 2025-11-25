package com.example.shop.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SequentialIdGeneratorTest {
    @Test
    void incrementsSequentially() {
        SequentialIdGenerator generator = new SequentialIdGenerator("ord-");
        assertEquals("ord-1", generator.nextId());
        assertEquals("ord-2", generator.nextId());
    }
}
