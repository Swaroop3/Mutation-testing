package com.example.shop.promo;

import com.example.shop.util.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class Promotion {
    private final String code;
    private final PromotionType type;
    private final BigDecimal percent;
    private final Money amount;
    private final String category;
    private final boolean combinable;
    private final boolean freeShipping;
    private final Instant startsAt;
    private final Instant endsAt;
    private final BigDecimal minSubtotal;
    private final Set<String> tiersAllowed;
    private final int maxUsesPerUser;

    private Promotion(Builder builder) {
        this.code = builder.code;
        this.type = builder.type;
        this.percent = builder.percent;
        this.amount = builder.amount;
        this.category = builder.category;
        this.combinable = builder.combinable;
        this.freeShipping = builder.freeShipping;
        this.startsAt = builder.startsAt;
        this.endsAt = builder.endsAt;
        this.minSubtotal = builder.minSubtotal;
        this.tiersAllowed = builder.tiersAllowed;
        this.maxUsesPerUser = builder.maxUsesPerUser;
    }

    public String code() { return code; }
    public PromotionType type() { return type; }
    public Optional<BigDecimal> percent() { return Optional.ofNullable(percent); }
    public Optional<Money> amount() { return Optional.ofNullable(amount); }
    public Optional<String> category() { return Optional.ofNullable(category); }
    public boolean combinable() { return combinable; }
    public boolean freeShipping() { return freeShipping; }
    public Optional<Instant> startsAt() { return Optional.ofNullable(startsAt); }
    public Optional<Instant> endsAt() { return Optional.ofNullable(endsAt); }
    public Optional<BigDecimal> minSubtotal() { return Optional.ofNullable(minSubtotal); }
    public Set<String> tiersAllowed() { return tiersAllowed; }
    public int maxUsesPerUser() { return maxUsesPerUser; }

    public boolean isActive(Instant now) {
        if (startsAt != null && now.isBefore(startsAt)) {
            return false;
        }
        if (endsAt != null && now.isAfter(endsAt)) {
            return false;
        }
        return true;
    }

    public static Builder builder(String code, PromotionType type) {
        return new Builder(code, type);
    }

    public static final class Builder {
        private final String code;
        private final PromotionType type;
        private BigDecimal percent;
        private Money amount;
        private String category;
        private boolean combinable = true;
        private boolean freeShipping;
        private Instant startsAt;
        private Instant endsAt;
        private BigDecimal minSubtotal;
        private Set<String> tiersAllowed = Set.of();
        private int maxUsesPerUser = Integer.MAX_VALUE;

        private Builder(String code, PromotionType type) {
            this.code = Objects.requireNonNull(code, "code");
            this.type = Objects.requireNonNull(type, "type");
        }

        public Builder percent(BigDecimal percent) { this.percent = percent; return this; }
        public Builder amount(Money amount) { this.amount = amount; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder combinable(boolean combinable) { this.combinable = combinable; return this; }
        public Builder freeShipping(boolean freeShipping) { this.freeShipping = freeShipping; return this; }
        public Builder startsAt(Instant startsAt) { this.startsAt = startsAt; return this; }
        public Builder endsAt(Instant endsAt) { this.endsAt = endsAt; return this; }
        public Builder minSubtotal(BigDecimal minSubtotal) { this.minSubtotal = minSubtotal; return this; }
        public Builder tiersAllowed(Set<String> tiersAllowed) { this.tiersAllowed = tiersAllowed; return this; }
        public Builder maxUsesPerUser(int maxUsesPerUser) { this.maxUsesPerUser = maxUsesPerUser; return this; }

        public Promotion build() { return new Promotion(this); }
    }
}
