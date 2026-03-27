package com.oop.project.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.oop.project.db.DBConnection;
import com.oop.project.model.MenuItem;

public class MenuDAO {

    // =========================
    // Get all menu items
    // =========================
    public List<MenuItem> getAllMenuItems() {

        List<MenuItem> list = new ArrayList<>();

        String sql = """
            SELECT 
                id,
                name,
                description,
                base_price,
                category_id,
                created_at
            FROM menu_items
            ORDER BY id
        """;

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {

            while (rs.next()) {

                MenuItem item = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("base_price"),
                        rs.getInt("category_id"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );

                list.add(item);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load menu items", e);
        }

        return list;
    }

    // =========================
    // Search menu items
    // =========================
    public List<MenuItem> searchMenuItems(String keyword) {

        List<MenuItem> list = new ArrayList<>();

        String sql = """
            SELECT 
                id,
                name,
                description,
                base_price,
                category_id,
                created_at
            FROM menu_items
            WHERE LOWER(name) LIKE LOWER(?)
        """;

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, "%" + keyword + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                MenuItem item = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("base_price"),
                        rs.getInt("category_id"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );

                list.add(item);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Search failed", e);
        }

        return list;
    }

    // =========================
    // Filter by category
    // =========================
    public List<MenuItem> getByCategory(int categoryId) {

        List<MenuItem> list = new ArrayList<>();

        String sql = """
            SELECT 
                id,
                name,
                description,
                base_price,
                category_id,
                created_at
            FROM menu_items
            WHERE category_id = ?
        """;

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, categoryId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                MenuItem item = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("base_price"),
                        rs.getInt("category_id"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );

                list.add(item);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot filter menu", e);
        }

        return list;
    }

    // =========================
    // Update price
    // =========================
    public void updatePrice(int itemId, BigDecimal newPrice) {

        String sql = """
            UPDATE menu_items
            SET base_price = ?
            WHERE id = ?
        """;

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setBigDecimal(1, newPrice);
            stmt.setInt(2, itemId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Cannot update price", e);
        }
    }
}