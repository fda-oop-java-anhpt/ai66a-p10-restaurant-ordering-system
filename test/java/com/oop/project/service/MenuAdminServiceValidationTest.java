package com.oop.project.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.oop.project.exception.UnauthorizedException;
import com.oop.project.model.User;

class MenuAdminServiceValidationTest {

    private final MenuAdminService menuAdminService = new MenuAdminService();
    private final User manager = new User(1, "manager", "MANAGER");
    private final User staff = new User(2, "staff", "STAFF");

    @Test
    void staffCannotModifyMenu() {
        assertThrows(UnauthorizedException.class, () ->
            menuAdminService.addFood(staff, "Tea", "desc", new BigDecimal("10000"), 1)
        );

        assertThrows(UnauthorizedException.class, () ->
            menuAdminService.updatePrice(staff, 1, new BigDecimal("20000"))
        );

        assertThrows(UnauthorizedException.class, () ->
            menuAdminService.deleteFood(staff, 1)
        );

        assertThrows(UnauthorizedException.class, () ->
            menuAdminService.updateFood(staff, 1, "Tea", "desc", new BigDecimal("20000"), 1)
        );
    }

    @Test
    void managerInputValidation_runsBeforeRepositoryCalls() {
        assertThrows(IllegalArgumentException.class, () ->
            menuAdminService.addFood(manager, "", "desc", new BigDecimal("10000"), 1)
        );

        assertThrows(IllegalArgumentException.class, () ->
            menuAdminService.addFood(manager, "Tea", "desc", new BigDecimal("-1"), 1)
        );

        assertThrows(IllegalArgumentException.class, () ->
            menuAdminService.addFood(manager, "Tea", "desc", new BigDecimal("10000"), 0)
        );

        assertThrows(IllegalArgumentException.class, () ->
            menuAdminService.updatePrice(manager, 1, new BigDecimal("-1"))
        );

        assertThrows(IllegalArgumentException.class, () ->
            menuAdminService.updateFood(manager, 1, "", "desc", new BigDecimal("10000"), 1)
        );

        assertThrows(IllegalArgumentException.class, () ->
            menuAdminService.updateFood(manager, 1, "Tea", "desc", new BigDecimal("-1"), 1)
        );
    }
}
