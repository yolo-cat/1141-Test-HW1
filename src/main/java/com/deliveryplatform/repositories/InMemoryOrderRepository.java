package com.deliveryplatform.repositories;

import com.deliveryplatform.models.Order;
import com.deliveryplatform.models.OrderStatus;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of OrderRepository for development and testing.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        orders.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return orders.values().stream()
                .filter(order -> customerId.equals(order.getCustomerId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByRestaurantId(String restaurantId) {
        return orders.values().stream()
                .filter(order -> restaurantId.equals(order.getRestaurantId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orders.values().stream()
                .filter(order -> status == order.getStatus())
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByRestaurantIdAndStatus(String restaurantId, OrderStatus status) {
        return orders.values().stream()
                .filter(order -> restaurantId.equals(order.getRestaurantId()) && status == order.getStatus())
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String orderId) {
        return orders.remove(orderId) != null;
    }

    @Override
    public boolean existsById(String orderId) {
        return orders.containsKey(orderId);
    }

    @Override
    public long countActiveOrdersByRestaurant(String restaurantId) {
        return orders.values().stream()
                .filter(order -> restaurantId.equals(order.getRestaurantId()))
                .filter(order -> !order.isTerminal())
                .count();
    }

    // Additional methods for testing
    public void clear() {
        orders.clear();
    }

    public int size() {
        return orders.size();
    }
}