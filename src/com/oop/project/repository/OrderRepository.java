package com.oop.project.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oop.project.db.DBConnection;
import com.oop.project.model.MenuItem;
import com.oop.project.model.Order;
import com.oop.project.model.OrderItem;

public class OrderRepository {

    public int createOrder(int staffId,
                           BigDecimal subtotal,
                           BigDecimal tax,
                           BigDecimal serviceFee,
                           BigDecimal total) {
        String sql = """
            INSERT INTO orders (staff_id, subtotal, tax, service_fee, total)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, staffId);
            ps.setBigDecimal(2, subtotal);
            ps.setBigDecimal(3, tax);
            ps.setBigDecimal(4, serviceFee);
            ps.setBigDecimal(5, total);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot create order", e);
        }

        return -1;
    }

    public void deleteOrder(int orderId) {
        String sql = "DELETE FROM orders WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot delete order", e);
        }
    }

    public List<Order> findByDate(LocalDate date) {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT 
                o.id, 
                o.staff_id, 
                u.username AS staff_name,
                o.subtotal, 
                o.tax, 
                o.service_fee, 
                o.total, 
                o.created_at
            FROM orders o
            JOIN users u ON o.staff_id = u.id
            WHERE DATE(o.created_at) = ?
            ORDER BY o.created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT 
                o.id,
                o.staff_id,
                u.username AS staff_name,
                o.subtotal,
                o.tax,
                o.service_fee,
                o.total,
                o.created_at
            FROM orders o
            JOIN users u ON o.staff_id = u.id
            ORDER BY o.created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT 
                o.id,
                o.staff_id,
                u.username AS staff_name,
                o.subtotal,
                o.tax,
                o.service_fee,
                o.total,
                o.created_at
            FROM orders o
            JOIN users u ON o.staff_id = u.id
            WHERE DATE(o.created_at) BETWEEN ? AND ?
            ORDER BY o.created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, startDate.toString());
            ps.setString(2, endDate.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT 
                o.id,
                o.staff_id,
                u.username AS staff_name,
                o.subtotal,
                o.tax,
                o.service_fee,
                o.total,
                o.created_at
            FROM orders o
            JOIN users u ON o.staff_id = u.id
            WHERE o.total BETWEEN ? AND ?
            ORDER BY o.total DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, minPrice);
            ps.setBigDecimal(2, maxPrice);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> findByStaff(int staffId) {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT 
                o.id,
                o.staff_id,
                u.username AS staff_name,
                o.subtotal,
                o.tax,
                o.service_fee,
                o.total,
                o.created_at
            FROM orders o
            JOIN users u ON o.staff_id = u.id
            WHERE o.staff_id = ?
            ORDER BY o.created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, staffId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<OrderItem> findOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = """
            SELECT 
                oi.id,
                oi.order_id,
                oi.menu_item_id,
                m.name AS menu_item_name,
                oi.quantity,
                oi.unit_price
            FROM order_items oi
            JOIN menu_items m ON oi.menu_item_id = m.id
            WHERE oi.order_id = ?
            ORDER BY m.name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                items.add(mapOrderItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public BigDecimal calculateDailyRevenue(LocalDate date) {
        String sql = """
            SELECT COALESCE(SUM(total), 0) AS total_revenue
            FROM orders
            WHERE DATE(created_at) = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total_revenue");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public int countOrdersByDate(LocalDate date) {
        String sql = """
            SELECT COUNT(*) AS order_count
            FROM orders
            WHERE DATE(created_at) = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("order_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public BigDecimal calculateAverageOrderValue(LocalDate date) {
        String sql = """
            SELECT COALESCE(AVG(total), 0) AS avg_value
            FROM orders
            WHERE DATE(created_at) = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("avg_value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public Map<String, Integer> getBestSellingItems(LocalDate date) {
        Map<String, Integer> itemSales = new HashMap<>();
        String sql = """
            SELECT 
                m.name,
                COALESCE(SUM(oi.quantity), 0) AS total_qty
            FROM menu_items m
            LEFT JOIN order_items oi ON m.id = oi.menu_item_id
            LEFT JOIN orders o ON oi.order_id = o.id
            WHERE DATE(o.created_at) = ? OR o.created_at IS NULL
            GROUP BY m.id, m.name
            ORDER BY total_qty DESC
            LIMIT 10
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                itemSales.put(rs.getString("name"), rs.getInt("total_qty"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itemSales;
    }

    public Map<String, BigDecimal> getBestSellingCategories(LocalDate date) {
        Map<String, BigDecimal> categorySales = new HashMap<>();
        String sql = """
            SELECT 
                mc.name,
                COALESCE(SUM(oi.unit_price * oi.quantity), 0) AS category_revenue
            FROM menu_categories mc
            LEFT JOIN menu_items m ON mc.id = m.category_id
            LEFT JOIN order_items oi ON m.id = oi.menu_item_id
            LEFT JOIN orders o ON oi.order_id = o.id
            WHERE DATE(o.created_at) = ? OR o.created_at IS NULL
            GROUP BY mc.id, mc.name
            ORDER BY category_revenue DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                categorySales.put(rs.getString("name"), rs.getBigDecimal("category_revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categorySales;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        return new Order(
            rs.getInt("id"),
            rs.getInt("staff_id"),
            rs.getString("staff_name"),
            rs.getBigDecimal("subtotal"),
            rs.getBigDecimal("tax"),
            rs.getBigDecimal("service_fee"),
            rs.getBigDecimal("total"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            new ArrayList<>()
        );
    }

    private OrderItem mapOrderItem(ResultSet rs) throws SQLException {
        MenuItem menuItem = new MenuItem(
            rs.getInt("menu_item_id"),
            rs.getString("menu_item_name"),
            "",
            rs.getBigDecimal("unit_price"),
            0,
            null
        );

        return new OrderItem(
            menuItem,
            new ArrayList<>(),
            rs.getInt("quantity")
        );
    }
}
