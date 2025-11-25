package com.example.shop.promo;

import com.example.shop.cart.CartLine;
import com.example.shop.catalog.Product;
import com.example.shop.util.Clock;
import com.example.shop.util.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PromoEngine {
    private final List<Promotion> promotions;
    private final Clock clock;
    private final Map<String, Product> catalog;
    private final CouponValidator couponValidator;
    private final Currency baseCurrency;

    public PromoEngine(List<Promotion> promotions, Clock clock, Map<String, Product> catalog, CouponValidator couponValidator, Currency baseCurrency) {
        this.promotions = List.copyOf(promotions);
        this.clock = Objects.requireNonNull(clock, "clock");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.couponValidator = Objects.requireNonNull(couponValidator, "couponValidator");
        this.baseCurrency = Objects.requireNonNull(baseCurrency, "baseCurrency");
    }

    public PromotionOutcome apply(List<CartLine> lines, String userTier, String userId) {
        Instant now = clock.now();
        Money currencyZero = lines.isEmpty() ? Money.zero(baseCurrency) : Money.zero(lines.get(0).unitPrice().currency());
        Money totalDiscount = currencyZero;
        boolean freeShipping = false;
        List<String> applied = new ArrayList<>();

        for (Promotion promo : promotions) {
            if (!promo.isActive(now)) continue;
            if (!promo.tiersAllowed().isEmpty() && !promo.tiersAllowed().contains(userTier)) {
                continue;
            }
            if (!couponValidator.canUse(userId, promo)) {
                continue;
            }
            Money discount = evaluatePromo(promo, lines);
            boolean appliedNow = discount != null && discount.raw().compareTo(BigDecimal.ZERO) > 0;
            if (promo.type() == PromotionType.FREE_SHIPPING) {
                freeShipping = true;
                appliedNow = true;
            }
            if (appliedNow) {
                applied.add(promo.code());
                if (discount != null) {
                    totalDiscount = totalDiscount.add(discount);
                }
                couponValidator.recordUse(userId, promo);
                if (!promo.combinable()) {
                    break;
                }
            }
        }
        return new PromotionOutcome(totalDiscount, freeShipping, applied);
    }

    private Money evaluatePromo(Promotion promo, List<CartLine> lines) {
        switch (promo.type()) {
            case PERCENTAGE:
                return percentOff(lines, promo.percent().orElse(BigDecimal.ZERO));
            case FIXED_AMOUNT:
                return promo.amount().orElseThrow(() -> new IllegalStateException("Fixed promo missing amount"));
            case BOGO:
                return bogo(lines, promo.category().orElse(""));
            case CATEGORY_PERCENTAGE:
                return categoryPercent(lines, promo.category().orElse(""), promo.percent().orElse(BigDecimal.ZERO));
            case FREE_SHIPPING:
                return Money.zero(lines.get(0).unitPrice().currency());
            default:
                return Money.zero(lines.get(0).unitPrice().currency());
        }
    }

    private Money percentOff(List<CartLine> lines, BigDecimal percent) {
        if (percent.compareTo(BigDecimal.ZERO) <= 0) {
            return Money.zero(lines.get(0).unitPrice().currency());
        }
        Money subtotal = lines.stream()
                .map(CartLine::subtotal)
                .reduce(Money.zero(lines.get(0).unitPrice().currency()), Money::add);
        return subtotal.multiply(percent);
    }

    private Money categoryPercent(List<CartLine> lines, String category, BigDecimal percent) {
        Money zero = Money.zero(lines.get(0).unitPrice().currency());
        if (category.isBlank() || percent.compareTo(BigDecimal.ZERO) <= 0) return zero;
        List<CartLine> eligible = lines.stream()
                .filter(line -> category.equalsIgnoreCase(productCategory(line)))
                .collect(Collectors.toList());
        Money subtotal = eligible.stream().map(CartLine::subtotal).reduce(zero, Money::add);
        return subtotal.multiply(percent);
    }

    private Money bogo(List<CartLine> lines, String category) {
        Money zero = Money.zero(lines.get(0).unitPrice().currency());
        int totalEligible = lines.stream()
                .filter(line -> category.isBlank() || category.equalsIgnoreCase(productCategory(line)))
                .mapToInt(CartLine::quantity)
                .sum();
        if (totalEligible < 2) return zero;
        int freeItems = totalEligible / 2;
        BigDecimal unitPrice = lines.stream()
                .filter(line -> category.isBlank() || category.equalsIgnoreCase(productCategory(line)))
                .map(line -> line.unitPrice().raw())
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        return new Money(unitPrice.multiply(BigDecimal.valueOf(freeItems)), lines.get(0).unitPrice().currency());
    }

    private String productCategory(CartLine line) {
        Product product = catalog.get(line.productId());
        return product == null ? "" : product.category();
    }
}
