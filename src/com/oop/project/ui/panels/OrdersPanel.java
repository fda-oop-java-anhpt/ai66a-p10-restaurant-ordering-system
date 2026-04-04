package com.oop.project.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import com.oop.project.model.CustomizationOption;
import com.oop.project.model.MenuItem;
import com.oop.project.model.OrderDraft;
import com.oop.project.model.OrderItem;
import com.oop.project.model.User;
import com.oop.project.service.OrderService;

public class OrdersPanel extends JPanel {

    private final User currentUser;
    private final OrderService orderService = new OrderService();
    private OrderDraft currentDraft;

    private final JComboBox<MenuItem> menuCombo = new JComboBox<>();
    private final JList<CustomizationOption> customizationList = new JList<>(new DefaultListModel<>());
    private final JTextField quantityField = new JTextField("1");
    private final JButton decreaseQtyBtn = new JButton("-");
    private final JButton increaseQtyBtn = new JButton("+");

    private final JLabel unitPriceLabel = new JLabel("Unit price: 0");
    private final JLabel lineTotalLabel = new JLabel("Line total: 0");
    private final JLabel subtotalLabel = new JLabel("Subtotal: 0");

    private final JButton newOrderBtn = new JButton("New Order");
    private final JButton addOrUpdateBtn = new JButton("Add Item");
    private final JButton removeBtn = new JButton("Remove Selected");

    private final JTable orderTable = new JTable();
    private final DefaultTableModel orderModel = new DefaultTableModel(
        new Object[] {"No.", "Item", "Customizations", "Qty", "Unit Price", "Line Total"},
        0
    );

    private boolean applyingSelection = false;

    public OrdersPanel(User currentUser) {
        this.currentUser = currentUser;
        this.currentDraft = orderService.createOrder(currentUser.getId());

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buildEditorPanel();
        buildTablePanel();
        bindEvents();

        loadMenuItems();
        refreshTable();
        updatePreviewAndSubtotal();
    }

    private void buildEditorPanel() {
        JPanel editor = new JPanel(new GridLayout(0, 1, 0, 6));
        editor.setPreferredSize(new Dimension(320, 0));

        editor.add(new JLabel("Menu Item"));
        editor.add(menuCombo);

        editor.add(new JLabel("Customization Options"));
        customizationList.setVisibleRowCount(8);
        customizationList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        editor.add(new JScrollPane(customizationList));

        editor.add(new JLabel("Quantity"));
        quantityField.setColumns(3);
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        quantityPanel.add(decreaseQtyBtn);
        quantityPanel.add(quantityField);
        quantityPanel.add(increaseQtyBtn);
        editor.add(quantityPanel);

        editor.add(unitPriceLabel);
        editor.add(lineTotalLabel);
        editor.add(subtotalLabel);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonBar.add(newOrderBtn);
        buttonBar.add(addOrUpdateBtn);
        buttonBar.add(removeBtn);
        editor.add(buttonBar);

        add(editor, BorderLayout.WEST);
    }

    private void buildTablePanel() {
        orderTable.setModel(orderModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(orderTable), BorderLayout.CENTER);
    }

