package com.oop.project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    void priceCalculations_includeCustomizationsAndQuantity() {
        MenuItem menuItem = new MenuItem(10, "Beef Noodle", "", new BigDecimal("50000"), 1, LocalDateTime.now());
        List<CustomizationOption> customizations = List.of(
            new CustomizationOption(1, "Extra Beef", new BigDecimal("10000"), 10),
            new CustomizationOption(2, "Special Broth", new BigDecimal("5000"), 10)
        );

        OrderItem orderItem = new OrderItem(menuItem, customizations, 2);

        assertEquals(0, orderItem.getCustomizationTotal().compareTo(new BigDecimal("15000")));
        assertEquals(0, orderItem.getUnitPrice().compareTo(new BigDecimal("65000")));
        assertEquals(0, orderItem.getLineTotal().compareTo(new BigDecimal("130000")));
        assertEquals("Extra Beef, Special Broth", orderItem.getCustomizationSummary());
    }

    @Test
    void customizationsList_isReadOnlyFromOutside() {
        MenuItem menuItem = new MenuItem(11, "Iced Coffee", "", new BigDecimal("25000"), 2, LocalDateTime.now());
        OrderItem orderItem = new OrderItem(menuItem, new ArrayList<>(), 1);

        assertThrows(UnsupportedOperationException.class, () ->
            orderItem.getCustomizations().add(new CustomizationOption(3, "Less Ice", BigDecimal.ZERO, 11))
        );
    }
}
