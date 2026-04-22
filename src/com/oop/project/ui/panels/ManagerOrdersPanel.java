package com.oop.project.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.oop.project.model.CustomizationOption;
import com.oop.project.model.Order;
import com.oop.project.model.OrderItem;
import com.oop.project.model.User;
import com.oop.project.service.OrderAdminService;
import com.oop.project.ui.theme.AppTheme;
import com.oop.project.ui.theme.ThemeFonts;

public class ManagerOrdersPanel extends JPanel {

    private static final Color STATUS_OPEN_COLOR = new Color(0x1565C0);
    private static final Color STATUS_PAID_COLOR = new Color(0x1B8A3C);
    private static final Color STATUS_CANCELLED_COLOR = new Color(0xC62828);

    private final User currentUser;
    private final OrderAdminService orderAdminService = new OrderAdminService();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JTextField searchField = new JTextField();

    private final JLabel totalOrdersValueLabel = new JLabel("0");
    private final JLabel openOrdersValueLabel = new JLabel("0");
    private final JLabel paidOrdersValueLabel = new JLabel("0");
    private final JLabel cancelledOrdersValueLabel = new JLabel("0");

    private final JTable ordersTable = new JTable();
    private final DefaultTableModel ordersModel = new DefaultTableModel(
        new Object[] {"Order", "Time", "Staff", "Status", "Items", "Total"},
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable itemsTable = new JTable();
    private final DefaultTableModel itemsModel = new DefaultTableModel(
        new Object[] {"Line ID", "Item", "Customizations", "Qty", "Line Total"},
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JLabel statusValueBadge = new JLabel("-");
    private final JLabel detailHeaderLabel = new JLabel("Select an order to view details");

    private final JButton markPaidBtn = new JButton("Mark Paid");
    private final JButton cancelOrderBtn = new JButton("Cancel Order");
    private final JButton refreshBtn = new JButton("Refresh");

    private List<Order> visibleOrders = new ArrayList<>();
    private Order selectedOrder;

    public ManagerOrdersPanel(User currentUser) {
        this.currentUser = currentUser;

        setLayout(new BorderLayout(AppTheme.SPACE_4, AppTheme.SPACE_4));
        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_4,
            AppTheme.SPACE_4,
            AppTheme.SPACE_4,
            AppTheme.SPACE_4
        ));

        add(buildSummaryStrip(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        bindEvents();
        refresh();
    }

    public void refresh() {
        refresh(null);
    }

    public void refresh(Integer preferredOrderId) {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        loadOrders(keyword, preferredOrderId);
    }

    private JPanel buildSummaryStrip() {
        JPanel strip = new JPanel(new java.awt.GridLayout(1, 4, AppTheme.SPACE_2, 0));
        strip.setOpaque(false);

        strip.add(createSummaryCard("Total Orders", totalOrdersValueLabel));
        strip.add(createSummaryCard("Open", openOrdersValueLabel));
        strip.add(createSummaryCard("Paid", paidOrdersValueLabel));
        strip.add(createSummaryCard("Cancelled", cancelledOrdersValueLabel));

        return strip;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.SURFACE_CONTAINER_LOWEST);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.OUTLINE, 1),
            BorderFactory.createEmptyBorder(AppTheme.SPACE_3, AppTheme.SPACE_3, AppTheme.SPACE_3, AppTheme.SPACE_3)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        titleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        valueLabel.setFont(ThemeFonts.titleLg().deriveFont(Font.BOLD));
        valueLabel.setForeground(AppTheme.TEXT_PRIMARY);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        card.add(valueLabel);

        return card;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(AppTheme.SPACE_3, 0));
        body.setOpaque(false);

        body.add(buildOrdersListPanel(), BorderLayout.CENTER);
        body.add(buildDetailsPanel(), BorderLayout.EAST);

