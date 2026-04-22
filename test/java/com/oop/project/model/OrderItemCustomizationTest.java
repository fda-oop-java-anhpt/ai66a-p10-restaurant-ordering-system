package com.oop.project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class OrderItemCustomizationTest {

    @Test
    void constructor_setsFields() {
        OrderItemCustomization relation = new OrderItemCustomization(1, 2, 3);

        assertEquals(1, relation.getId());
        assertEquals(2, relation.getOrderItemId());
        assertEquals(3, relation.getCustomizationId());
    }

    @Test
    void setters_validatePositiveIds() {
        OrderItemCustomization relation = new OrderItemCustomization();

        assertThrows(IllegalArgumentException.class, () -> relation.setOrderItemId(0));
        assertThrows(IllegalArgumentException.class, () -> relation.setCustomizationId(0));
    }
}
