package com.example.shop.user;

import java.util.Locale;
import java.util.Objects;

public final class User {
    private final String id;
    private final String tier;
    private final Locale locale;

    public User(String id, String tier, Locale locale) {
        this.id = Objects.requireNonNull(id, "id");
        this.tier = Objects.requireNonNull(tier, "tier");
        this.locale = Objects.requireNonNull(locale, "locale");
    }

    public String id() { return id; }
    public String tier() { return tier; }
    public Locale locale() { return locale; }
}
