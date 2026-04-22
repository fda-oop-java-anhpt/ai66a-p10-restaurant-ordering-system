package com.oop.project.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.oop.project.model.Order;
import com.oop.project.model.OrderItem;
import com.oop.project.repository.OrderRepository;

public class DashboardService {
    
    private final OrderRepository orderRepo = new OrderRepository();

    private boolean isCancelled(Order order) {
        if (order == null) {
            return false;
        }

        String status = order.getOrderStatus();
        return Order.STATUS_CANCELLED.equals(status)
            || "CANCELED".equals(status)
            || "VOID".equals(status);
    }

    private List<Order> excludeCancelled(List<Order> orders) {
        return orders.stream()
            .filter(order -> !isCancelled(order))
            .collect(Collectors.toList());
    }

    public List<Order> getTodaysOrders() {
        return excludeCancelled(orderRepo.findByDate(LocalDate.now()));
    }

    public List<Order> getOrdersByDate(LocalDate date) {
        return excludeCancelled(orderRepo.findByDate(date));
    }

    public List<Order> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        return excludeCancelled(orderRepo.findByDateRange(startDate, endDate));
    }

    public List<Order> getOrdersByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return excludeCancelled(orderRepo.findByPriceRange(minPrice, maxPrice));
    }

    public List<Order> getOrdersByStaff(int staffId) {
        return excludeCancelled(orderRepo.findByStaff(staffId));
    }

    public List<OrderItem> getOrderItems(int orderId) {
        return orderRepo.findOrderItems(orderId);
    }

    public Order getOrderWithItems(Order order) {
        List<OrderItem> items = getOrderItems(order.getId());
        order.setItems(items);
        return order;
    }

    public Map<String, Object> getDailyAnalytics(LocalDate date) {
        List<Order> nonCancelledOrders = getOrdersByDate(date);
        int orderCount = nonCancelledOrders.size();
        BigDecimal totalRevenue = nonCancelledOrders.stream()
            .map(Order::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgOrderValue = orderCount > 0 
            ? totalRevenue.divide(new BigDecimal(orderCount), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        return Map.of(
            "totalRevenue", totalRevenue,
            "orderCount", orderCount,
            "averageOrderValue", avgOrderValue
        );
    }

    public Map<String, Integer> getBestSellingItems(LocalDate date) {
        return orderRepo.getBestSellingItems(date);
    }

    public Map<String, BigDecimal> getBestSellingCategories(LocalDate date) {
        return orderRepo.getBestSellingCategories(date);
    }

    public List<Order> searchOrders(String keyword) {
        List<Order> allOrders = orderRepo.findAll().stream()
            .filter(order -> !isCancelled(order))
            .map(this::getOrderWithItems)
            .collect(Collectors.toList());
        String searchTerm = keyword.toLowerCase().trim();

        return allOrders.stream()
            .filter(order -> {
                if (order.getStaffName().toLowerCase().contains(searchTerm)) {
                    return true;
                }
                return order.getItems().stream()
                    .anyMatch(item -> item.getMenuItemName().toLowerCase().contains(searchTerm));
            })
            .collect(Collectors.toList());
    }

    public List<Order> filterOrders(LocalDate date, BigDecimal minPrice, BigDecimal maxPrice) {
        return getOrdersByDateRange(date, date).stream()
            .filter(order -> order.getTotal().compareTo(minPrice) >= 0 
                         && order.getTotal().compareTo(maxPrice) <= 0)
            .collect(Collectors.toList());
    }

    public List<Order> sortOrders(List<Order> orders, String sortBy, boolean ascending) {
        return orders.stream()
            .sorted((o1, o2) -> {
                int comparison = 0;
                switch (sortBy.toLowerCase()) {
                    case "time" -> comparison = o1.getCreatedAt().compareTo(o2.getCreatedAt());
                    case "total" -> comparison = o1.getTotal().compareTo(o2.getTotal());
                    case "items" -> comparison = Integer.compare(o1.getItemCount(), o2.getItemCount());
                    case "staff" -> comparison = o1.getStaffName().compareTo(o2.getStaffName());
                    default -> comparison = o1.getCreatedAt().compareTo(o2.getCreatedAt());
                }
                return ascending ? comparison : -comparison;
            })
            .collect(Collectors.toList());
    }
}
