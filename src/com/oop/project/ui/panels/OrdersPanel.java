package com.oop.project.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import com.oop.project.model.CustomizationOption;
import com.oop.project.model.MenuItem;
import com.oop.project.model.OrderDraft;
import com.oop.project.model.OrderItem;
import com.oop.project.model.User;
import com.oop.project.service.OrderService;

public class OrdersPanel extends JPanel {

    private final User currentUser;
    private final OrderService orderService = new OrderService();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");
    private OrderDraft currentDraft;
    private Runnable onUpdate;

    private final JComboBox<MenuItem> menuCombo = new JComboBox<>();
    private final JPanel customizationContainer = new JPanel();
    private final JScrollPane customizationScroll = new JScrollPane(customizationContainer);
    private final Map<Integer, CustomizationOption> customizationById = new LinkedHashMap<>();
    private final Map<Integer, AbstractButton> customizationButtons = new LinkedHashMap<>();
    private final Map<String, ButtonGroup> singleSelectGroups = new LinkedHashMap<>();
    private final JTextField quantityField = new JTextField("1");
    private final JButton decreaseQtyBtn = new JButton("-");
    private final JButton increaseQtyBtn = new JButton("+");

    private final JLabel unitPriceLabel = new JLabel("Unit price: 0 VND");
    private final JLabel lineTotalLabel = new JLabel("Line total: 0 VND");
    private final JLabel subtotalLabel = new JLabel("Subtotal: 0 VND");

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
        this(currentUser, new OrderService().createOrder(currentUser.getId()), null);
    }

    public OrdersPanel(User currentUser, OrderDraft sharedDraft, Runnable onUpdate) {
        this.currentUser = currentUser;
        this.currentDraft = sharedDraft;
        this.onUpdate = onUpdate;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        configureMenuComboRenderer();
        configureQuantityField();

        buildEditorPanel();
        buildTablePanel();
        bindEvents();

        loadMenuItems();
        refreshTable();
        updatePreviewAndSubtotal();
    }

    public void refresh() {
        refreshTable();
        updatePreviewAndSubtotal();
    }

    private void configureMenuComboRenderer() {
        menuCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MenuItem item) {
                    setText(item.getName() + " - " + formatCurrency(item.getBasePrice()));
                }
                return this;
            }
        });
    }

    private void buildEditorPanel() {
        JPanel editor = new JPanel(new GridLayout(0, 1, 0, 6));
        editor.setPreferredSize(new Dimension(320, 0));

        editor.add(new JLabel("Menu Item"));
        editor.add(menuCombo);

        editor.add(new JLabel("Customization Options"));
        customizationContainer.setLayout(new BoxLayout(customizationContainer, BoxLayout.Y_AXIS));
        customizationScroll.setPreferredSize(new Dimension(0, 170));
        editor.add(customizationScroll);

        editor.add(new JLabel("Quantity"));
        quantityField.setColumns(5);
        quantityField.setHorizontalAlignment(SwingConstants.CENTER);
        quantityField.setPreferredSize(new Dimension(72, quantityField.getPreferredSize().height));
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

    private void configureQuantityField() {
        ((AbstractDocument) quantityField.getDocument()).setDocumentFilter(new PositiveQuantityFilter());
        quantityField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                quantityField.setText(String.valueOf(parseQuantityOrDefault()));
            }
        });

        InputMap inputMap = quantityField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = quantityField.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "increaseQty");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "decreaseQty");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "increaseQty");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "decreaseQty");
        inputMap.put(KeyStroke.getKeyStroke('+'), "increaseQty");
        inputMap.put(KeyStroke.getKeyStroke('-'), "decreaseQty");

        actionMap.put("increaseQty", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                increaseQuantity();
            }
        });
        actionMap.put("decreaseQty", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                decreaseQuantity();
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
        if (selected == null) {
            buildCustomizationControls(List.of());
            return;
        }

        List<CustomizationOption> options = orderService.getCustomizationOptions(selected.getId());
        buildCustomizationControls(options);
    }

    private void startNewOrder() {
        currentDraft.getItems().clear();
        orderModel.setRowCount(0);
        orderTable.clearSelection();
        addOrUpdateBtn.setText("Add Item");
        quantityField.setText("1");
        clearCustomizationSelection();
        updatePreviewAndSubtotal();
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    private void addOrUpdateItem() {
        MenuItem selectedItem = (MenuItem) menuCombo.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select a menu item.", "Error", JOptionPane.ERROR_MESSAGE);
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

        List<CustomizationOption> selectedCustomizations = orderService.copyOfSelected(getSelectedCustomizations());
        int selectedRow = orderTable.getSelectedRow();

        if (selectedRow >= 0) {
            orderService.replaceItem(currentDraft, selectedRow, selectedItem, selectedCustomizations, quantity);
            JOptionPane.showMessageDialog(this, "Item updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            orderService.addItem(currentDraft, selectedItem, selectedCustomizations, quantity);
            JOptionPane.showMessageDialog(this, "Item added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        }

        refreshTable();
        updatePreviewAndSubtotal();
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    private void removeSelectedItem() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        orderService.removeItem(currentDraft, selectedRow);
        refreshTable();
        addOrUpdateBtn.setText("Add Item");
        updatePreviewAndSubtotal();
        JOptionPane.showMessageDialog(this, "Item removed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        if (onUpdate != null) {
            onUpdate.run();
        }
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
                formatCurrency(item.getUnitPrice()),
                formatCurrency(item.getLineTotal())
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

        List<CustomizationOption> selectedCustomizations = new ArrayList<>(getSelectedCustomizations());
        orderService.replaceItem(currentDraft, selectedRow, selectedItem, selectedCustomizations, quantity);
        refreshTable();
        orderTable.setRowSelectionInterval(selectedRow, selectedRow);
        updatePreviewAndSubtotal();
    }

    private void updatePreviewAndSubtotal() {
        MenuItem item = (MenuItem) menuCombo.getSelectedItem();
        if (item == null) {
            unitPriceLabel.setText("Unit price: 0 VND");
            lineTotalLabel.setText("Line total: 0 VND");
            subtotalLabel.setText("Subtotal: " + formatCurrency(currentDraft.getSubtotal()));
            return;
        }

        BigDecimal customizationTotal = BigDecimal.ZERO;
        for (CustomizationOption option : getSelectedCustomizations()) {
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

        unitPriceLabel.setText("Unit price: " + formatCurrency(unitPrice));
        lineTotalLabel.setText("Line total: " + formatCurrency(lineTotal));
        subtotalLabel.setText("Subtotal: " + formatCurrency(currentDraft.getSubtotal()));
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

    private static class PositiveQuantityFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String replacement = text == null ? "" : text;
            String candidate = current.substring(0, offset) + replacement + current.substring(offset + length);

            if (candidate.matches("([1-9]\\d*)?")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            replace(fb, offset, length, "", null);
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
        clearCustomizationSelection();

        Set<Integer> selectedIds = new HashSet<>();
        for (CustomizationOption selected : selectedCustomizations) {
            selectedIds.add(selected.getId());
        }

        for (Map.Entry<Integer, AbstractButton> entry : customizationButtons.entrySet()) {
            if (selectedIds.contains(entry.getKey())) {
                entry.getValue().setSelected(true);
            }
        }
    }

    private void buildCustomizationControls(List<CustomizationOption> options) {
        customizationContainer.removeAll();
        customizationById.clear();
        customizationButtons.clear();
        singleSelectGroups.clear();

        if (options.isEmpty()) {
            JLabel emptyLabel = new JLabel("No customization available.");
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            customizationContainer.add(emptyLabel);
            customizationContainer.revalidate();
            customizationContainer.repaint();
            return;
        }

        List<CustomizationOption> sorted = new ArrayList<>(options);
        sorted.sort(Comparator.comparingInt(option -> groupPriority(classifyGroupTitle(option.getName()))));

        Map<String, List<CustomizationOption>> groupedOptions = new LinkedHashMap<>();
        for (CustomizationOption option : sorted) {
            String groupTitle = classifyGroupTitle(option.getName());
            groupedOptions.computeIfAbsent(groupTitle, key -> new ArrayList<>()).add(option);
        }

        ActionListener optionSelectionListener = e -> {
            if (applyingSelection) {
                return;
            }
            updatePreviewAndSubtotal();
            applyEditorToSelectedRowIfPossible();
        };

        for (Map.Entry<String, List<CustomizationOption>> entry : groupedOptions.entrySet()) {
            String groupTitle = entry.getKey();
            List<CustomizationOption> groupOptions = entry.getValue();

            JPanel groupPanel = new JPanel();
            groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
            groupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            groupPanel.setBorder(BorderFactory.createTitledBorder(groupTitle));

            boolean singleSelect = isSingleSelectGroup(groupTitle);
            ButtonGroup buttonGroup = null;
            if (singleSelect) {
                buttonGroup = new ButtonGroup();
                singleSelectGroups.put(groupTitle, buttonGroup);
            }

            for (CustomizationOption option : groupOptions) {
                AbstractButton optionButton = singleSelect
                    ? new JRadioButton(formatOptionLabel(option))
                    : new JCheckBox(formatOptionLabel(option));

                optionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                optionButton.addActionListener(optionSelectionListener);

                customizationById.put(option.getId(), option);
                customizationButtons.put(option.getId(), optionButton);

                if (buttonGroup != null) {
                    buttonGroup.add(optionButton);
                }

                groupPanel.add(optionButton);
            }

            customizationContainer.add(groupPanel);
        }

        customizationContainer.revalidate();
        customizationContainer.repaint();
    }

    private int groupPriority(String groupTitle) {
        return switch (groupTitle) {
            case "Size" -> 0;
            case "Extra Proteins" -> 1;
            case "Sauce" -> 2;
            case "Extras" -> 3;
            case "Spice Level" -> 4;
            case "Removals" -> 5;
            default -> 6;
        };
    }

    private String classifyGroupTitle(String optionName) {
        String lower = optionName.toLowerCase();
        
        if (lower.contains("size:") || lower.matches(".*(small|medium|large|extra large|s|m|l|xl).*")) {
            return "Size";
        }
        if (lower.contains("sauce:") || lower.matches(".*(gravy|sauce|dressing).*")) {
            return "Sauce";
        }
        if (lower.contains("protein:") || lower.matches(".*(chicken|beef|pork|shrimp|tofu).*")) {
            return "Extra Proteins";
        }
        if (lower.contains("spic") || lower.matches(".*(mild|medium|hot|extra hot|spicy).*")) {
            return "Spice Level";
        }
        if (lower.startsWith("no ") || lower.startsWith("without ")) {
            return "Removals";
        }
        
        return "Extras";
    }

    private boolean isSingleSelectGroup(String groupTitle) {
        return "Size".equals(groupTitle) || "Sauce".equals(groupTitle) || "Spice Level".equals(groupTitle);
    }

    private String formatOptionLabel(CustomizationOption option) {
        BigDecimal priceDelta = option.getPriceDelta();
        if (priceDelta.compareTo(BigDecimal.ZERO) == 0) {
            return option.getName();
        }
        String sign = priceDelta.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return option.getName() + " (" + sign + formatCurrency(priceDelta.abs()) + ")";
    }

    private String formatCurrency(BigDecimal amount) {
        return priceFormat.format(amount) + " VND";
    }

    private List<CustomizationOption> getSelectedCustomizations() {
        List<CustomizationOption> selected = new ArrayList<>();
        for (Map.Entry<Integer, AbstractButton> entry : customizationButtons.entrySet()) {
            if (entry.getValue().isSelected()) {
                CustomizationOption option = customizationById.get(entry.getKey());
                if (option != null) {
                    selected.add(option);
                }
            }
        }
        return selected;
    }

    private void clearCustomizationSelection() {
        for (ButtonGroup buttonGroup : singleSelectGroups.values()) {
            buttonGroup.clearSelection();
        }
        for (AbstractButton button : customizationButtons.values()) {
            button.setSelected(false);
        }
    }
}
