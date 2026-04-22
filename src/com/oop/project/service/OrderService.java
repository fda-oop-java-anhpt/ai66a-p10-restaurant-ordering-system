package com.oop.project.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.oop.project.exception.UnauthorizedException;
import com.oop.project.model.CustomizationOption;
import com.oop.project.model.MenuItem;
import com.oop.project.model.Order;
import com.oop.project.model.OrderDraft;
import com.oop.project.model.OrderItem;
import com.oop.project.model.User;
import com.oop.project.repository.CustomizationOptionRepository;
import com.oop.project.repository.MenuItemRepository;
import com.oop.project.repository.OrderRepository;

public class OrderService {

    private final MenuItemRepository menuItemRepository = new MenuItemRepository();
    private final CustomizationOptionRepository customizationOptionRepository = new CustomizationOptionRepository();
    private final OrderRepository orderRepository = new OrderRepository();

    public OrderDraft createOrder(int staffId) {
        return new OrderDraft(staffId);
    }

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public List<CustomizationOption> getCustomizationOptions(int menuItemId) {
        return customizationOptionRepository.findByMenuItemId(menuItemId);
    }

    public void addItem(OrderDraft draft, MenuItem menuItem, List<CustomizationOption> customizations, int quantity) {
        addItem(draft, menuItem, customizations, quantity, "");
    }

    public void addItem(OrderDraft draft, MenuItem menuItem, List<CustomizationOption> customizations, int quantity, String note) {
        validateQuantity(quantity);
        draft.addItem(new OrderItem(menuItem, customizations, quantity, note));
    }

    public void replaceItem(OrderDraft draft, int index, MenuItem menuItem, List<CustomizationOption> customizations, int quantity) {
        replaceItem(draft, index, menuItem, customizations, quantity, "");
    }

    public void replaceItem(OrderDraft draft, int index, MenuItem menuItem, List<CustomizationOption> customizations, int quantity, String note) {
        validateQuantity(quantity);
        draft.replaceItem(index, new OrderItem(menuItem, customizations, quantity, note));
    }

    public void removeItem(OrderDraft draft, int index) {
        draft.removeItem(index);
    }

    public static int parseQuantity(String input) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Quantity must be a valid integer.");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
    }

    public List<CustomizationOption> copyOfSelected(List<CustomizationOption> selected) {
        return new ArrayList<>(selected);
    }

    public BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.10"));
    }

    public BigDecimal calculateServiceFee(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.05"));
    }

    public BigDecimal calculateTotal(BigDecimal subtotal) {
        BigDecimal tax = calculateTax(subtotal);
        BigDecimal fee = calculateServiceFee(subtotal);
        return subtotal.add(tax).add(fee);
    }

    public void updateOrderStatus(User staff, int orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order #" + orderId + " not found"));

        if (staff == null || order.getStaffId() != staff.getId()) {
            throw new UnauthorizedException("Staff can only update their own orders");
        }

        if (!order.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                "Cannot transition from '" + order.getOrderStatus() + "' to '" + newStatus + "'"
            );
        }

        orderRepository.updateOrderStatus(orderId, newStatus);
    }
}
