package com.oop.project.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.oop.project.model.CustomizationOption;
import com.oop.project.model.MenuItem;
import com.oop.project.model.OrderDraft;
import com.oop.project.model.OrderItem;
import com.oop.project.repository.CustomizationOptionRepository;
import com.oop.project.repository.MenuItemRepository;

public class OrderService {

    private final MenuItemRepository menuItemRepository = new MenuItemRepository();
    private final CustomizationOptionRepository customizationOptionRepository = new CustomizationOptionRepository();

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
        validateQuantity(quantity);
        draft.addItem(new OrderItem(menuItem, customizations, quantity));
    }

    public void replaceItem(OrderDraft draft, int index, MenuItem menuItem, List<CustomizationOption> customizations, int quantity) {
        validateQuantity(quantity);
        draft.replaceItem(index, new OrderItem(menuItem, customizations, quantity));
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
}
