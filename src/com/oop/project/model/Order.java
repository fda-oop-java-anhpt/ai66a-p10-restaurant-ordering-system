package com.oop.project.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int staffId;
    private String staffName;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal serviceFee;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private List<OrderItem> items;

    public Order(
        int id,
        int staffId,
        String staffName,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal serviceFee,
        BigDecimal total,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.staffId = staffId;
        this.staffName = staffName;
        this.subtotal = subtotal;
        this.tax = tax;
        this.serviceFee = serviceFee;
        this.total = total;
        this.createdAt = createdAt;
        this.items = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int getStaffId() {
        return staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public int getItemCount() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", staffName='" + staffName + '\'' +
                ", total=" + total +
                ", createdAt=" + createdAt +
                '}';
    }
}
