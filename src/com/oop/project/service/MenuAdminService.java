package com.oop.project.service;

import java.math.BigDecimal;

import com.oop.project.exception.UnauthorizedException;
import com.oop.project.model.MenuItem;
import com.oop.project.model.User;
import com.oop.project.repository.AuditLogRepository;

public class MenuAdminService extends MenuService {
    
    private final AuditLogRepository auditRepo = new AuditLogRepository();

    public void addFood(User admin, String name, String description, BigDecimal basePrice, int categoryId) {
        if (!admin.isManager()) {
            throw new UnauthorizedException("Only manager can add menu item");
        }

        itemRepo.addFood(name, description, basePrice, categoryId);
    }

    public void updatePrice(User admin, int menuItemId, BigDecimal newPrice) {
        if (!admin.isManager()) {
            throw new UnauthorizedException("Only manager can update menu price");
        }

        MenuItem item = itemRepo.findById(menuItemId);
        if (item == null) return;

        BigDecimal oldPrice = item.getBasePrice();

        itemRepo.updatePrice(menuItemId, newPrice);

        auditRepo.logPriceChange(
            admin.getId(),
            menuItemId,
            oldPrice, 
            newPrice);
    }

    public void deleteFood(User admin, int menuItemId) {
        if (!admin.isManager()) {
            throw new UnauthorizedException("Only manager can delete menu item");
        }

        MenuItem item = itemRepo.findById(menuItemId);
        if (item == null) {
            throw new RuntimeException("Menu item not found");
        }

        itemRepo.deleteById(menuItemId);
    }
}
