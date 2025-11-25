package com.example.shop.promo;

import com.example.shop.util.Money;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PromotionOutcome {
    private final Money discount;
    private final boolean freeShipping;
    private final List<String> appliedCodes;

    public PromotionOutcome(Money discount, boolean freeShipping, List<String> appliedCodes) {
        this.discount = discount;
        this.freeShipping = freeShipping;
        this.appliedCodes = Collections.unmodifiableList(new ArrayList<>(appliedCodes));
    }

    public Money discount() { return discount; }
    public boolean freeShipping() { return freeShipping; }
    public List<String> appliedCodes() { return appliedCodes; }
}
