package com.example.shop.user;

import org.junit.jupiter.api.Test;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {

    @Test
    void testUser() {
        // given
        String id = "123";
        String tier = "gold";
        Locale locale = Locale.US;

        // when
        User user = new User(id, tier, locale);

        // then
        assertEquals(id, user.id());
        assertEquals(tier, user.tier());
        assertEquals(locale, user.locale());
    }
}
