package com.oop.project.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.oop.project.db.DBConnection;
import com.oop.project.model.User;

public class UserRepository {

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

                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }
}