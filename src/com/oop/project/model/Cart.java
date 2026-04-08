package com.oop.project.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items;

    public Cart() {
        this.items = new ArrayList<>();
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public void addItem(CartItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Cart item cannot be null");
        }
        items.add(item);
    }

    public void removeItem(int index) {
        if (index < 0 || index >= items.size()) {
            throw new IllegalArgumentException("Invalid cart item index");
        }
        items.remove(index);
    }

    public CartItem getItem(int index) {
        if (index < 0 || index >= items.size()) {
            throw new IllegalArgumentException("Invalid cart item index");
        }
        return items.get(index);
    }

    public void updateQuantity(int index, int newQuantity) {
        if (index < 0 || index >= items.size()) {
            throw new IllegalArgumentException("Invalid cart item index");
        }
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        items.get(index).setQuantity(newQuantity);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getTotalItems() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public BigDecimal getSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : items) {
            subtotal = subtotal.add(item.getLineTotal());
        }
        return subtotal;
    }

    public BigDecimal getTax(BigDecimal taxRate) {
        if (taxRate == null || taxRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tax rate must be >= 0");
        }
        return getSubtotal().multiply(taxRate);
    }

    public BigDecimal getServiceFee(BigDecimal serviceFeeRate) {
        if (serviceFeeRate == null || serviceFeeRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Service fee rate must be >= 0");
        }
        return getSubtotal().multiply(serviceFeeRate);
    }

    public BigDecimal getTotal(BigDecimal taxRate, BigDecimal serviceFeeRate) {
        return getSubtotal()
                .add(getTax(taxRate))
                .add(getServiceFee(serviceFeeRate));
    }
}