package com.example.shop.promo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CouponValidator {
    private final Map<String, Integer> usagePerUser = new HashMap<>();

    public boolean canUse(String userId, Promotion promo) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(promo, "promo");
        int used = usagePerUser.getOrDefault(key(userId, promo), 0);
        return used < promo.maxUsesPerUser();
    }

    public void recordUse(String userId, Promotion promo) {
        usagePerUser.put(key(userId, promo), usagePerUser.getOrDefault(key(userId, promo), 0) + 1);
    }

    private String key(String userId, Promotion promo) {
        return userId + ":" + promo.code();
    }
}
