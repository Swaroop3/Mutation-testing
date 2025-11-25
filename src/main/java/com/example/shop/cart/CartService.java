package com.example.shop.cart;

import com.example.shop.catalog.InventoryService;
import com.example.shop.catalog.Product;
import com.example.shop.pricing.PriceBreakdown;
import com.example.shop.pricing.PriceCalculator;
import com.example.shop.util.Money;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CartService {
    private final InventoryService inventory;
    private final PriceCalculator priceCalculator;
    private final Map<String, Product> catalog;
    private final Map<String, List<CartLine>> carts = new HashMap<>();
    private final int maxLines;
    private final int maxQtyPerItem;

    public CartService(InventoryService inventory, PriceCalculator priceCalculator, Map<String, Product> catalog, int maxLines, int maxQtyPerItem) {
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.priceCalculator = Objects.requireNonNull(priceCalculator, "priceCalculator");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.maxLines = maxLines;
        this.maxQtyPerItem = maxQtyPerItem;
    }

    public synchronized Cart addItem(String cartId, String productId, int qty) {
        validateQty(qty);
        Product product = requireProduct(productId);
        int available = inventory.available(productId);
        if (available < qty) {
            throw new IllegalStateException("Insufficient stock for " + productId);
        }
        List<CartLine> lines = carts.computeIfAbsent(cartId, id -> new ArrayList<>());
        if (lines.size() >= maxLines) {
            throw new IllegalStateException("Cart line limit reached");
        }
        lines.add(new CartLine(productId, qty, product.price(), Money.zero(product.price().currency())));
        return recompute(cartId);
    }

    public synchronized Cart updateQty(String cartId, String productId, int qty) {
        validateQty(qty);
        Product product = requireProduct(productId);
        List<CartLine> lines = carts.get(cartId);
        if (lines == null) {
            throw new IllegalStateException("Cart not found");
        }
        boolean updated = false;
        for (int i = 0; i < lines.size(); i++) {
            CartLine line = lines.get(i);
            if (line.productId().equals(productId)) {
                if (qty > maxQtyPerItem) {
                    throw new IllegalArgumentException("Exceeds per-item limit");
                }
                if (inventory.available(productId) < qty) {
                    throw new IllegalStateException("Insufficient stock for " + productId);
                }
                lines.set(i, new CartLine(productId, qty, product.price(), line.discount()));
                updated = true;
            }
        }
        if (!updated) {
            throw new IllegalStateException("Item not in cart");
        }
        return recompute(cartId);
    }

    public synchronized Cart removeItem(String cartId, String productId) {
        List<CartLine> lines = carts.get(cartId);
        if (lines == null) {
            throw new IllegalStateException("Cart not found");
        }
        lines.removeIf(line -> line.productId().equals(productId));
        return recompute(cartId);
    }

    public synchronized Cart getCart(String cartId) {
        return recompute(cartId);
    }

    private Cart recompute(String cartId) {
        List<CartLine> lines = carts.getOrDefault(cartId, new ArrayList<>());
        PriceBreakdown breakdown = priceCalculator.price(lines);
        return new Cart(cartId, breakdown.lines(), breakdown.subtotal(), breakdown.discounts(), breakdown.taxes(), breakdown.shipping(), breakdown.total());
    }

    private void validateQty(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (qty > maxQtyPerItem) {
            throw new IllegalArgumentException("Exceeds per-item limit");
        }
    }

    private Product requireProduct(String productId) {
        Product product = catalog.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Unknown product " + productId);
        }
        return product;
    }
}