        return body;
    }

    private JPanel buildOrdersListPanel() {
        JPanel panel = new JPanel(new BorderLayout(AppTheme.SPACE_2, AppTheme.SPACE_2));
        panel.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(AppTheme.SPACE_2, 0));
        top.setOpaque(false);

        JLabel titleLabel = new JLabel("Orders Overview");
        titleLabel.setFont(ThemeFonts.titleLg());
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);

        searchField.setFont(ThemeFonts.bodyMd());
        searchField.setToolTipText("Search by order id, staff, status, or item name");
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.OUTLINE, 1),
            BorderFactory.createEmptyBorder(AppTheme.SPACE_1, AppTheme.SPACE_2, AppTheme.SPACE_1, AppTheme.SPACE_2)
        ));

        top.add(titleLabel, BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);

        ordersTable.setModel(ordersModel);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.setRowHeight(28);
        ordersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        ordersTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());

        JScrollPane ordersScroll = new JScrollPane(ordersTable);
        ordersScroll.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE, 1));

        panel.add(top, BorderLayout.NORTH);
        panel.add(ordersScroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(AppTheme.SPACE_2, AppTheme.SPACE_2));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(460, 0));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        detailHeaderLabel.setFont(ThemeFonts.titleLg());
        detailHeaderLabel.setForeground(AppTheme.TEXT_PRIMARY);
        detailHeaderLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel statusRow = new JPanel(new BorderLayout(AppTheme.SPACE_2, 0));
        statusRow.setOpaque(false);
        statusRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel statusLabel = new JLabel("Order status:");
        statusLabel.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        statusLabel.setForeground(AppTheme.TEXT_SECONDARY);

        statusValueBadge.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        statusValueBadge.setOpaque(true);
        statusValueBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.OUTLINE, 1),
            BorderFactory.createEmptyBorder(AppTheme.SPACE_1, AppTheme.SPACE_2, AppTheme.SPACE_1, AppTheme.SPACE_2)
        ));
        styleStatusBadge(null);

        markPaidBtn.setEnabled(false);
        markPaidBtn.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        markPaidBtn.setBackground(STATUS_OPEN_COLOR);
        markPaidBtn.setForeground(Color.WHITE);
        markPaidBtn.setFocusPainted(false);

        cancelOrderBtn.setEnabled(false);
        cancelOrderBtn.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        cancelOrderBtn.setBackground(STATUS_CANCELLED_COLOR);
        cancelOrderBtn.setForeground(Color.WHITE);
        cancelOrderBtn.setFocusPainted(false);

        JPanel statusActionsPanel = new JPanel(new GridLayout(1, 2, AppTheme.SPACE_2, 0));
        statusActionsPanel.setOpaque(false);
        statusActionsPanel.add(markPaidBtn);
        statusActionsPanel.add(cancelOrderBtn);

        statusRow.add(statusLabel, BorderLayout.WEST);
        statusRow.add(statusValueBadge, BorderLayout.CENTER);
        statusRow.add(statusActionsPanel, BorderLayout.EAST);

        top.add(detailHeaderLabel);
        top.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        top.add(statusRow);

        itemsTable.setModel(itemsModel);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsTable.setRowHeight(28);
        itemsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane itemsScroll = new JScrollPane(itemsTable);
        itemsScroll.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE, 1));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACE_2, 0));
        actions.setOpaque(false);

        styleActionButton(refreshBtn, AppTheme.SURFACE_CONTAINER_HIGH);
        refreshBtn.setForeground(AppTheme.TEXT_PRIMARY);

        actions.add(refreshBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(itemsScroll, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void styleActionButton(JButton button, Color bg) {
        button.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    private void bindEvents() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                loadOrders(searchField.getText().trim(), null);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                loadOrders(searchField.getText().trim(), null);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                loadOrders(searchField.getText().trim(), null);
            }
        });

        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                applySelectedOrder();
            }
        });

        markPaidBtn.addActionListener(e -> updateStatus(Order.STATUS_PAID));
        cancelOrderBtn.addActionListener(e -> updateStatus(Order.STATUS_CANCELLED));
        refreshBtn.addActionListener(e -> refresh(selectedOrder == null ? null : selectedOrder.getId()));
    }

    private void loadOrders(String keyword, Integer preferredOrderId) {
        try {
            if (keyword == null || keyword.isBlank()) {
                visibleOrders = orderAdminService.getAllOrdersWithItems();
            } else {
                visibleOrders = orderAdminService.searchOrders(keyword);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Orders", JOptionPane.ERROR_MESSAGE);
            visibleOrders = new ArrayList<>();
        }

        updateSummary();
        fillOrdersTable(preferredOrderId);
    }

    private void updateSummary() {
        int total = visibleOrders.size();
        int open = 0;
        int paid = 0;
        int cancelled = 0;

        for (Order order : visibleOrders) {
            if (order.isOpen()) {
                open++;
            } else if (order.isPaid()) {
                paid++;
            } else if (order.isCancelled()) {
                cancelled++;
            }
        }

        totalOrdersValueLabel.setText(String.valueOf(total));
        openOrdersValueLabel.setText(String.valueOf(open));
        paidOrdersValueLabel.setText(String.valueOf(paid));
        cancelledOrdersValueLabel.setText(String.valueOf(cancelled));
    }

    private void fillOrdersTable(Integer preferredOrderId) {
        ordersModel.setRowCount(0);

        int selectedRow = -1;
        for (int i = 0; i < visibleOrders.size(); i++) {
            Order order = visibleOrders.get(i);
            ordersModel.addRow(new Object[] {
                order.getId(),
                order.getCreatedAt() == null ? "-" : order.getCreatedAt().format(dateTimeFormatter),
                order.getStaffName(),
                order.getOrderStatus(),
                order.getItemCount(),
                formatCurrency(order.getTotal())
            });

            if (preferredOrderId != null && order.getId() == preferredOrderId.intValue()) {
                selectedRow = i;
            }
        }

        if (ordersModel.getRowCount() == 0) {
            selectedOrder = null;
            detailHeaderLabel.setText("No orders found");
            markPaidBtn.setEnabled(false);
            cancelOrderBtn.setEnabled(false);
            styleStatusBadge(null);
            itemsModel.setRowCount(0);
            return;
        }

        if (selectedRow < 0) {
            selectedRow = 0;
        }

        ordersTable.setRowSelectionInterval(selectedRow, selectedRow);
        applySelectedOrder();
    }

    private void applySelectedOrder() {
        int row = ordersTable.getSelectedRow();
        if (row < 0 || row >= visibleOrders.size()) {
            selectedOrder = null;
            detailHeaderLabel.setText("Select an order to view details");
            markPaidBtn.setEnabled(false);
            cancelOrderBtn.setEnabled(false);
            styleStatusBadge(null);
            itemsModel.setRowCount(0);
            return;
        }

        selectedOrder = visibleOrders.get(row);
        detailHeaderLabel.setText(
            "Order #" + selectedOrder.getId() + " • "
                + selectedOrder.getStaffName() + " • "
                + formatCurrency(selectedOrder.getTotal())
        );

        styleStatusBadge(selectedOrder.getOrderStatus());
        boolean open = selectedOrder.isOpen();
        markPaidBtn.setEnabled(open);
        cancelOrderBtn.setEnabled(open);

        fillItemsTable(selectedOrder);
    }

    private void fillItemsTable(Order order) {
        itemsModel.setRowCount(0);

        for (OrderItem item : order.getItems()) {
            itemsModel.addRow(new Object[] {
                item.getId(),
                item.getMenuItemName(),
                summarizeCustomizations(item),
                item.getQuantity(),
                formatCurrency(item.getLineTotal())
            });
        }

        boolean hasItems = itemsModel.getRowCount() > 0;
        if (hasItems) {
            itemsTable.setRowSelectionInterval(0, 0);
        }
    }

    private void updateStatus(String newStatus) {
        if (selectedOrder == null) {
            return;
        }

        if (!selectedOrder.isOpen()) {
            return;
        }

        try {
            orderAdminService.updateOrderStatus(currentUser, selectedOrder.getId(), newStatus);
            refresh(selectedOrder.getId());
            String action = Order.STATUS_PAID.equals(newStatus) ? "marked as paid" : "cancelled";
            JOptionPane.showMessageDialog(this, "Order " + action + ".");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Status", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleStatusBadge(String status) {
        if (status == null || status.isBlank()) {
            statusValueBadge.setText("-");
            statusValueBadge.setForeground(AppTheme.TEXT_SECONDARY);
            statusValueBadge.setBackground(AppTheme.SURFACE_CONTAINER_LOWEST);
            return;
        }

        statusValueBadge.setText(status);
        Color color = getStatusColor(status);
        statusValueBadge.setForeground(Color.WHITE);
        statusValueBadge.setBackground(color);
    }

    private Color getStatusColor(String status) {
        if (Order.STATUS_OPEN.equals(status)) {
            return STATUS_OPEN_COLOR;
        }
        if (Order.STATUS_PAID.equals(status)) {
            return STATUS_PAID_COLOR;
        }
        if (Order.STATUS_CANCELLED.equals(status)) {
            return STATUS_CANCELLED_COLOR;
        }
        return AppTheme.TEXT_SECONDARY;
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value == null ? "" : value.toString();
            setHorizontalAlignment(CENTER);
            setText(status);

            if (isSelected) {
                setForeground(Color.WHITE);
                setBackground(getStatusColor(status).darker());
            } else {
                setForeground(Color.WHITE);
                setBackground(getStatusColor(status));
            }
            return this;
        }
    }

    private String summarizeCustomizations(OrderItem orderItem) {
        String customizationSummary = orderItem.getCustomizations().isEmpty()
            ? "-"
            : orderItem.getCustomizations()
            .stream()
            .map(CustomizationOption::getName)
            .collect(Collectors.joining(", "));

        String note = orderItem.getNote() == null ? "" : orderItem.getNote().trim();
        if (!note.isBlank()) {
            return customizationSummary + " | Note: " + note;
        }

        return customizationSummary;
    }

    private String formatCustomizationOption(CustomizationOption option) {
        BigDecimal delta = option.getPriceDelta();
        if (delta == null || delta.compareTo(BigDecimal.ZERO) == 0) {
            return option.getName();
        }

        String sign = delta.compareTo(BigDecimal.ZERO) > 0 ? "+" : "-";
        return option.getName() + " (" + sign + formatCurrency(delta.abs()) + ")";
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 VND";
        }
        return priceFormat.format(amount) + " VND";
    }
}