    private void bindEvents() {
        menuCombo.addActionListener(e -> {
            if (applyingSelection) {
                return;
            }
            loadCustomizationsForSelectedItem();
            updatePreviewAndSubtotal();
            applyEditorToSelectedRowIfPossible();
        });

        customizationList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updatePreviewAndSubtotal();
                applyEditorToSelectedRowIfPossible();
            }
        });

        quantityField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onQuantityChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onQuantityChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onQuantityChanged();
            }

            private void onQuantityChanged() {
                updatePreviewAndSubtotal();
                applyEditorToSelectedRowIfPossible();
            }
        });

        newOrderBtn.addActionListener(e -> startNewOrder());
        addOrUpdateBtn.addActionListener(e -> addOrUpdateItem());
        removeBtn.addActionListener(e -> removeSelectedItem());
        decreaseQtyBtn.addActionListener(e -> decreaseQuantity());
        increaseQtyBtn.addActionListener(e -> increaseQuantity());

        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                syncEditorFromSelectedRow();
            }
        });
    }

    private void loadMenuItems() {
        menuCombo.removeAllItems();
        List<MenuItem> items = orderService.getAllMenuItems();
        for (MenuItem item : items) {
            menuCombo.addItem(item);
        }
        if (!items.isEmpty()) {
            menuCombo.setSelectedIndex(0);
            loadCustomizationsForSelectedItem();
        }
    }

    private void loadCustomizationsForSelectedItem() {
        MenuItem selected = (MenuItem) menuCombo.getSelectedItem();
        DefaultListModel<CustomizationOption> model = (DefaultListModel<CustomizationOption>) customizationList.getModel();
        model.clear();
        if (selected == null) {
            return;
        }

        List<CustomizationOption> options = orderService.getCustomizationOptions(selected.getId());
        for (CustomizationOption option : options) {
            model.addElement(option);
        }
    }

    private void startNewOrder() {
        currentDraft = orderService.createOrder(currentUser.getId());
        orderModel.setRowCount(0);
        orderTable.clearSelection();
        addOrUpdateBtn.setText("Add Item");
        quantityField.setText("1");
        customizationList.clearSelection();
        updatePreviewAndSubtotal();
    }

    private void addOrUpdateItem() {
        MenuItem selectedItem = (MenuItem) menuCombo.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select a menu item.");
            return;
        }

        int quantity;
        try {
            quantity = OrderService.parseQuantity(quantityField.getText());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
                return;
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<CustomizationOption> selectedCustomizations = orderService.copyOfSelected(customizationList.getSelectedValuesList());
        int selectedRow = orderTable.getSelectedRow();

        if (selectedRow >= 0) {
            orderService.replaceItem(currentDraft, selectedRow, selectedItem, selectedCustomizations, quantity);
        } else {
            orderService.addItem(currentDraft, selectedItem, selectedCustomizations, quantity);
        }

        refreshTable();
        updatePreviewAndSubtotal();
    }

    private void removeSelectedItem() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item in the order table.");
            return;
        }

        orderService.removeItem(currentDraft, selectedRow);
        refreshTable();
        addOrUpdateBtn.setText("Add Item");
        updatePreviewAndSubtotal();
    }

    private void refreshTable() {
        int previousSelection = orderTable.getSelectedRow();

        orderModel.setRowCount(0);
        List<OrderItem> items = currentDraft.getItems();
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            orderModel.addRow(new Object[] {
                i + 1,
                item.getMenuItem().getName(),
                item.getCustomizationSummary(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
            });
        }

        if (previousSelection >= 0 && previousSelection < orderModel.getRowCount()) {
            orderTable.setRowSelectionInterval(previousSelection, previousSelection);
        }
    }

    private void syncEditorFromSelectedRow() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentDraft.getItems().size()) {
            addOrUpdateBtn.setText("Add Item");
            return;
        }

        OrderItem selectedOrderItem = currentDraft.getItems().get(selectedRow);
        applyingSelection = true;
        try {
            selectMenuItemById(selectedOrderItem.getMenuItem().getId());
            loadCustomizationsForSelectedItem();
            selectCustomizations(selectedOrderItem.getCustomizations());
            quantityField.setText(String.valueOf(selectedOrderItem.getQuantity()));
        } finally {
            applyingSelection = false;
        }

        addOrUpdateBtn.setText("Update Item");
        updatePreviewAndSubtotal();
    }

    private void applyEditorToSelectedRowIfPossible() {
        if (applyingSelection) {
            return;
        }

        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentDraft.getItems().size()) {
            return;
        }

        MenuItem selectedItem = (MenuItem) menuCombo.getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        int quantity;
        try {
            quantity = OrderService.parseQuantity(quantityField.getText());
            if (quantity <= 0) {
                return;
            }
        } catch (IllegalArgumentException ex) {
            return;
        }

        List<CustomizationOption> selectedCustomizations = new ArrayList<>(customizationList.getSelectedValuesList());
        orderService.replaceItem(currentDraft, selectedRow, selectedItem, selectedCustomizations, quantity);
        refreshTable();
        orderTable.setRowSelectionInterval(selectedRow, selectedRow);
        updatePreviewAndSubtotal();
    }

    private void updatePreviewAndSubtotal() {
        MenuItem item = (MenuItem) menuCombo.getSelectedItem();
        if (item == null) {
            unitPriceLabel.setText("Unit price: 0");
            lineTotalLabel.setText("Line total: 0");
            subtotalLabel.setText("Subtotal: " + currentDraft.getSubtotal());
            return;
        }

        BigDecimal customizationTotal = BigDecimal.ZERO;
        for (CustomizationOption option : customizationList.getSelectedValuesList()) {
            customizationTotal = customizationTotal.add(option.getPriceDelta());
        }
        BigDecimal unitPrice = item.getBasePrice().add(customizationTotal);

        BigDecimal lineTotal = BigDecimal.ZERO;
        try {
            int qty = OrderService.parseQuantity(quantityField.getText());
            if (qty > 0) {
                lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            }
        } catch (IllegalArgumentException ignored) {
            // Preview remains 0 until quantity becomes valid.
        }

        unitPriceLabel.setText("Unit price: " + unitPrice);
        lineTotalLabel.setText("Line total: " + lineTotal);
        subtotalLabel.setText("Subtotal: " + currentDraft.getSubtotal());
    }

    private void increaseQuantity() {
        int qty = parseQuantityOrDefault();
        quantityField.setText(String.valueOf(qty + 1));
    }

    private void decreaseQuantity() {
        int qty = parseQuantityOrDefault();
        if (qty > 1) {
            quantityField.setText(String.valueOf(qty - 1));
        }
    }

    private int parseQuantityOrDefault() {
        try {
            int qty = OrderService.parseQuantity(quantityField.getText());
            return Math.max(1, qty);
        } catch (IllegalArgumentException ex) {
            return 1;
        }
    }

    private void selectMenuItemById(int menuItemId) {
        for (int i = 0; i < menuCombo.getItemCount(); i++) {
            MenuItem item = menuCombo.getItemAt(i);
            if (item.getId() == menuItemId) {
                menuCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectCustomizations(List<CustomizationOption> selectedCustomizations) {
        List<Integer> selectedIndices = new ArrayList<>();
        DefaultListModel<CustomizationOption> model = (DefaultListModel<CustomizationOption>) customizationList.getModel();

        for (int i = 0; i < model.getSize(); i++) {
            CustomizationOption option = model.getElementAt(i);
            for (CustomizationOption selected : selectedCustomizations) {
                if (option.getId() == selected.getId()) {
                    selectedIndices.add(i);
                    break;
                }
            }
        }

        int[] indices = new int[selectedIndices.size()];
        for (int i = 0; i < selectedIndices.size(); i++) {
            indices[i] = selectedIndices.get(i);
        }
        customizationList.setSelectedIndices(indices);
    }
}
