package com.oop.project.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.oop.project.model.CustomizationOption;
import com.oop.project.model.Order;
import com.oop.project.model.OrderItem;
import com.oop.project.model.User;
import com.oop.project.service.OrderAdminService;
import com.oop.project.ui.theme.AppTheme;
import com.oop.project.ui.theme.ThemeFonts;

public class ManagerOrdersPanel extends JPanel {

    // ── Status colours ──────────────────────────────────────────────────────────
    private static final Color STATUS_OPEN_COLOR      = new Color(0x1565C0);
    private static final Color STATUS_PAID_COLOR      = new Color(0x1B8A3C);
    private static final Color STATUS_CANCELLED_COLOR = new Color(0xC62828);

    // ── Table accent colours ─────────────────────────────────────────────────────
    private static final Color ROW_EVEN  = Color.WHITE;
    private static final Color ROW_ODD   = new Color(0xF8F9FA);
    private static final Color ROW_SEL   = new Color(0xBBDEFB);
    private static final Color HDR_BG    = new Color(0xF1F3F4);
    private static final Color HDR_FG    = new Color(0x5F6368);
    private static final Color BORDER_CLR = new Color(0xE0E0E0);

    // ── Services / formatters ────────────────────────────────────────────────────
    private final User currentUser;
    private final OrderAdminService orderAdminService = new OrderAdminService();
    private final DecimalFormat priceFormat           = new DecimalFormat("#,##0");
    private final DateTimeFormatter dtFormatter       = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ── Summary labels ───────────────────────────────────────────────────────────
    private final JLabel totalOrdersValueLabel    = new JLabel("0");
    private final JLabel openOrdersValueLabel     = new JLabel("0");
    private final JLabel paidOrdersValueLabel     = new JLabel("0");
    private final JLabel cancelledOrdersValueLabel = new JLabel("0");

    // ── Search ───────────────────────────────────────────────────────────────────
    private final JTextField searchField = new JTextField();

