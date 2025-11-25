package com.example.shop.catalog;

import com.example.shop.util.Money;
import java.util.Objects;

public final class Product {
    private final String id;
    private final String name;
    private final String category;
    private final Money price;
    private final TaxCode taxCode;
    private final boolean digital;
    private final boolean fragile;

    public Product(String id, String name, String category, Money price, TaxCode taxCode, boolean digital, boolean fragile) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.category = Objects.requireNonNull(category, "category");
        this.price = Objects.requireNonNull(price, "price");
        this.taxCode = Objects.requireNonNull(taxCode, "taxCode");
        this.digital = digital;
        this.fragile = fragile;
    }

    public String id() { return id; }
    public String name() { return name; }
    public String category() { return category; }
    public Money price() { return price; }
    public TaxCode taxCode() { return taxCode; }
    public boolean isDigital() { return digital; }
    public boolean isFragile() { return fragile; }
}
