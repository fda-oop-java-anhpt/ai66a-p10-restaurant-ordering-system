package com.oop.project.model;

public class OrderItemCustomization {
    private int id;
    private int orderItemId;
    private int customizationId;

    // Constructor rỗng
    public OrderItemCustomization() {
    }

    // Constructor chính
    public OrderItemCustomization(int id, int orderItemId, int customizationId) {
        this.id = id;
        setOrderItemId(orderItemId);
        setCustomizationId(customizationId);
    }

    // getters
    public int getId() {
        return id;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public int getCustomizationId() {
        return customizationId;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setOrderItemId(int orderItemId) {
        if (orderItemId <= 0) {
            throw new IllegalArgumentException("orderItemId must be > 0");
        }
        this.orderItemId = orderItemId;
    }


    public void setCustomizationId(int customizationId) {
        if (customizationId <= 0) {
            throw new IllegalArgumentException("customizationId must be > 0");
        }
        this.customizationId = customizationId;
    }

    @Override
    public String toString() {
        return "OrderItemCustomization{" +
                "id=" + id +
                ", orderItemId=" + orderItemId +
                ", customizationId=" + customizationId +
                '}';
    }
}
