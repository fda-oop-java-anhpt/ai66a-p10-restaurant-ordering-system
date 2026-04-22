package com.oop.project.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private int id;
    private int staffId;
    private String staffName;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal serviceFee;
    private BigDecimal total;
    private String orderStatus;
    private LocalDateTime createdAt;
    private List<OrderItem> items;

    // Constructor đầy đủ
    public Order(int id, int staffId, BigDecimal subtotal, BigDecimal tax,
                 BigDecimal serviceFee, BigDecimal total,
                 LocalDateTime createdAt, List<OrderItem> items) {
        this(id, staffId, null, subtotal, tax, serviceFee, total, null, createdAt, items);
    }

    public Order(int id, int staffId, String staffName, BigDecimal subtotal, BigDecimal tax,
                 BigDecimal serviceFee, BigDecimal total,
                 LocalDateTime createdAt, List<OrderItem> items) {
        this(id, staffId, staffName, subtotal, tax, serviceFee, total, null, createdAt, items);
    }

    public Order(int id, int staffId, String staffName, BigDecimal subtotal, BigDecimal tax,
                 BigDecimal serviceFee, BigDecimal total, String orderStatus,
                 LocalDateTime createdAt, List<OrderItem> items) {
        this.id = id;
        this.staffId = staffId;
        this.staffName = staffName;
        this.subtotal = subtotal;
        this.tax = tax;
        this.serviceFee = serviceFee;
        this.total = total;
        this.orderStatus = normalizeStatus(orderStatus);
        this.createdAt = createdAt;
        this.items = items;
    }

    // Constructor rỗng
    public Order() {}

    // Getter
    public int getId() { return id; }

    public int getStaffId() { return staffId; }

    public String getStaffName() { return staffName == null ? "" : staffName; }

    public BigDecimal getSubtotal() { return subtotal; }

    public BigDecimal getTax() { return tax; }

    public BigDecimal getServiceFee() { return serviceFee; }

    public BigDecimal getTotal() { return total; }

    public String getOrderStatus() { return normalizeStatus(orderStatus); }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<OrderItem> getItems() { return items; }

    public int getItemCount() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    // Setter
    public void setId(int id) { this.id = id; }

    public void setStaffId(int staffId) { this.staffId = staffId; }

    public void setStaffName(String staffName) { this.staffName = staffName; }

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

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = normalizeStatus(orderStatus);
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

    public boolean canTransitionTo(String newStatus) {
        String normalizedNewStatus = normalizeStatus(newStatus);
        return STATUS_OPEN.equals(getOrderStatus())
            && (STATUS_PAID.equals(normalizedNewStatus) || STATUS_CANCELLED.equals(normalizedNewStatus));
    }

    public boolean isOpen() {
        return STATUS_OPEN.equals(getOrderStatus());
    }

    public boolean isPaid() {
        return STATUS_PAID.equals(getOrderStatus());
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(getOrderStatus());
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return STATUS_OPEN;
        }
        return status.trim().toUpperCase();
    }

    // toString
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", staffId=" + staffId +
            ", staffName='" + staffName + '\'' +
                ", subtotal=" + subtotal +
                ", tax=" + tax +
                ", serviceFee=" + serviceFee +
                ", total=" + total +
                ", orderStatus='" + orderStatus + '\'' +
                ", createdAt=" + createdAt +
                ", items=" + (items != null ? items.size() : 0) +
                '}';
    }
}