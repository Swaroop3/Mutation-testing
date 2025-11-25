package com.example.shop.catalog;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InventoryService {
    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, Integer> reserved = new HashMap<>();

    public synchronized void upsertStock(String productId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must be non-negative");
        }
        stock.put(productId, quantity);
    }

    public synchronized int available(String productId) {
        int total = stock.getOrDefault(productId, 0);
        int hold = reserved.getOrDefault(productId, 0);
        return Math.max(0, total - hold);
    }

    public synchronized boolean reserve(String productId, int quantity) {
        Objects.requireNonNull(productId, "productId");
        if (quantity <= 0) return false;
        int available = available(productId);
        if (available < quantity) {
            return false;
        }
        reserved.put(productId, reserved.getOrDefault(productId, 0) + quantity);
        return true;
    }

    public synchronized void release(String productId, int quantity) {
        if (quantity <= 0) return;
        int current = reserved.getOrDefault(productId, 0);
        int next = Math.max(0, current - quantity);
        if (next == 0) {
            reserved.remove(productId);
        } else {
            reserved.put(productId, next);
        }
    }

    public synchronized void commit(String productId, int quantity) {
        if (quantity <= 0) return;
        int hold = reserved.getOrDefault(productId, 0);
        if (hold < quantity) {
            throw new IllegalStateException("Not enough reserved to commit");
        }
        reserved.put(productId, hold - quantity);
        int current = stock.getOrDefault(productId, 0);
        if (current < quantity) {
            throw new IllegalStateException("Not enough stock to commit");
        }
        stock.put(productId, current - quantity);
    }
}
