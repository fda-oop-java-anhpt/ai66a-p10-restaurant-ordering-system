package com.oop.project.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oop.project.db.DBConnection;
import com.oop.project.model.CustomizationOption;

public class CustomizationOptionRepository {

    public List<CustomizationOption> findByMenuItemId(int menuItemId) {
        List<CustomizationOption> options = new ArrayList<>();
        String sql = """
            SELECT id, name, price_delta, menu_item_id
            FROM customization_options
            WHERE menu_item_id = ?
            ORDER BY id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, menuItemId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                options.add(new CustomizationOption(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getBigDecimal("price_delta"),
                    rs.getInt("menu_item_id")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot load customization options by menu item", e);
        }

        return options;
    }
}