package com.example.shop.user;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.example.shop.util.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

public class DataObjectFuzzTest {

    @FuzzTest
    void userFuzzTest(FuzzedDataProvider data) {
        String id = data.consumeString(20);
        String tier = data.consumeString(10);
        try {
            new User(id, tier, Locale.US);
        } catch (NullPointerException e) {
            // Expected if any argument is null, which Jazzer might try.
        }
    }

    @FuzzTest
    void addressFuzzTest(FuzzedDataProvider data) {
        String street = data.consumeString(50);
        String city = data.consumeString(30);
        String state = data.consumeString(20);
        String country = data.consumeString(10);
        String zip = data.consumeString(10);
        try {
            new Address(street, city, state, country, zip);
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @FuzzTest
    void moneyFuzzTest(FuzzedDataProvider data) {
        try {
            // Fuzz with a wide range of values, including potentially problematic doubles
            BigDecimal amount = BigDecimal.valueOf(data.consumeDouble());
            new Money(amount, Currency.getInstance("USD"));
        } catch (Exception e) {
            // Catch any exception from invalid BigDecimal or Currency operations
        }
    }
}
