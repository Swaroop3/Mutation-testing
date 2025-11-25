package com.example.shop.checkout;

import com.example.shop.user.Address;
import com.example.shop.user.User;
import java.util.Objects;

public final class CheckoutRequest {
    private final String cartId;
    private final User user;
    private final Address address;

    public CheckoutRequest(String cartId, User user, Address address) {
        this.cartId = Objects.requireNonNull(cartId, "cartId");
        this.user = Objects.requireNonNull(user, "user");
        this.address = Objects.requireNonNull(address, "address");
    }

    public String cartId() { return cartId; }
    public User user() { return user; }
    public Address address() { return address; }
}
