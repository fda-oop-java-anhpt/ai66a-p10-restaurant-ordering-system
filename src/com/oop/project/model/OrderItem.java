package com.oop.project.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrderItem {
    private int id;
    private final MenuItem menuItem;
    private List<CustomizationOption> customizations;
    private int quantity;

    public OrderItem(MenuItem menuItem, List<CustomizationOption> customizations, int quantity) {
        this.menuItem = menuItem;
        this.customizations = new ArrayList<>(customizations);
        this.quantity = quantity;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMenuItemName() {
        return menuItem == null ? "" : menuItem.getName();
    }

    public List<CustomizationOption> getCustomizations() {
        return Collections.unmodifiableList(customizations);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCustomizations(List<CustomizationOption> customizations) {
        this.customizations = new ArrayList<>(customizations);
    }

    public BigDecimal getCustomizationTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CustomizationOption option : customizations) {
            total = total.add(option.getPriceDelta());
        }
        return total;
    }

    public BigDecimal getUnitPrice() {
        return menuItem.getBasePrice().add(getCustomizationTotal());
    }

    public BigDecimal getLineTotal() {
        return getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public String getCustomizationSummary() {
        if (customizations.isEmpty()) {
            return "-";
        }
        return customizations.stream()
                .map(CustomizationOption::getName)
                .collect(Collectors.joining(", "));
    }
}
