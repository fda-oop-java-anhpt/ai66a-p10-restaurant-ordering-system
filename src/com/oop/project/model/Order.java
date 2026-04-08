package com.oop.project.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private int id;
    private int staffId;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal serviceFee;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private List<OrderItem> items;

    // Constructor đầy đủ
    public Order(int id, int staffId, BigDecimal subtotal, BigDecimal tax,
                 BigDecimal serviceFee, BigDecimal total,
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

    public BigDecimal getSubtotal() { return subtotal; }

    public BigDecimal getTax() { return tax; }

    public BigDecimal getServiceFee() { return serviceFee; }

    public BigDecimal getTotal() { return total; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<OrderItem> getItems() { return items; }

    // Setter
    public void setId(int id) { this.id = id; }

    public void setStaffId(int staffId) { this.staffId = staffId; }

     public void setSubtotal(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("subtotal must be a non-negative BigDecimal");
        }
        this.subtotal = subtotal;
    }

    public void setTax(BigDecimal tax) {
        if (tax == null || tax.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("tax must be a non-negative BigDecimal");
        }
        this.tax = tax;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        if (serviceFee == null || serviceFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("serviceFee must be a non-negative BigDecimal");
        }
        this.serviceFee = serviceFee;
    }

    public void setTotal(BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("total must be a non-negative BigDecimal");
        }
        this.total = total;
    }

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