package com.oop.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.oop.project.db.DBConnection;
import com.oop.project.model.User;

public class UserDAO {
    
    public User login(String username, String password) {
        String sql = """
            SELECT u.id, u.username, r.name AS role
            FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.username = ? AND u.password = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                int id = rs.getInt("id");
                String user = rs.getString("username");
                String role = rs.getString("role");
                return new User(id, user, role);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }
}
