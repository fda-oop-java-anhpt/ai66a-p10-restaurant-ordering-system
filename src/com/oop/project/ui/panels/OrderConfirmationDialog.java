package com.oop.project.ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.oop.project.model.OrderDraft;
import com.oop.project.model.OrderItem;
import com.oop.project.repository.OrderRepository;
import com.oop.project.service.OrderService;
import com.oop.project.ui.util.ReceiptPrinter;

public class OrderConfirmationDialog extends JDialog {
    private final OrderDraft orderDraft;
    private final int staffId;
    private final String staffName;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ReceiptPrinter receiptPrinter = new ReceiptPrinter();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");
    
    private int submittedOrderId = -1;
    private boolean submitted = false;

    public OrderConfirmationDialog(
        JFrame parent,
        OrderDraft draft,
        int staffId,
        String staffName,
        OrderService orderService,
        OrderRepository orderRepository
    ) {
        super(parent, "Order Confirmation", true);
        this.orderDraft = draft;
        this.staffId = staffId;
        this.staffName = staffName;
        this.orderService = orderService;
        this.orderRepository = orderRepository;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        buildDialog();
    }

    private void buildDialog() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(buildHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(buildItemsTable(), BorderLayout.CENTER);
        mainPanel.add(buildSummaryPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 5));
        panel.add(new JLabel("Staff: " + staffName + " (ID: " + staffId + ")"));
        panel.add(new JLabel("Items: " + orderDraft.getItems().size()));
        return panel;
    }

    private JPanel buildItemsTable() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[] {"Item", "Qty", "Unit Price", "Total"},
            0
        );

        for (OrderItem item : orderDraft.getItems()) {
            model.addRow(new Object[] {
                item.getMenuItem().getName() + " " + item.getCustomizationSummary(),
                item.getQuantity(),
                formatCurrency(item.getUnitPrice()),
                formatCurrency(item.getLineTotal())
            });
        }

        JTable table = new JTable(model);
        table.setEnabled(false);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Order Summary"));

        BigDecimal subtotal = orderDraft.getSubtotal();
        BigDecimal tax = orderService.calculateTax(subtotal);
        BigDecimal fee = orderService.calculateServiceFee(subtotal);
        BigDecimal total = orderService.calculateTotal(subtotal);

        JPanel calcPanel = new JPanel();
        calcPanel.setLayout(new javax.swing.BoxLayout(calcPanel, javax.swing.BoxLayout.Y_AXIS));
        calcPanel.add(new JLabel("Subtotal: " + formatCurrency(subtotal)));
        calcPanel.add(new JLabel("Tax (10%): " + formatCurrency(tax)));
        calcPanel.add(new JLabel("Service Fee (5%): " + formatCurrency(fee)));
        
        JLabel totalLabel = new JLabel("Total: " + formatCurrency(total));
        totalLabel.setFont(totalLabel.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        calcPanel.add(totalLabel);

        panel.add(calcPanel, BorderLayout.CENTER);
        panel.add(buildActionButtons(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton submitBtn = new JButton("Submit Order");
        JButton printBtn = new JButton("Print Receipt");
        JButton cancelBtn = new JButton("Cancel");

        submitBtn.addActionListener(e -> submitOrder());
        printBtn.addActionListener(e -> printReceipt());
        cancelBtn.addActionListener(e -> dispose());

        panel.add(submitBtn);
        panel.add(printBtn);
        panel.add(cancelBtn);

        return panel;
    }

    private void submitOrder() {
        try {
            BigDecimal subtotal = orderDraft.getSubtotal();
            BigDecimal tax = orderService.calculateTax(subtotal);
            BigDecimal fee = orderService.calculateServiceFee(subtotal);
            BigDecimal total = orderService.calculateTotal(subtotal);

            submittedOrderId = orderRepository.submitOrder(
                orderDraft,
                staffId,
                subtotal,
                tax,
                fee,
                total
            );

            submitted = true;
            JOptionPane.showMessageDialog(
                this,
                "Order submitted successfully! Order ID: " + submittedOrderId,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to submit order: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void printReceipt() {
        if (submittedOrderId <= 0) {
            JOptionPane.showMessageDialog(
                this,
                "Please submit the order first before printing.",
                "No Order ID",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        receiptPrinter.printReceipt(orderDraft, submittedOrderId, staffId, staffName);
    }

    private String formatCurrency(BigDecimal amount) {
        return priceFormat.format(amount) + " VND";
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public int getSubmittedOrderId() {
        return submittedOrderId;
    }
}
