package com.oop.project.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oop.project.db.DBConnection;
import com.oop.project.model.MenuCategory;

public class MenuCategoryRepository {
    
    public List<MenuCategory> findAll() {
        List<MenuCategory> categories = new ArrayList<>();
        String sql = "SELECT id, name FROM menu_categories ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                categories.add(
                    new MenuCategory(
                        rs.getInt("id"),
                        rs.getString("name")
                    )
                );
            } 
        }   catch (SQLException e) {
                e.printStackTrace();
            }
            return categories;
    }
}
