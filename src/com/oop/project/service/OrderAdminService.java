package com.oop.project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.oop.project.exception.UnauthorizedException;
import com.oop.project.model.CustomizationOption;
import com.oop.project.model.Order;
import com.oop.project.model.OrderItem;
import com.oop.project.model.User;
import com.oop.project.repository.CustomizationOptionRepository;
import com.oop.project.repository.OrderRepository;

public class OrderAdminService {

    private final OrderRepository orderRepository = new OrderRepository();
    private final CustomizationOptionRepository customizationOptionRepository = new CustomizationOptionRepository();

    public List<Order> getAllOrdersWithItems() {
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            order.setItems(orderRepository.findOrderItems(order.getId()));
        }
        return orders;
    }

    public List<Order> searchOrders(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (normalizedKeyword.isEmpty()) {
            return getAllOrdersWithItems();
        }

        List<Order> allOrders = getAllOrdersWithItems();
        return allOrders.stream()
            .filter(order -> {
                if (String.valueOf(order.getId()).contains(normalizedKeyword)) {
                    return true;
                }

                if (order.getStaffName().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                    return true;
                }

                if (order.getOrderStatus().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                    return true;
                }

                for (OrderItem item : order.getItems()) {
                    if (item.getMenuItemName().toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                        return true;
                    }
                }

                return false;
            })
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public void updateOrderStatus(User manager, int orderId, String newStatus) {
        requireManager(manager);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order #" + orderId + " not found"));

        if (!order.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                "Cannot transition from '" + order.getOrderStatus() + "' to '" + newStatus + "'"
            );
        }

        orderRepository.updateOrderStatus(orderId, newStatus);
    }

    public void updateOrderItem(User manager, int orderId, int orderItemId, int menuItemId, int quantity, List<Integer> customizationIds) {
        requireManager(manager);
        orderRepository.updateOrderItemQuantity(orderId, orderItemId, quantity);
        orderRepository.updateOrderItemCustomizations(orderId, orderItemId, menuItemId, customizationIds);
    }

    public void removeOrderItem(User manager, int orderId, int orderItemId) {
        requireManager(manager);
        orderRepository.deleteOrderItem(orderId, orderItemId);
    }

    public List<CustomizationOption> getCustomizationOptionsForMenuItem(int menuItemId) {
        return customizationOptionRepository.findByMenuItemId(menuItemId);
    }

    private void requireManager(User user) {
        if (user == null || !user.isManager()) {
            throw new UnauthorizedException("Only manager can manage persisted orders");
        }
    }
}
