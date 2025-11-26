package com.example.shop.catalog;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

public class InventoryServiceFuzzTest {

    @FuzzTest
    void inventoryServiceFuzzTest(FuzzedDataProvider data) {
        InventoryService inv = new InventoryService();
        String productId = data.consumeString(20);
        int quantity = data.consumeInt();

        try {
            // Fuzz all critical methods of InventoryService
            inv.upsertStock(productId, data.consumeInt());
            inv.reserve(productId, quantity);
            inv.commit(productId, quantity);
            inv.release(productId, quantity);
            inv.available(productId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Expected exceptions for negative quantities or other invalid states
        }
    }
}
