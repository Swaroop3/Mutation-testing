package com.example.shop.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddressTest {

    @Test
    void testAddress() {
        // given
        String line1 = "123 Main St";
        String city = "Anytown";
        String state = "CA";
        String country = "USA";
        String postalCode = "12345";

        // when
        Address address = new Address(line1, city, state, country, postalCode);

        // then
        assertEquals(line1, address.line1());
        assertEquals(city, address.city());
        assertEquals(state, address.state());
        assertEquals(country, address.country());
        assertEquals(postalCode, address.postalCode());
    }
}
