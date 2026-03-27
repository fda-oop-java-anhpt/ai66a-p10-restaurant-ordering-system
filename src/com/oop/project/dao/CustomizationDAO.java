package com.oop.project.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.oop.project.db.DBConnection;
import com.oop.project.model.CustomizationOption;

public class CustomizationDAO {

    // Get all customization options
    public List<CustomizationOption> getAllOptions() {

        List<CustomizationOption> list = new ArrayList<>();

        String sql = """
            SELECT id, name, price_delta, menu_item_id
            FROM customization_options
            ORDER BY id
        """;

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {

            while (rs.next()) {
                list.add(new CustomizationOption(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price_delta"),
                        rs.getInt("menu_item_id")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load customization options", e);
        }

        return list;
    }

    // Get options by Menu Item
    public List<CustomizationOption> getByMenuItemId(int menuItemId) {

        List<CustomizationOption> list = new ArrayList<>();

        String sql = """
            SELECT id, name, price_delta, menu_item_id
            FROM customization_options
            WHERE menu_item_id = ?
        """;

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, menuItemId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new CustomizationOption(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("menu_item_id")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load options by menu item", e);
        }

        return list;
    }

    // Get customization of ORDER ITEM (QUAN TRỌNG)
    public List<CustomizationOption> getByOrderItemId(int orderItemId) {

        List<CustomizationOption> list = new ArrayList<>();

        String sql = """
            SELECT c.id, c.name, c.price_delta, c.menu_item_id
            FROM order_item_customizations oic
            JOIN customization_options c
              ON oic.customization_id = c.id
            WHERE oic.order_item_id = ?
        """;

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, orderItemId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new CustomizationOption(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price_delta"),
                        rs.getInt("menu_item_id")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Cannot load order item customization", e);
        }

        return list;
    }
}