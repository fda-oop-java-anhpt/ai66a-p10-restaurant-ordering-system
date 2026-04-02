package com.oop.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oop.project.db.DBConnection;
import com.oop.project.model.Order;

public class OrderDAO {

    // tạo Order
    public int createOrder(int staffId,
                           double subtotal,
                           double tax,
                           double serviceFee,
                           double total) {

        String sql = """
            INSERT INTO orders (staff_id, subtotal, tax, service_fee, total)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
        """;

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, staffId);
            stmt.setDouble(2, subtotal);
            stmt.setDouble(3, tax);
            stmt.setDouble(4, serviceFee);
            stmt.setDouble(5, total);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot create order", e);
        }

        return -1;
    }

    // lấy danh sách order của nhân viên
    public List<Order> getOrdersByStaff(int staffId) {
        List<Order> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM orders
            WHERE staff_id = ?
            ORDER BY created_at DESC
        """;

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, staffId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setStaffId(rs.getInt("staff_id"));
                    order.setSubtotal(rs.getDouble("subtotal"));
                    order.setTax(rs.getDouble("tax"));
                    order.setServiceFee(rs.getDouble("service_fee"));
                    order.setTotal(rs.getDouble("total"));
                    order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                    list.add(order);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot fetch orders", e);
        }

        return list;
    }

    // xóa order
    public void deleteOrder(int orderId) {
        String sql = "DELETE FROM orders WHERE id = ?";

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot delete order", e);
        }
    }
}