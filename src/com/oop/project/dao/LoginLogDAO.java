package com.oop.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.oop.project.db.DBConnection;

public class LoginLogDAO {

    public void log(int userId, String action) {
        String sql = "INSERT INTO login_logs (user_id, action) VALUES (?, ?)";

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)

        ) { 
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.executeUpdate();
        }    catch (SQLException e) {
            throw new RuntimeException("Cannot log login action", e);
        }
    }
}
