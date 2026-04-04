package com.oop.project.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private int id;
    private int staffId;
    private double subtotal;
    private double tax;
    private double serviceFee;
    private double total;
    private LocalDateTime createdAt;
    private List<OrderItem> items;

    // Constructor đầy đủ
    public Order(int id, int staffId, double subtotal, double tax,
                 double serviceFee, double total,
                 LocalDateTime createdAt, List<OrderItem> items) {
        this.id = id;
        this.staffId = staffId;
        this.subtotal = subtotal;
        this.tax = tax;
        this.serviceFee = serviceFee;
        this.total = total;
        this.createdAt = createdAt;
        this.items = items;
    }

    // Constructor rỗng
    public Order() {}

    // Getter
    public int getId() { return id; }

    public int getStaffId() { return staffId; }

    public double getSubtotal() { return subtotal; }

    public double getTax() { return tax; }

    public double getServiceFee() { return serviceFee; }

    public double getTotal() { return total; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<OrderItem> getItems() { return items; }

    // Setter
    public void setId(int id) { this.id = id; }

    public void setStaffId(int staffId) { this.staffId = staffId; }

     public void setSubtotal(double subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("subtotal must be >= 0");
        }
        this.subtotal = subtotal;
    }

    public void setTax(double tax) {
        if (tax < 0) {
            throw new IllegalArgumentException("tax must be >= 0");
        }
        this.tax = tax;
    }

    public void setServiceFee(double serviceFee) {
        if (serviceFee < 0) {
            throw new IllegalArgumentException("serviceFee must be >= 0");
        }
        this.serviceFee = serviceFee;
    }

    public void setTotal(double total) { this.total = total; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setItems(List<OrderItem> items) {
        if (items == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = new ArrayList<>(items);
        }
    }
    public void addItem(OrderItem item) {
        if (item == null) return;

        if (this.items == null) {
            this.items = new ArrayList<>();
        }

        this.items.add(item);
    }
    // toString
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", staffId=" + staffId +
                ", subtotal=" + subtotal +
                ", tax=" + tax +
                ", serviceFee=" + serviceFee +
                ", total=" + total +
                ", createdAt=" + createdAt +
                ", items=" + (items != null ? items.size() : 0) +
                '}';
    }
}