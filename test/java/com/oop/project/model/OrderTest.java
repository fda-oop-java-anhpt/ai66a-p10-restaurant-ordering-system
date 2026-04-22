package com.oop.project.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void status_defaultsToOpenWhenMissing() {
        Order order = new Order(
            1,
            2,
            "staff",
            new BigDecimal("100000"),
            new BigDecimal("10000"),
            new BigDecimal("5000"),
            new BigDecimal("115000"),
            null,
            LocalDateTime.now(),
            List.of()
        );

        assertEquals(Order.STATUS_OPEN, order.getOrderStatus());

        order.setOrderStatus(Order.STATUS_PAID.toLowerCase());
        assertEquals(Order.STATUS_PAID, order.getOrderStatus());
    }

    @Test
    void status_transitionGate_allowsOnlyOpenToPaidOrCancelled() {
        Order order = new Order();

        order.setOrderStatus(Order.STATUS_OPEN);
        assertTrue(order.canTransitionTo(Order.STATUS_PAID));
        assertTrue(order.canTransitionTo(Order.STATUS_CANCELLED));
        assertFalse(order.canTransitionTo(Order.STATUS_OPEN));

        order.setOrderStatus(Order.STATUS_PAID);
        assertFalse(order.canTransitionTo(Order.STATUS_CANCELLED));

        order.setOrderStatus(Order.STATUS_CANCELLED);
        assertFalse(order.canTransitionTo(Order.STATUS_PAID));

        order.setOrderStatus("UNKNOWN");
        assertFalse(order.canTransitionTo(Order.STATUS_PAID));
    }

    @Test
    void itemCount_sumsAllQuantities() {
        MenuItem item = new MenuItem(1, "Tea", "", new BigDecimal("10000"), 1, LocalDateTime.now());
        OrderItem line1 = new OrderItem(item, List.of(), 2);
        OrderItem line2 = new OrderItem(item, List.of(), 3);

        Order order = new Order();
        order.setItems(List.of(line1, line2));

        assertEquals(5, order.getItemCount());
    }

    @Test
    void nonNegativeMoneySetters_areValidated() {
        Order order = new Order();

        assertThrows(IllegalArgumentException.class, () -> order.setSubtotal(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> order.setTax(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> order.setServiceFee(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> order.setTotal(new BigDecimal("-1")));
    }

    @Test
    void setItems_handlesNullGracefully() {
        Order order = new Order();
        order.setItems(null);
        assertNotNull(order.getItems());
        assertEquals(0, order.getItems().size());
    }
}
