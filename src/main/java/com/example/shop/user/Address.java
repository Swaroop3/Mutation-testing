package com.example.shop.user;

import java.util.Objects;

public final class Address {
    private final String line1;
    private final String city;
    private final String state;
    private final String country;
    private final String postalCode;

    public Address(String line1, String city, String state, String country, String postalCode) {
        this.line1 = Objects.requireNonNull(line1, "line1");
        this.city = Objects.requireNonNull(city, "city");
        this.state = Objects.requireNonNull(state, "state");
        this.country = Objects.requireNonNull(country, "country");
        this.postalCode = Objects.requireNonNull(postalCode, "postalCode");
    }

    public String line1() { return line1; }
    public String city() { return city; }
    public String state() { return state; }
    public String country() { return country; }
    public String postalCode() { return postalCode; }
}
