package com.oop.project.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartItem {
    private MenuItem menuItem;
    private int quantity;
    private List<CustomizationOption> customizations;

    public CartItem(MenuItem menuItem, int quantity, List<CustomizationOption> customizations) {
        setMenuItem(menuItem);
        setQuantity(quantity);
        setCustomizations(customizations);
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item cannot be null");
        }
        this.menuItem = menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.quantity = quantity;
    }

    public List<CustomizationOption> getCustomizations() {
        return new ArrayList<>(customizations);
    }

    public void setCustomizations(List<CustomizationOption> customizations) {
        if (customizations == null) {
            this.customizations = new ArrayList<>();
        } else {
            this.customizations = new ArrayList<>(customizations);
        }
    }

    public BigDecimal getCustomizationTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CustomizationOption option : customizations) {
            total = total.add(option.getPriceDelta());
        }
        return total;
    }

    public BigDecimal getUnitPrice() {
        return menuItem.getPrice().add(getCustomizationTotal());
    }

    public BigDecimal getLineTotal() {
        return getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public String getCustomizationSummary() {
        if (customizations.isEmpty()) {
            return "No customizations";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < customizations.size(); i++) {
            sb.append(customizations.get(i).getName());
            if (i < customizations.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}