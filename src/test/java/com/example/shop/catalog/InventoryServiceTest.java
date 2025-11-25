package com.example.shop.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {

    @Test
    void reservesCommitsAndReleases() {
        InventoryService inv = new InventoryService();
        inv.upsertStock("p1", 5);

        assertEquals(5, inv.available("p1"));
        assertTrue(inv.reserve("p1", 3));
        assertEquals(2, inv.available("p1"));

        inv.release("p1", 1);
        assertEquals(3, inv.available("p1"));

        inv.commit("p1", 2);
        assertEquals(3, inv.available("p1"));
    }

    @Test
    void failsReserveWhenInsufficient() {
        InventoryService inv = new InventoryService();
        inv.upsertStock("p1", 1);
        assertFalse(inv.reserve("p1", 2));
    }

    @Test
    void throwsWhenCommitWithoutReserve() {
        InventoryService inv = new InventoryService();
        inv.upsertStock("p1", 2);
        assertThrows(IllegalStateException.class, () -> inv.commit("p1", 1));
    }

    @Test
    void reserveRejectsNonPositiveQuantity() {
        InventoryService inv = new InventoryService();
        inv.upsertStock("p1", 2);
        assertFalse(inv.reserve("p1", 0));
        assertFalse(inv.reserve("p1", -1));
    }
}
