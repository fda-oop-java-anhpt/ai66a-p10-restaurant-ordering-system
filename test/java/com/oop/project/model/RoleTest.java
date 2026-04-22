package com.oop.project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void fromString_acceptsCaseInsensitiveValues() {
        assertEquals(Role.MANAGER, Role.fromString("manager"));
        assertEquals(Role.STAFF, Role.fromString("StAfF"));
    }

    @Test
    void fromString_throwsForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> Role.fromString("cashier"));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString(null));
    }

    @Test
    void helpers_returnExpectedFlags() {
        assertTrue(Role.MANAGER.isManager());
        assertTrue(Role.STAFF.isStaff());
    }
}
