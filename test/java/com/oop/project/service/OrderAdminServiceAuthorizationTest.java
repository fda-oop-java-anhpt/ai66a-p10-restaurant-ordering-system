package com.oop.project.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.oop.project.exception.UnauthorizedException;
import com.oop.project.model.User;

class OrderAdminServiceAuthorizationTest {

    private final OrderAdminService orderAdminService = new OrderAdminService();
    private final User staff = new User(10, "staff", "STAFF");

    @Test
    void nonManagerCannotUpdatePersistedOrders() {
        assertThrows(UnauthorizedException.class, () ->
            orderAdminService.updateOrderStatus(staff, 1, "PAID")
        );

        assertThrows(UnauthorizedException.class, () ->
            orderAdminService.updateOrderItem(staff, 1, 1, 1, 2, List.of())
        );

        assertThrows(UnauthorizedException.class, () ->
            orderAdminService.removeOrderItem(staff, 1, 1)
        );
    }
}
