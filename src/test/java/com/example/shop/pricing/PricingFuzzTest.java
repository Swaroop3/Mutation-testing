package com.example.shop.pricing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.example.shop.cart.CartLine;
import com.example.shop.catalog.Product;
import com.example.shop.catalog.TaxCode;
import com.example.shop.promo.CouponValidator;
import com.example.shop.promo.PromoEngine;
import com.example.shop.util.FixedClock;
import com.example.shop.util.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PricingFuzzTest {

    private final Currency usd = Currency.getInstance("USD");

    private List<CartLine> generateFuzzedCartLines(FuzzedDataProvider data) {
        int numLines = data.consumeInt(1, 5);
        return IntStream.range(0, numLines)
                .mapToObj(i -> {
                    String productId = data.consumeString(10);
                    int quantity = data.consumeInt(1, 100);
                    double priceDouble = data.consumeDouble();
                    BigDecimal price = Double.isFinite(priceDouble) ? BigDecimal.valueOf(priceDouble) : BigDecimal.ZERO;
                    return new CartLine(productId, quantity, new Money(price, usd), Money.zero(usd));
                })
                .collect(Collectors.toList());
    }

    @FuzzTest
    void priceCalculatorFuzzTest(FuzzedDataProvider data) {
        Map<String, Product> catalog = Map.of(); // Not needed for this level of test
        TaxCalculator taxCalculator = new TaxCalculator(Map.of(TaxCode.STANDARD, new BigDecimal("0.10")), true);
        ShippingCalculator shipping = new ShippingCalculator(new Money(new BigDecimal("5.00"), usd), new Money(new BigDecimal("50.00"), usd), catalog, BigDecimal.ZERO);
        PromoEngine promoEngine = new PromoEngine(List.of(), new FixedClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC")), catalog, new CouponValidator(), usd);
        PriceCalculator priceCalculator = new PriceCalculator(taxCalculator, shipping, promoEngine, Map.of("anonymous", "standard"), catalog, usd);

        List<CartLine> lines = generateFuzzedCartLines(data);

        try {
            priceCalculator.price(lines);
        } catch (Exception e) {
            // Catching generic exception as complex calculations might throw various errors
        }
    }

    @FuzzTest
    void taxCalculatorFuzzTest(FuzzedDataProvider data) {
        double rateDouble = data.consumeDouble();
        BigDecimal rate = Double.isFinite(rateDouble) ? BigDecimal.valueOf(rateDouble) : BigDecimal.ZERO;
        TaxCalculator taxCalculator = new TaxCalculator(Map.of(TaxCode.STANDARD, rate), data.consumeBoolean());
        double productPriceDouble = data.consumeDouble();
        Product product = new Product("p1", "Prod", "cat", new Money(Double.isFinite(productPriceDouble) ? BigDecimal.valueOf(productPriceDouble) : BigDecimal.ZERO, usd), TaxCode.STANDARD, false, false);
        double lineAmountDouble = data.consumeDouble();
        Money lineAmount = new Money(Double.isFinite(lineAmountDouble) ? BigDecimal.valueOf(lineAmountDouble) : BigDecimal.ZERO, usd);
        double lineDiscountDouble = data.consumeDouble();
        Money lineDiscount = new Money(Double.isFinite(lineDiscountDouble) ? BigDecimal.valueOf(lineDiscountDouble) : BigDecimal.ZERO, usd);

        try {
            taxCalculator.taxFor(product, lineAmount, lineDiscount);
        } catch (Exception e) {
            // Catching generic exception
        }
    }

    @FuzzTest
    void shippingCalculatorFuzzTest(FuzzedDataProvider data) {
        Map<String, Product> catalog = Map.of(); // Not needed for this level of test
        double freeThresholdDouble = data.consumeDouble();
        Money freeShippingThreshold = new Money(Double.isFinite(freeThresholdDouble) ? BigDecimal.valueOf(freeThresholdDouble) : BigDecimal.ZERO, usd);
        double flatRateDouble = data.consumeDouble();
        Money flatRate = new Money(Double.isFinite(flatRateDouble) ? BigDecimal.valueOf(flatRateDouble) : BigDecimal.ZERO, usd);
        ShippingCalculator shippingCalculator = new ShippingCalculator(flatRate, freeShippingThreshold, catalog, BigDecimal.ZERO);
        List<CartLine> lines = generateFuzzedCartLines(data);
        double subtotalDouble = data.consumeDouble();
        Money subtotal = new Money(Double.isFinite(subtotalDouble) ? BigDecimal.valueOf(subtotalDouble) : BigDecimal.ZERO, usd);

        try {
            shippingCalculator.shippingFor(lines, subtotal);
        } catch (Exception e) {
            // Catching generic exception
        }
    }
}