    // ── Orders table ─────────────────────────────────────────────────────────────
    private final JTable ordersTable = new JTable();
    private final DefaultTableModel ordersModel = new DefaultTableModel(
        new Object[]{"ORDER", "TIME", "STAFF", "STATUS", "ITEMS", "TOTAL"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    // ── Items table ──────────────────────────────────────────────────────────────
    private final JTable itemsTable = new JTable();
    private final DefaultTableModel itemsModel = new DefaultTableModel(
        new Object[]{"Line ID", "Item", "Customizations", "Qty", "Line Total"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    // ── Detail widgets ───────────────────────────────────────────────────────────
    private final JLabel detailHeaderLabel = new JLabel("Select an order to view details");
    private final JLabel statusValueBadge  = new JLabel("-");
    private final JButton markPaidBtn    = new JButton("Mark Paid");
    private final JButton cancelOrderBtn = new JButton("Cancel Order");
    private final JButton refreshBtn     = new JButton("Refresh");

    // ── State ─────────────────────────────────────────────────────────────────────
    private List<Order> visibleOrders = new ArrayList<>();
    private Order selectedOrder;

    // ═════════════════════════════════════════════════════════════════════════════
    public ManagerOrdersPanel(User currentUser) {
        this.currentUser = currentUser;

        setLayout(new BorderLayout(AppTheme.SPACE_3, AppTheme.SPACE_3));
        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_4, AppTheme.SPACE_4, AppTheme.SPACE_4, AppTheme.SPACE_4));

        add(buildSummaryStrip(), BorderLayout.NORTH);
        add(buildBody(),         BorderLayout.CENTER);

        bindEvents();
        refresh();
    }

    // ── Public API ───────────────────────────────────────────────────────────────
    public void refresh() { refresh(null); }

    public void refresh(Integer preferredOrderId) {
        String kw = searchField.getText() == null ? "" : searchField.getText().trim();
        loadOrders(kw, preferredOrderId);
    }

    // ── Summary strip ────────────────────────────────────────────────────────────
    private JPanel buildSummaryStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 4, AppTheme.SPACE_3, 0));
        strip.setOpaque(false);
        strip.add(createSummaryCard("Total Orders", totalOrdersValueLabel,    new Color(0xE3F2FD), new Color(0x1565C0)));
        strip.add(createSummaryCard("Open",         openOrdersValueLabel,     new Color(0xFFF8E1), new Color(0xF57F17)));
        strip.add(createSummaryCard("Paid",         paidOrdersValueLabel,     new Color(0xE8F5E9), new Color(0x1B8A3C)));
        strip.add(createSummaryCard("Cancelled",    cancelledOrdersValueLabel, new Color(0xFFEBEE), new Color(0xC62828)));
        return strip;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color bgColor, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // left accent bar
                g2.setColor(accentColor);
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            BorderFactory.createEmptyBorder(AppTheme.SPACE_3, AppTheme.SPACE_4 + 4, AppTheme.SPACE_3, AppTheme.SPACE_3)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        titleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        valueLabel.setFont(ThemeFonts.displayMd());
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        card.add(valueLabel);
        return card;
    }

    // ── Body (left table + right detail) ─────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(AppTheme.SPACE_3, 0));
        body.setOpaque(false);
        body.add(buildOrdersListPanel(), BorderLayout.CENTER);
        body.add(buildDetailsPanel(),    BorderLayout.EAST);
        return body;
    }

    // ── Left: orders list ─────────────────────────────────────────────────────────
    private JPanel buildOrdersListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, AppTheme.SPACE_2));
        panel.setOpaque(false);

        // ── toolbar row
        JPanel toolbar = new JPanel(new BorderLayout(AppTheme.SPACE_3, 0));
        toolbar.setOpaque(false);

        JLabel title = new JLabel("Orders Overview");
        title.setFont(ThemeFonts.titleLg());
        title.setForeground(AppTheme.TEXT_PRIMARY);

        // Search field with placeholder appearance
        searchField.setFont(ThemeFonts.bodyMd());
        searchField.setToolTipText("Search orders…");
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(AppTheme.TEXT_PRIMARY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            BorderFactory.createEmptyBorder(6, AppTheme.SPACE_2, 6, AppTheme.SPACE_2)
        ));
        // Hint text
        searchField.putClientProperty("JTextField.placeholderText", "Search orders...");

        toolbar.add(title,       BorderLayout.WEST);
        toolbar.add(searchField, BorderLayout.CENTER);

        // ── table
        styleTable(ordersTable, ordersModel);
        ordersTable.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());
        // Column widths
        int[] widths = {55, 115, 80, 80, 55, 100};
        for (int i = 0; i < widths.length; i++) {
            ordersTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = styledScrollPane(ordersTable);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll,  BorderLayout.CENTER);
        return panel;
    }

    // ── Right: order detail ───────────────────────────────────────────────────────
    private JPanel buildDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, AppTheme.SPACE_2));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(460, 0));

        // ── header card
        JPanel headerCard = new JPanel(new BorderLayout(AppTheme.SPACE_2, AppTheme.SPACE_2));
        headerCard.setBackground(Color.WHITE);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            BorderFactory.createEmptyBorder(AppTheme.SPACE_3, AppTheme.SPACE_3, AppTheme.SPACE_3, AppTheme.SPACE_3)
        ));

        detailHeaderLabel.setFont(ThemeFonts.titleLg());
        detailHeaderLabel.setForeground(AppTheme.TEXT_PRIMARY);

        // Status row: "Order status:" label | badge | spacer | Mark Paid | Cancel Order
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_2, 0));
        statusRow.setOpaque(false);

        JLabel statusLbl = new JLabel("Order status:");
        statusLbl.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        statusLbl.setForeground(AppTheme.TEXT_SECONDARY);

        statusValueBadge.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        statusValueBadge.setOpaque(true);
        statusValueBadge.setHorizontalAlignment(SwingConstants.CENTER);
        statusValueBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        styleStatusBadge(null);

        styleBtn(markPaidBtn,    STATUS_OPEN_COLOR);
        styleBtn(cancelOrderBtn, STATUS_CANCELLED_COLOR);
        markPaidBtn.setEnabled(false);
        cancelOrderBtn.setEnabled(false);

        statusRow.add(statusLbl);
        statusRow.add(statusValueBadge);
        statusRow.add(Box.createHorizontalStrut(AppTheme.SPACE_2));
        statusRow.add(markPaidBtn);
        statusRow.add(cancelOrderBtn);

        headerCard.add(detailHeaderLabel, BorderLayout.NORTH);
        headerCard.add(statusRow,         BorderLayout.CENTER);

        // ── items table
        styleTable(itemsTable, itemsModel);
        int[] iWidths = {55, 110, 130, 45, 90};
        for (int i = 0; i < iWidths.length; i++) {
            itemsTable.getColumnModel().getColumn(i).setPreferredWidth(iWidths[i]);
        }
        JScrollPane itemsScroll = styledScrollPane(itemsTable);

        // ── bottom actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        styleBtn(refreshBtn, new Color(0x5F6368));
        actions.add(refreshBtn);

        panel.add(headerCard,   BorderLayout.NORTH);
        panel.add(itemsScroll,  BorderLayout.CENTER);
        panel.add(actions,      BorderLayout.SOUTH);
        return panel;
    }

    // ── Shared table styling ──────────────────────────────────────────────────────
    private void styleTable(JTable table, DefaultTableModel model) {
        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xEEEEEE));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(ThemeFonts.bodyMd());
        table.setBackground(Color.WHITE);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.setSelectionBackground(ROW_SEL);
        table.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        table.setFocusable(false);

        JTableHeader hdr = table.getTableHeader();
        hdr.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        hdr.setBackground(HDR_BG);
        hdr.setForeground(HDR_FG);
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR));
        hdr.setReorderingAllowed(false);
        hdr.setDefaultRenderer(new HeaderRenderer());

        table.setDefaultRenderer(Object.class, new ZebraRenderer());
    }

    private JScrollPane styledScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_CLR, 1));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // ── Button helper ─────────────────────────────────────────────────────────────
    private void styleBtn(JButton btn, Color bg) {
        btn.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        Color hover = bg.darker();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(hover);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
    }

    // ── Events ────────────────────────────────────────────────────────────────────
    private void bindEvents() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { onSearch(); }
            @Override public void removeUpdate(DocumentEvent e)  { onSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { onSearch(); }
            private void onSearch() { loadOrders(searchField.getText().trim(), null); }
        });

        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) applySelectedOrder();
        });

        markPaidBtn.addActionListener(e    -> updateStatus(Order.STATUS_PAID));
        cancelOrderBtn.addActionListener(e -> updateStatus(Order.STATUS_CANCELLED));
        refreshBtn.addActionListener(e     -> refresh(selectedOrder == null ? null : selectedOrder.getId()));
    }

    // ── Data loading ──────────────────────────────────────────────────────────────
    private void loadOrders(String keyword, Integer preferredOrderId) {
        try {
            visibleOrders = (keyword == null || keyword.isBlank())
                ? orderAdminService.getAllOrdersWithItems()
                : orderAdminService.searchOrders(keyword);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Orders", JOptionPane.ERROR_MESSAGE);
            visibleOrders = new ArrayList<>();
        }
        updateSummary();
        fillOrdersTable(preferredOrderId);
    }

    private void updateSummary() {
        int total = visibleOrders.size(), open = 0, paid = 0, cancelled = 0;
        for (Order o : visibleOrders) {
            if      (o.isOpen())      open++;
            else if (o.isPaid())      paid++;
            else if (o.isCancelled()) cancelled++;
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
            Order o = visibleOrders.get(i);
            ordersModel.addRow(new Object[]{
                o.getId(),
                o.getCreatedAt() == null ? "-" : o.getCreatedAt().format(dtFormatter),
                o.getStaffName(),
                o.getOrderStatus(),
                o.getItemCount(),
                formatCurrency(o.getTotal())
            });
            if (preferredOrderId != null && o.getId() == preferredOrderId.intValue()) selectedRow = i;
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

        if (selectedRow < 0) selectedRow = 0;
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
            "Order #" + selectedOrder.getId()
            + "  •  " + selectedOrder.getStaffName()
            + "  •  " + formatCurrency(selectedOrder.getTotal())
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
            itemsModel.addRow(new Object[]{
                item.getId(),
                item.getMenuItemName(),
                summarizeCustomizations(item),
                item.getQuantity(),
                formatCurrency(item.getLineTotal())
            });
        }
        if (itemsModel.getRowCount() > 0) itemsTable.setRowSelectionInterval(0, 0);
    }

    private void updateStatus(String newStatus) {
        if (selectedOrder == null || !selectedOrder.isOpen()) return;
        try {
            orderAdminService.updateOrderStatus(currentUser, selectedOrder.getId(), newStatus);
            refresh(selectedOrder.getId());
            String action = Order.STATUS_PAID.equals(newStatus) ? "marked as paid" : "cancelled";
            JOptionPane.showMessageDialog(this, "Order " + action + ".");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Status", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Status badge styling ──────────────────────────────────────────────────────
    private void styleStatusBadge(String status) {
        if (status == null || status.isBlank()) {
            statusValueBadge.setText("  -  ");
            statusValueBadge.setForeground(AppTheme.TEXT_SECONDARY);
            statusValueBadge.setBackground(AppTheme.SURFACE_CONTAINER);
            return;
        }
        statusValueBadge.setText("  " + status + "  ");
        statusValueBadge.setForeground(Color.WHITE);
        statusValueBadge.setBackground(getStatusColor(status));
    }

    private Color getStatusColor(String status) {
        if (Order.STATUS_OPEN.equals(status))      return STATUS_OPEN_COLOR;
        if (Order.STATUS_PAID.equals(status))      return STATUS_PAID_COLOR;
        if (Order.STATUS_CANCELLED.equals(status)) return STATUS_CANCELLED_COLOR;
        return AppTheme.TEXT_SECONDARY;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────
    private String summarizeCustomizations(OrderItem item) {
        String opts = item.getCustomizations().isEmpty()
            ? "-"
            : item.getCustomizations().stream()
                .map(CustomizationOption::getName)
                .collect(Collectors.joining(", "));
        String note = item.getNote() == null ? "" : item.getNote().trim();
        return note.isBlank() ? opts : opts + " | Note: " + note;
    }

    private String formatCurrency(BigDecimal amount) {
        return amount == null ? "0 VND" : priceFormat.format(amount) + " VND";
    }

    // ── Custom renderers ──────────────────────────────────────────────────────────

    /** Coloured status pill in the orders table */
    private class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            String status = value == null ? "" : value.toString();
            setHorizontalAlignment(CENTER);
            setText(status);
            setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
            Color statusColor = getStatusColor(status);
            if (isSelected) {
                setForeground(Color.WHITE);
                setBackground(statusColor.darker());
            } else {
                setForeground(Color.WHITE);
                setBackground(statusColor);
            }
            setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            return this;
        }
    }

    /** Zebra-striped rows for the generic object renderer */
    private class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setFont(ThemeFonts.bodyMd());
            setBorder(BorderFactory.createEmptyBorder(0, AppTheme.SPACE_2, 0, AppTheme.SPACE_2));
            if (isSelected) {
                setBackground(ROW_SEL);
                setForeground(AppTheme.TEXT_PRIMARY);
            } else {
                setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                setForeground(AppTheme.TEXT_PRIMARY);
            }
            return this;
        }
    }

    /** Styled table header renderer */
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setHorizontalAlignment(LEFT);
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
            setBackground(HDR_BG);
            setForeground(HDR_FG);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR),
                BorderFactory.createEmptyBorder(4, AppTheme.SPACE_2, 4, AppTheme.SPACE_2)
            ));
            return this;
        }
    }
}
