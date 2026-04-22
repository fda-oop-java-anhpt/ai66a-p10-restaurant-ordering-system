package com.oop.project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CustomizationOptionTest {

    @Test
    void constructor_setsFields() {
        CustomizationOption option = new CustomizationOption(7, "Extra Cheese", new BigDecimal("5000"), 3);

        assertEquals(7, option.getId());
        assertEquals("Extra Cheese", option.getName());
        assertEquals(0, option.getPriceDelta().compareTo(new BigDecimal("5000")));
        assertEquals(3, option.getMenuItemId());
    }

    @Test
    void setMenuItemId_rejectsNonPositiveValue() {
        CustomizationOption option = new CustomizationOption();
        assertThrows(IllegalArgumentException.class, () -> option.setMenuItemId(0));
        assertThrows(IllegalArgumentException.class, () -> option.setMenuItemId(-1));
    }

    @Test
    void copyConstructor_rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new CustomizationOption(null));
    }
}
