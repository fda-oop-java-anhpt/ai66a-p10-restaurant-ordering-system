package com.oop.project.service;

import java.math.BigDecimal;

import com.oop.project.exception.UnauthorizedException;
import com.oop.project.model.MenuItem;
import com.oop.project.model.User;
import com.oop.project.repository.AuditLogRepository;

public class MenuAdminService extends MenuService {
    
    private final AuditLogRepository auditRepo = new AuditLogRepository();

    public void updatePrice(User admin, int menuItemId, BigDecimal newPrice) {
        if (!admin.isManager()) {
            throw new UnauthorizedException("Only admin can update menu price");
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
}
