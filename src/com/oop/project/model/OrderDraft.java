package com.oop.project.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderDraft {
    private final int staffId;
    private final List<OrderItem> items = new ArrayList<>();

    public OrderDraft(int staffId) {
        this.staffId = staffId;
    }

    public int getStaffId() {
        return staffId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public void replaceItem(int index, OrderItem item) {
        items.set(index, item);
    }

    public void removeItem(int index) {
        items.remove(index);
    }

    public BigDecimal getSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : items) {
            subtotal = subtotal.add(item.getLineTotal());
        }
        return subtotal;
    }
}
