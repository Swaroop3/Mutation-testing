package com.example.shop.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OrderRepository {
    private final List<Order> orders = new ArrayList<>();

    public synchronized void save(Order order) {
        orders.add(order);
    }

    public synchronized Optional<Order> findById(String id) {
        return orders.stream().filter(o -> o.id().equals(id)).findFirst();
    }

    public synchronized List<Order> all() {
        return Collections.unmodifiableList(new ArrayList<>(orders));
    }
}
