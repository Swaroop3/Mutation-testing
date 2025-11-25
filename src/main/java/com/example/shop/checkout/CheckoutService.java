package com.example.shop.checkout;

import com.example.shop.cart.Cart;
import com.example.shop.cart.CartService;
import com.example.shop.catalog.InventoryService;
import com.example.shop.catalog.Product;
import com.example.shop.order.Order;
import com.example.shop.order.OrderRepository;
import com.example.shop.payment.PaymentGateway;
import com.example.shop.payment.PaymentResult;
import com.example.shop.payment.PaymentStatus;
import com.example.shop.util.IdGenerator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CheckoutService {
    private final CartService cartService;
    private final InventoryService inventory;
    private final PaymentGateway paymentGateway;
    private final OrderRepository orderRepository;
    private final IdGenerator idGenerator;
    private final Map<String, Product> catalog;

    public CheckoutService(CartService cartService, InventoryService inventory, PaymentGateway paymentGateway, OrderRepository orderRepository, IdGenerator idGenerator, Map<String, Product> catalog) {
        this.cartService = Objects.requireNonNull(cartService, "cartService");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.paymentGateway = Objects.requireNonNull(paymentGateway, "paymentGateway");
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
    }

    public CheckoutResult checkout(CheckoutRequest request) {
        Cart cart = cartService.getCart(request.cartId());
        reserveInventory(cart);
        PaymentResult payment = paymentGateway.authorize(request.cartId(), cart.total());
        if (payment.status() != PaymentStatus.AUTHORIZED) {
            releaseInventory(cart);
            return new CheckoutResult(toOrder(cart, List.of()), payment);
        }
        commitInventory(cart);
        Order order = toOrder(cart, List.of());
        orderRepository.save(order);
        return new CheckoutResult(order, payment);
    }

    private void reserveInventory(Cart cart) {
        cart.lines().forEach(line -> {
            boolean reserved = inventory.reserve(line.productId(), line.quantity());
            if (!reserved) {
                throw new IllegalStateException("Unable to reserve " + line.productId());
            }
        });
    }

    private void commitInventory(Cart cart) {
        cart.lines().forEach(line -> inventory.commit(line.productId(), line.quantity()));
    }

    private void releaseInventory(Cart cart) {
        cart.lines().forEach(line -> inventory.release(line.productId(), line.quantity()));
    }

    private Order toOrder(Cart cart, List<String> promotions) {
        return new Order(idGenerator.nextId(), cart.lines(), cart.total(), cart.taxes(), cart.shipping(), promotions);
    }
}
