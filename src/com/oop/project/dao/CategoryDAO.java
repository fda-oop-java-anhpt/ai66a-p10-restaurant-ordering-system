package com.oop.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oop.project.db.DBConnection;
import com.oop.project.model.MenuCategory;

public class CategoryDAO {
    // Get all categories
    public List<MenuCategory> getAllCategoriesDAO(){
        List<MenuCategory> categories = new ArrayList<>();

        String sql = """
            SELECT 
                id,
                name,
                description,
                created_at
            FROM menu_categories
            ORDER BY id
        """;

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {

           while (rs.next()) {
                MenuCategory category = new MenuCategory(
                        rs.getInt("id"),
                        rs.getString("name")
                );

                categories.add(category);
            }
           } catch (SQLException e) {
            throw new RuntimeException("Cannot load categories", e);
           }
        return categories;
    }
}
