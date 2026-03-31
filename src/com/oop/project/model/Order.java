package com.oop.project.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {

    private int id;
    private int staffId;
    private double subtotal;
    private double tax;
    private double serviceFee;
    private LocalDateTime createdAt;
    private List<OrderItem> items;

    // Constructor đầy đủ
    public Order(int id, int staffId, double subtotal, double tax,
                 double serviceFee, LocalDateTime createdAt, List<OrderItem> items) {
        this.id = id;
        this.staffId = staffId;
        this.subtotal = subtotal;
        this.tax = tax;
        this.serviceFee = serviceFee;
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

    public double getTotal() {
        return subtotal + tax + serviceFee;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<OrderItem> getItems() { return items; }

    // Setter
    public void setId(int id) { this.id = id; }

    public void setStaffId(int staffId) { this.staffId = staffId; }

    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public void setTax(double tax) { this.tax = tax; }

    public void setServiceFee(double serviceFee) { this.serviceFee = serviceFee; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setItems(List<OrderItem> items) { this.items = items; }

    // toString
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", staffId=" + staffId +
                ", subtotal=" + subtotal +
                ", tax=" + tax +
                ", serviceFee=" + serviceFee +
                ", total=" + getTotal() +
                ", createdAt=" + createdAt +
                '}';
    }
}