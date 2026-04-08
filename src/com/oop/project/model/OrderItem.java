package com.oop.project.model;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderItem {

    private int id;
    private int menuItemId;
    private int quantity;
    private BigDecimal unitPrice;
    private List<CustomizationOption> customizations;

    // Constructor rỗng
    public OrderItem() {
        this.customizations = new ArrayList<>();
    }

    // Constructor chính
    public OrderItem(int id,
                     int menuItemId,
                     int quantity,
                     BigDecimal unitPrice,
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
                     BigDecimal unitPrice,
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public List<CustomizationOption> getCustomizations() {
        return customizations;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal customizationTotal = BigDecimal.ZERO;

        for (CustomizationOption option : customizations) {
            if (option != null) {
                customizationTotal = customizationTotal.add(option.getPriceDelta());
            }
        }

        return (unitPrice.add(customizationTotal)).multiply(BigDecimal.valueOf(quantity));
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

    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("unitPrice must be a non-negative BigDecimal");
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