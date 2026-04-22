package com.oop.project.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import com.oop.project.model.CustomizationOption;
import com.oop.project.model.Order;
import com.oop.project.model.OrderItem;
import com.oop.project.model.User;
import com.oop.project.service.OrderAdminService;
import com.oop.project.ui.theme.AppTheme;
import com.oop.project.ui.theme.ThemeFonts;

public class ManagerOrdersPanel extends JPanel {

    private static final String[] STATUS_OPTIONS = {"OPEN", "SENT_TO_KITCHEN", "PAID", "VOID"};

    private final User currentUser;
    private final OrderAdminService orderAdminService = new OrderAdminService();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JTextField searchField = new JTextField();

    private final JLabel totalOrdersValueLabel = new JLabel("0");
    private final JLabel openOrdersValueLabel = new JLabel("0");
    private final JLabel kitchenOrdersValueLabel = new JLabel("0");
    private final JLabel paidOrdersValueLabel = new JLabel("0");

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

    private final JComboBox<String> statusCombo = new JComboBox<>(STATUS_OPTIONS);
    private final JLabel detailHeaderLabel = new JLabel("Select an order to view details");

    private final JButton applyStatusBtn = new JButton("Update Status");
    private final JButton editItemBtn = new JButton("Edit Selected Item");
    private final JButton removeItemBtn = new JButton("Remove Selected Item");
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
        strip.add(createSummaryCard("In Kitchen", kitchenOrdersValueLabel));
        strip.add(createSummaryCard("Paid", paidOrdersValueLabel));

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

        statusCombo.setFont(ThemeFonts.bodyMd());
        statusCombo.setEnabled(false);

        applyStatusBtn.setEnabled(false);
        applyStatusBtn.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        applyStatusBtn.setBackground(new Color(0x1565C0));
        applyStatusBtn.setForeground(Color.WHITE);
        applyStatusBtn.setFocusPainted(false);

        statusRow.add(statusLabel, BorderLayout.WEST);
        statusRow.add(statusCombo, BorderLayout.CENTER);
        statusRow.add(applyStatusBtn, BorderLayout.EAST);

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

        editItemBtn.setEnabled(false);
        removeItemBtn.setEnabled(false);

        styleActionButton(editItemBtn, new Color(0x2E7D32));
        styleActionButton(removeItemBtn, AppTheme.ERROR);
        styleActionButton(refreshBtn, AppTheme.SURFACE_CONTAINER_HIGH);
        refreshBtn.setForeground(AppTheme.TEXT_PRIMARY);

        actions.add(refreshBtn);
        actions.add(editItemBtn);
        actions.add(removeItemBtn);

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

        itemsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = getSelectedOrderItem() != null;
                editItemBtn.setEnabled(hasSelection);
                removeItemBtn.setEnabled(hasSelection);
            }
        });

        applyStatusBtn.addActionListener(e -> updateStatus());
        editItemBtn.addActionListener(e -> editSelectedItem());
        removeItemBtn.addActionListener(e -> removeSelectedItem());
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
        int kitchen = 0;
        int paid = 0;

        for (Order order : visibleOrders) {
            String status = order.getOrderStatus();
            if ("OPEN".equals(status)) {
                open++;
            } else if ("SENT_TO_KITCHEN".equals(status)) {
                kitchen++;
            } else if ("PAID".equals(status)) {
                paid++;
            }
        }

        totalOrdersValueLabel.setText(String.valueOf(total));
        openOrdersValueLabel.setText(String.valueOf(open));
        kitchenOrdersValueLabel.setText(String.valueOf(kitchen));
        paidOrdersValueLabel.setText(String.valueOf(paid));
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
            statusCombo.setEnabled(false);
            applyStatusBtn.setEnabled(false);
            itemsModel.setRowCount(0);
            editItemBtn.setEnabled(false);
            removeItemBtn.setEnabled(false);
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
            statusCombo.setEnabled(false);
            applyStatusBtn.setEnabled(false);
            itemsModel.setRowCount(0);
            return;
        }

        selectedOrder = visibleOrders.get(row);
        detailHeaderLabel.setText(
            "Order #" + selectedOrder.getId() + " • "
                + selectedOrder.getStaffName() + " • "
                + formatCurrency(selectedOrder.getTotal())
        );

        statusCombo.setSelectedItem(selectedOrder.getOrderStatus());
        statusCombo.setEnabled(true);
        applyStatusBtn.setEnabled(true);

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
        editItemBtn.setEnabled(hasItems);
        removeItemBtn.setEnabled(hasItems);
    }

    private void updateStatus() {
        if (selectedOrder == null) {
            return;
        }

        String selectedStatus = (String) statusCombo.getSelectedItem();
        if (selectedStatus == null || selectedStatus.equals(selectedOrder.getOrderStatus())) {
            return;
        }

        try {
            orderAdminService.updateOrderStatus(currentUser, selectedOrder.getId(), selectedStatus);
            refresh(selectedOrder.getId());
            JOptionPane.showMessageDialog(this, "Order status updated.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Status", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedItem() {
        if (selectedOrder == null) {
            return;
        }

        OrderItem selectedItem = getSelectedOrderItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an item to edit.", "Edit Item", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<CustomizationOption> availableOptions =
            orderAdminService.getCustomizationOptionsForMenuItem(selectedItem.getMenuItem().getId());

        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(selectedItem.getQuantity(), 1, 999, 1));

        JPanel customizationsPanel = new JPanel();
        customizationsPanel.setLayout(new BoxLayout(customizationsPanel, BoxLayout.Y_AXIS));
        customizationsPanel.setOpaque(false);

        Set<Integer> selectedOptionIds = selectedItem.getCustomizations()
            .stream()
            .map(CustomizationOption::getId)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        List<JCheckBox> optionChecks = new ArrayList<>();
        for (CustomizationOption option : availableOptions) {
            JCheckBox optionCheck = new JCheckBox(formatCustomizationOption(option));
            optionCheck.putClientProperty("optionId", option.getId());
            optionCheck.setSelected(selectedOptionIds.contains(option.getId()));
            optionChecks.add(optionCheck);
            customizationsPanel.add(optionCheck);
        }

        JScrollPane customizationsScroll = new JScrollPane(customizationsPanel);
        customizationsScroll.setPreferredSize(new Dimension(320, 160));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel qtyLabel = new JLabel("Quantity");
        qtyLabel.setAlignmentX(LEFT_ALIGNMENT);
        quantitySpinner.setAlignmentX(LEFT_ALIGNMENT);

        JLabel customLabel = new JLabel("Customization options");
        customLabel.setAlignmentX(LEFT_ALIGNMENT);
        customizationsScroll.setAlignmentX(LEFT_ALIGNMENT);

        form.add(qtyLabel);
        form.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        form.add(quantitySpinner);
        form.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        form.add(customLabel);
        form.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        form.add(customizationsScroll);

        int result = JOptionPane.showConfirmDialog(
            this,
            form,
            "Edit Item in Order #" + selectedOrder.getId(),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        List<Integer> newOptionIds = new ArrayList<>();
        for (JCheckBox check : optionChecks) {
            if (check.isSelected()) {
                Object optionId = check.getClientProperty("optionId");
                if (optionId instanceof Integer id) {
                    newOptionIds.add(id);
                }
            }
        }

        int quantity = (Integer) quantitySpinner.getValue();

        try {
            orderAdminService.updateOrderItem(
                currentUser,
                selectedOrder.getId(),
                selectedItem.getId(),
                selectedItem.getMenuItem().getId(),
                quantity,
                newOptionIds
            );
            refresh(selectedOrder.getId());
            JOptionPane.showMessageDialog(this, "Order item updated.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Edit Item", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelectedItem() {
        if (selectedOrder == null) {
            return;
        }

        OrderItem selectedItem = getSelectedOrderItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "Remove Item", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Remove item \"" + selectedItem.getMenuItemName() + "\" from order #" + selectedOrder.getId() + "?",
            "Remove Item",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            orderAdminService.removeOrderItem(currentUser, selectedOrder.getId(), selectedItem.getId());
            refresh(selectedOrder.getId());
            JOptionPane.showMessageDialog(this, "Item removed from order.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Remove Item", JOptionPane.ERROR_MESSAGE);
        }
    }

    private OrderItem getSelectedOrderItem() {
        if (selectedOrder == null) {
            return null;
        }

        int row = itemsTable.getSelectedRow();
        if (row < 0 || row >= selectedOrder.getItems().size()) {
            return null;
        }

        return selectedOrder.getItems().get(row);
    }

    private String summarizeCustomizations(OrderItem orderItem) {
        if (orderItem.getCustomizations().isEmpty()) {
            return "-";
        }

        return orderItem.getCustomizations()
            .stream()
            .map(CustomizationOption::getName)
            .collect(Collectors.joining(", "));
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
