package com.oop.project.model;

import java.util.ArrayList;
import java.util.List;

public class OrderItem {

    private int id;
    private int menuItemId;
    private int quantity;
    private double unitPrice;
    private List<CustomizationOption> customizations;

    // Constructor rỗng
    public OrderItem() {
        this.customizations = new ArrayList<>();
    }

    // Constructor chính
    public OrderItem(int id,
                     int menuItemId,
                     int quantity,
                     double unitPrice,
                     List<CustomizationOption> customizations) {
        this.id = id;
        setMenuItemId(menuItemId);
        setQuantity(quantity);
        setUnitPrice(unitPrice);
        setCustomizations(customizations);
    }

    // Constructor tạo mới
    public OrderItem(int menuItemId,
                     int quantity,
                     double unitPrice,
                     List<CustomizationOption> customizations) {
        setMenuItemId(menuItemId);
        setQuantity(quantity);
        setUnitPrice(unitPrice);
        setCustomizations(customizations);
    }

    // Getter
    public int getId() {
        return id;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public List<CustomizationOption> getCustomizations() {
        return customizations;
    }

    public double getTotalPrice() {
        double customizationTotal = 0.0;

        for (CustomizationOption option : customizations) {
            if (option != null) {
                customizationTotal += option.getPriceDelta();
            }
        }

        return (unitPrice + customizationTotal) * quantity;
    }

    // Setter
    public void setId(int id) {
        this.id = id;
    }

    public void setMenuItemId(int menuItemId) {
        if (menuItemId <= 0) {
            throw new IllegalArgumentException("menuItemId must be > 0");
        }
        this.menuItemId = menuItemId;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        this.quantity = quantity;
    }

    public void setUnitPrice(double unitPrice) {
        if (unitPrice < 0) {
            throw new IllegalArgumentException("unitPrice must be >= 0");
        }
        this.unitPrice = unitPrice;
    }

    public void setCustomizations(List<CustomizationOption> customizations) {
        if (customizations == null) {
            this.customizations = new ArrayList<>();
        } else {
            this.customizations = new ArrayList<>(customizations);
        }
    }

    // Business
    public void addCustomization(CustomizationOption option) {
        if (option == null) return;

        if (this.customizations == null) {
            this.customizations = new ArrayList<>();
        }

        this.customizations.add(option);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", menuItemId=" + menuItemId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + getTotalPrice() +
                ", customizations=" + customizations +
                '}';
    }
}