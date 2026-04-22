package com.oop.project.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oop.project.db.DBConnection;
import com.oop.project.model.CustomizationOption;
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
            INSERT INTO orders (staff_id, order_status, subtotal, tax, service_fee, total)
            VALUES (?, 'OPEN', ?, ?, ?, ?)
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
                o.order_status,
                o.subtotal, 
                o.tax, 
                o.service_fee, 
                o.total, 
                o.created_at
            FROM orders o
            JOIN users u ON o.staff_id = u.id
            WHERE o.created_at >= ? AND o.created_at < ?
            ORDER BY o.created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
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
                o.order_status,
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
                o.order_status,
                o.subtotal,
                o.tax,
                o.service_fee,
                o.total,
                o.created_at
            FROM orders o
            JOIN users u ON o.staff_id = u.id
            WHERE o.created_at >= ? AND o.created_at < ?
            ORDER BY o.created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
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
                o.order_status,
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
                o.order_status,
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
                oi.menu_item_id,
                m.name AS menu_item_name,
                oi.quantity,
                oi.unit_price,
                oi.note
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
                items.add(mapOrderItem(rs, orderId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public void updateOrderStatus(int orderId, String newStatus) {
        String normalizedStatus = normalizeStatus(newStatus);
        String sql = "UPDATE orders SET order_status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, normalizedStatus);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot update order status", e);
        }
    }

    public void updateOrderItemQuantity(int orderId, int orderItemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        String updateQtySql = "UPDATE order_items SET quantity = ? WHERE id = ? AND order_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(updateQtySql)) {
                ps.setInt(1, quantity);
                ps.setInt(2, orderItemId);
                ps.setInt(3, orderId);
                ps.executeUpdate();
            }

            recalculateOrderTotals(conn, orderId);
            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("Cannot update order item quantity", e);
        } finally {
            closeConnectionQuietly(conn);
        }
    }

    public void updateOrderItemCustomizations(int orderId, int orderItemId, int menuItemId, List<Integer> customizationIds) {
        String deleteSql = "DELETE FROM order_item_customizations WHERE order_item_id = ?";
        String insertSql = """
            INSERT INTO order_item_customizations (order_id, order_item_id, menu_item_id, customization_id)
            VALUES (?, ?, ?, ?)
        """;
        String basePriceSql = """
            SELECT m.base_price
            FROM order_items oi
            JOIN menu_items m ON m.id = oi.menu_item_id
            WHERE oi.id = ? AND oi.order_id = ?
        """;
        String updateUnitPriceSql = "UPDATE order_items SET unit_price = ? WHERE id = ? AND order_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, orderItemId);
                ps.executeUpdate();
            }

            if (customizationIds != null && !customizationIds.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (Integer customizationId : customizationIds) {
                        if (customizationId == null) {
                            continue;
                        }
                        ps.setInt(1, orderId);
                        ps.setInt(2, orderItemId);
                        ps.setInt(3, menuItemId);
                        ps.setInt(4, customizationId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            BigDecimal basePrice = BigDecimal.ZERO;
            try (PreparedStatement ps = conn.prepareStatement(basePriceSql)) {
                ps.setInt(1, orderItemId);
                ps.setInt(2, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        basePrice = rs.getBigDecimal("base_price");
                    }
                }
            }

            BigDecimal customizationDelta = sumCustomizationDelta(conn, orderItemId);
            BigDecimal newUnitPrice = basePrice.add(customizationDelta);

            try (PreparedStatement ps = conn.prepareStatement(updateUnitPriceSql)) {
                ps.setBigDecimal(1, newUnitPrice);
                ps.setInt(2, orderItemId);
                ps.setInt(3, orderId);
                ps.executeUpdate();
            }

            recalculateOrderTotals(conn, orderId);
            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("Cannot update order item customizations", e);
        } finally {
            closeConnectionQuietly(conn);
        }
    }

    public void deleteOrderItem(int orderId, int orderItemId) {
        String sql = "DELETE FROM order_items WHERE id = ? AND order_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orderItemId);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }

            recalculateOrderTotals(conn, orderId);
            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("Cannot remove order item", e);
        } finally {
            closeConnectionQuietly(conn);
        }
    }

    public BigDecimal calculateDailyRevenue(LocalDate date) {
        String sql = """
            SELECT COALESCE(SUM(total), 0) AS total_revenue
            FROM orders
            WHERE created_at >= ? AND created_at < ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
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
            WHERE created_at >= ? AND created_at < ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
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
            WHERE (o.created_at >= ? AND o.created_at < ?) OR o.created_at IS NULL
            GROUP BY m.id, m.name
            ORDER BY total_qty DESC
            LIMIT 10
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
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
            WHERE (o.created_at >= ? AND o.created_at < ?) OR o.created_at IS NULL
            GROUP BY mc.id, mc.name
            ORDER BY category_revenue DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
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
            rs.getString("order_status"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            new ArrayList<>()
        );
    }

    private OrderItem mapOrderItem(ResultSet rs, int orderId) throws SQLException {
        int orderItemId = rs.getInt("id");
        MenuItem menuItem = new MenuItem(
            rs.getInt("menu_item_id"),
            rs.getString("menu_item_name"),
            "",
            rs.getBigDecimal("unit_price"),
            0,
            null
        );

        List<CustomizationOption> customizations = new CustomizationOptionRepository().findByOrderItemId(orderItemId);
        OrderItem item = new OrderItem(
            menuItem,
            customizations,
            rs.getInt("quantity"),
            rs.getString("note")
        );
        item.setId(orderItemId);
        return item;
    }

    public int submitOrder(com.oop.project.model.OrderDraft draft, int staffId, BigDecimal subtotal, BigDecimal tax, BigDecimal serviceFee, BigDecimal total) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int orderId = createOrderHeader(conn, staffId, subtotal, tax, serviceFee, total);

            for (OrderItem item : draft.getItems()) {
                int orderItemId = addOrderItem(conn, orderId, item.getMenuItem().getId(), item.getQuantity(), item.getUnitPrice(), item.getNote());
                for (com.oop.project.model.CustomizationOption customization : item.getCustomizations()) {
                    addOrderItemCustomization(conn, orderId, orderItemId, item.getMenuItem().getId(), customization.getId());
                }
            }

            conn.commit();
            return orderId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw new RuntimeException("Failed to submit order: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int createOrderHeader(Connection conn, int staffId, BigDecimal subtotal, BigDecimal tax, BigDecimal serviceFee, BigDecimal total) throws SQLException {
        String sql = """
            INSERT INTO orders (staff_id, order_status, subtotal, tax, service_fee, total)
            VALUES (?, 'OPEN', ?, ?, ?, ?)
            RETURNING id
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
        }
        throw new SQLException("Failed to insert order header");
    }

    private int addOrderItem(Connection conn, int orderId, int menuItemId, int quantity, BigDecimal unitPrice, String note) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price, note) VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, menuItemId);
            ps.setInt(3, quantity);
            ps.setBigDecimal(4, unitPrice);
            ps.setString(5, note == null ? "" : note.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        throw new SQLException("Failed to insert order item");
    }

    private void addOrderItemCustomization(Connection conn, int orderId, int orderItemId, int menuItemId, int customizationId) throws SQLException {
        String sql = """
            INSERT INTO order_item_customizations (order_id, order_item_id, menu_item_id, customization_id)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, orderItemId);
            ps.setInt(3, menuItemId);
            ps.setInt(4, customizationId);
            ps.executeUpdate();
        }
    }

    private BigDecimal sumCustomizationDelta(Connection conn, int orderItemId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(c.price_delta), 0) AS total_delta
            FROM order_item_customizations oic
            JOIN customization_options c ON c.id = oic.customization_id
            WHERE oic.order_item_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total_delta");
                }
            }
        }

        return BigDecimal.ZERO;
    }

    private void recalculateOrderTotals(Connection conn, int orderId) throws SQLException {
        String subtotalSql = """
            SELECT COALESCE(SUM(unit_price * quantity), 0) AS subtotal
            FROM order_items
            WHERE order_id = ?
        """;
        String updateSql = """
            UPDATE orders
            SET subtotal = ?,
                tax = ?,
                service_fee = ?,
                total = ?
            WHERE id = ?
        """;

        BigDecimal subtotal = BigDecimal.ZERO;
        try (PreparedStatement ps = conn.prepareStatement(subtotalSql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    subtotal = rs.getBigDecimal("subtotal");
                }
            }
        }

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10"));
        BigDecimal serviceFee = subtotal.multiply(new BigDecimal("0.05"));
        BigDecimal total = subtotal.add(tax).add(serviceFee);

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setBigDecimal(1, subtotal);
            ps.setBigDecimal(2, tax);
            ps.setBigDecimal(3, serviceFee);
            ps.setBigDecimal(4, total);
            ps.setInt(5, orderId);
            ps.executeUpdate();
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Order status is required");
        }

        String normalized = status.trim().toUpperCase();
        if (!"OPEN".equals(normalized)
            && !"SENT_TO_KITCHEN".equals(normalized)
            && !"PAID".equals(normalized)
            && !"VOID".equals(normalized)) {
            throw new IllegalArgumentException("Unsupported order status: " + status);
        }

        return normalized;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // no-op
        }
    }

    private void closeConnectionQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException ignored) {
            // no-op
        }
    }
}
