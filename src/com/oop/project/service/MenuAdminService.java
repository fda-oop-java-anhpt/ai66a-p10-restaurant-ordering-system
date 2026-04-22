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

        validateFoodInput(name, basePrice, categoryId);

        itemRepo.addFood(name, description, basePrice, categoryId);
    }

    public void updatePrice(User admin, int menuItemId, BigDecimal newPrice) {
        if (!admin.isManager()) {
            throw new UnauthorizedException("Only manager can update menu price");
        }

        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be greater than or equal to 0");
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

    public void updateFood(User admin, int menuItemId, String name, String description, BigDecimal newPrice, int categoryId) {
        if (!admin.isManager()) {
            throw new UnauthorizedException("Only manager can update menu item");
        }

        validateFoodInput(name, newPrice, categoryId);

        MenuItem item = itemRepo.findById(menuItemId);
        if (item == null) {
            throw new RuntimeException("Menu item not found");
        }

        BigDecimal oldPrice = item.getBasePrice();
        itemRepo.updateDetails(menuItemId, name.trim(), normalizeDescription(description), newPrice, categoryId);

        if (oldPrice != null && oldPrice.compareTo(newPrice) != 0) {
            auditRepo.logPriceChange(admin.getId(), menuItemId, oldPrice, newPrice);
        }
    }

    public void deleteFood(User admin, int menuItemId) {
        if (!admin.isManager()) {
            throw new UnauthorizedException("Only manager can delete menu item");
        }

        MenuItem item = itemRepo.findById(menuItemId);
        if (item == null) {
            throw new RuntimeException("Menu item not found");
        }

        if (itemRepo.isUsedInOrders(menuItemId)) {
            throw new RuntimeException("Cannot delete this item because it already exists in order history");
        }

        itemRepo.deleteById(menuItemId);
    }

    private void validateFoodInput(String name, BigDecimal basePrice, int categoryId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Food name is required");
        }

        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base price must be greater than or equal to 0");
        }

        if (categoryId <= 0) {
            throw new IllegalArgumentException("Category is required");
        }
    }

    private String normalizeDescription(String description) {
        return description == null ? "" : description.trim();
    }
}
