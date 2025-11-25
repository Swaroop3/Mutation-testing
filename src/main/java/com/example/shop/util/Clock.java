package com.example.shop.util;

import java.time.Instant;
import java.time.ZoneId;

public interface Clock {
    Instant now();

    ZoneId zone();
}
