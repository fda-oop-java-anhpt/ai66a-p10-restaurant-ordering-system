package com.oop.project.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void isManager_isCaseInsensitive() {
        User manager = new User(1, "manager", "MANAGER");
        User managerLower = new User(2, "manager2", "manager");
        User staff = new User(3, "staff", "STAFF");

        assertTrue(manager.isManager());
        assertTrue(managerLower.isManager());
        assertFalse(staff.isManager());
    }
}
