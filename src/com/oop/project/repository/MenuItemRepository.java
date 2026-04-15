package com.oop.project.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oop.project.db.DBConnection;
import com.oop.project.model.MenuItem;

public class MenuItemRepository {

    public List<MenuItem> findAll() {
        List<MenuItem> items = new ArrayList<>();
        String sql = """
            SELECT id, name, description, base_price, category_id, created_at
            FROM menu_items
            ORDER BY category_id, name
        """;

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                items.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot load all menu items", e);
        }
        return items;
    }
    
    public List<MenuItem> findbyCategory(int CategoryId) {
        List<MenuItem> items = new ArrayList<>();
        String sql = """
            SELECT id, name, description, base_price, category_id, created_at
            FROM menu_items
            WHERE category_id = ?
            ORDER BY name
                """;
        
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, CategoryId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                items.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot load menu items by category", e);
        }
        return items;
    }

    public MenuItem findById (int id) {
        String sql = "SELECT * FROM menu_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load menu item", e);
        }
        return null;
    }

    public void addFood(String name, String description, BigDecimal basePrice, int categoryId) {
        String sql = "INSERT INTO menu_items(name, description, base_price, category_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, description);
            ps.setBigDecimal(3, basePrice);
            ps.setInt(4, categoryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot add menu item", e);
        }
    }

    public void updatePrice(int menuItemId, BigDecimal newPrice) {
        String sql = "UPDATE menu_items SET base_price = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, newPrice);
            ps.setInt(2, menuItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot update menu item price", e);
        }
    }

    public boolean deleteById(int menuItemId) {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, menuItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot delete menu item", e);
        }
    }

    private MenuItem map(ResultSet rs) throws SQLException {
        return new MenuItem(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("base_price"),
            rs.getInt("category_id"),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
