package com.oop.project.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.oop.project.db.DBConnection;

public class AuditLogRepository {

    public void logPriceChange(
        int managerId,
        int menuItemId,
        BigDecimal oldPrice,
        BigDecimal newPrice
    ) {
        String sql = """
            INSERT INTO audit_logs(manager_id, menu_item_id, old_price, new_price)
            VALUES(?, ?, ?, ?)
                """;
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, managerId);
            ps.setInt(2, menuItemId);
            ps.setBigDecimal(3, oldPrice);
            ps.setBigDecimal(4, newPrice);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}