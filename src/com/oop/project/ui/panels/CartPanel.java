package com.oop.project.ui.panels;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.oop.project.model.OrderDraft;
import com.oop.project.model.OrderItem;
import com.oop.project.model.User;
import com.oop.project.repository.OrderRepository;
import com.oop.project.service.OrderService;

public class CartPanel extends JPanel {
    
    private final User currentUser;
    private final OrderDraft currentDraft;
    private final Runnable onUpdate;
    private final OrderService orderService = new OrderService();
    private final OrderRepository orderRepository = new OrderRepository();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");

    private final JTable cartTable = new JTable();
    private final DefaultTableModel cartModel = new DefaultTableModel(
        new Object[] {"No.", "Item", "Customizations", "Qty", "Unit Price", "Line Total"},
        0
    );
    
    private final JLabel subtotalLabel = new JLabel("Subtotal: 0 VND");
    private final JLabel taxLabel = new JLabel("Tax (10%): 0 VND");
    private final JLabel feeLabel = new JLabel("Service Fee (5%): 0 VND");
    private final JLabel totalLabel = new JLabel("Total: 0 VND");
    
    private final JButton editBtn = new JButton("Edit");
    private final JButton removeBtn = new JButton("Remove");
    private final JButton checkoutBtn = new JButton("Checkout");
    private final JButton emptyBtn = new JButton("Empty Cart");

    public CartPanel(User user, OrderDraft draft, Runnable onUpdate) {
        this.currentUser = user;
        this.currentDraft = draft;
        this.onUpdate = onUpdate;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        buildCartTable();
        buildCalculationPanel();
        buildButtonPanel();
        refresh();
    }

    private void buildCartTable() {
        cartTable.setModel(cartModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.setRowHeight(25);
        
        add(new JScrollPane(cartTable), BorderLayout.CENTER);
    }

    private void buildCalculationPanel() {
        JPanel calcPanel = new JPanel(new BorderLayout());
        calcPanel.setBorder(BorderFactory.createTitledBorder("Order Summary"));
        
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new javax.swing.BoxLayout(summaryPanel, javax.swing.BoxLayout.Y_AXIS));
        summaryPanel.add(subtotalLabel);
        summaryPanel.add(taxLabel);
        summaryPanel.add(feeLabel);
        
        JLabel totalLabelBold = new JLabel();
        totalLabelBold.setFont(totalLabelBold.getFont().deriveFont(Font.BOLD, 14f));
        totalLabelBold.setText(totalLabel.getText());
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 14f));
        
        summaryPanel.add(totalLabel);
        calcPanel.add(summaryPanel, BorderLayout.NORTH);
        
        add(calcPanel, BorderLayout.SOUTH);
    }

    private void buildButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        editBtn.addActionListener(e -> editSelectedItem());
        removeBtn.addActionListener(e -> removeSelectedItem());
        checkoutBtn.addActionListener(e -> checkout());
        emptyBtn.addActionListener(e -> emptyCart());
        
        editBtn.setEnabled(false);
        removeBtn.setEnabled(false);
        
        cartTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = cartTable.getSelectedRow() >= 0;
            editBtn.setEnabled(hasSelection);
            removeBtn.setEnabled(hasSelection);
        });
        
        buttonPanel.add(editBtn);
        buttonPanel.add(removeBtn);
        buttonPanel.add(emptyBtn);
        buttonPanel.add(checkoutBtn);
        
        add(buttonPanel, BorderLayout.NORTH);
    }

    private void editSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to edit.");
            return;
        }

        OrderItem item = currentDraft.getItems().get(selectedRow);
        
        EditItemDialog dialog = new EditItemDialog(
            (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this),
            item
        );
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            int newQty = dialog.getQuantity();
            orderService.replaceItem(
                currentDraft, 
                selectedRow, 
                item.getMenuItem(), 
                item.getCustomizations(), 
                newQty
            );
            refresh();
        }
    }

    private void removeSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.");
            return;
        }

        orderService.removeItem(currentDraft, selectedRow);
        refresh();
        JOptionPane.showMessageDialog(this, "Item removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void emptyCart() {
        if (currentDraft.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is already empty.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Clear all items from cart?",
            "Empty Cart",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            currentDraft.clearItems();
            refresh();
        }
    }

    private void checkout() {
        if (currentDraft.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Add items before checkout.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal subtotal = currentDraft.getSubtotal();
        BigDecimal tax = orderService.calculateTax(subtotal);
        BigDecimal fee = orderService.calculateServiceFee(subtotal);
        BigDecimal total = orderService.calculateTotal(subtotal);

        OrderConfirmationDialog dialog = new OrderConfirmationDialog(
            (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this),
            currentDraft,
            currentUser.getId(),
            currentUser.getUsername(),
            orderService,
            orderRepository
        );
        dialog.setVisible(true);

        if (dialog.isSubmitted()) {
            int orderId = dialog.getSubmittedOrderId();
            JOptionPane.showMessageDialog(
                this,
                "Order #" + orderId + " submitted successfully!",
                "Checkout Complete",
                JOptionPane.INFORMATION_MESSAGE
            );
            currentDraft.clearItems();
            refresh();
        }
    }

    public void refresh() {
        refreshTable();
        updateCalculations();
        cartTable.repaint();
        revalidate();
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    private void refreshTable() {
        cartModel.setRowCount(0);
        List<OrderItem> items = currentDraft.getItems();
        
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            cartModel.addRow(new Object[] {
                i + 1,
                item.getMenuItem().getName(),
                item.getCustomizationSummary(),
                item.getQuantity(),
                formatCurrency(item.getUnitPrice()),
                formatCurrency(item.getLineTotal())
            });
        }
    }

    private void updateCalculations() {
        BigDecimal subtotal = currentDraft.getSubtotal();
        BigDecimal tax = orderService.calculateTax(subtotal);
        BigDecimal fee = orderService.calculateServiceFee(subtotal);
        BigDecimal total = orderService.calculateTotal(subtotal);

        subtotalLabel.setText("Subtotal: " + formatCurrency(subtotal));
        taxLabel.setText("Tax (10%): " + formatCurrency(tax));
        feeLabel.setText("Service Fee (5%): " + formatCurrency(fee));
        totalLabel.setText("Total: " + formatCurrency(total));
    }

    private String formatCurrency(BigDecimal amount) {
        return priceFormat.format(amount) + " VND";
    }
}

