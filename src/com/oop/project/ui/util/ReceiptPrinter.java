package com.oop.project.ui.util;

import java.awt.Font;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.oop.project.model.Order;
import com.oop.project.model.OrderDraft;
import com.oop.project.model.OrderItem;
import com.oop.project.service.OrderService;

public class ReceiptPrinter {
    private final OrderService orderService = new OrderService();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void printReceipt(OrderDraft draft, int orderId, int staffId, String staffName) {
        JTextPane textPane = new JTextPane();
        textPane.setText(generateReceipt(draft, orderId, staffId, staffName));
        textPane.setFont(new Font("Courier", Font.PLAIN, 10));
        textPane.setEditable(false);

        try {
            textPane.print();
        } catch (PrinterException e) {
            System.err.println("Print error: " + e.getMessage());
        }
    }

    private String generateReceipt(OrderDraft draft, int orderId, int staffId, String staffName) {
        StringBuilder sb = new StringBuilder();

        sb.append(pad("RESTAURANT ORDER", 40, "center")).append("\n");
        sb.append(repeatChar("=", 40)).append("\n\n");

        sb.append("Order ID: ").append(orderId).append("\n");
        sb.append("Staff: ").append(staffName).append(" (").append(staffId).append(")\n");
        sb.append("Date: ").append(dateFormat.format(new Date())).append("\n\n");

        sb.append(repeatChar("-", 40)).append("\n");
        sb.append(String.format("%-25s %5s %10s", "Item", "Qty", "Total")).append("\n");
        sb.append(repeatChar("-", 40)).append("\n");

        for (OrderItem item : draft.getItems()) {
            String itemName = truncate(item.getMenuItem().getName(), 25);
            String customizations = item.getCustomizationSummary();
            if (!customizations.isEmpty()) {
                itemName = truncate(itemName + " (" + customizations + ")", 25);
            }

            sb.append(String.format("%-25s %5d %10s\n",
                itemName,
                item.getQuantity(),
                formatCurrency(item.getLineTotal())
            ));
        }

        BigDecimal subtotal = draft.getSubtotal();
        BigDecimal tax = orderService.calculateTax(subtotal);
        BigDecimal fee = orderService.calculateServiceFee(subtotal);
        BigDecimal total = orderService.calculateTotal(subtotal);

        sb.append(repeatChar("-", 40)).append("\n");
        sb.append(String.format("%-25s %15s", "Subtotal:", formatCurrency(subtotal))).append("\n");
        sb.append(String.format("%-25s %15s", "Tax (10%):", formatCurrency(tax))).append("\n");
        sb.append(String.format("%-25s %15s", "Service Fee (5%):", formatCurrency(fee))).append("\n");
        sb.append(repeatChar("=", 40)).append("\n");
        sb.append(String.format("%-25s %15s", "TOTAL:", formatCurrency(total))).append("\n");
        sb.append(repeatChar("=", 40)).append("\n\n");

        sb.append(pad("Thank you for your order!", 40, "center")).append("\n");

        return sb.toString();
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,d", amount.longValue()) + " VND";
    }

    private String truncate(String s, int len) {
        return s.length() > len ? s.substring(0, len - 3) + "..." : s;
    }

    private String pad(String s, int len, String align) {
        if (s.length() >= len) return s;
        int padding = len - s.length();
        if ("center".equals(align)) {
            int left = padding / 2;
            return " ".repeat(left) + s + " ".repeat(padding - left);
        }
        return s + " ".repeat(padding);
    }

    private String repeatChar(String c, int count) {
        return c.repeat(count);
    }
}
