package com.oop.project.ui.util;

import java.awt.Font;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JTextArea;

import com.oop.project.model.OrderDraft;
import com.oop.project.model.OrderItem;
import com.oop.project.service.OrderService;

public class ReceiptPrinter {
    private static final int RECEIPT_WIDTH = 56;
    private static final int ITEM_COL_WIDTH = 34;
    private static final int QTY_COL_WIDTH = 4;
    private static final int TOTAL_COL_WIDTH = 16;

    private final OrderService orderService = new OrderService();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void printReceipt(OrderDraft draft, int orderId, int staffId, String staffName) {
        JTextArea textArea = new JTextArea();
        textArea.setText(generateReceipt(draft, orderId, staffId, staffName));
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        textArea.setEditable(false);
        textArea.setLineWrap(false);

        try {
            textArea.print();
        } catch (PrinterException e) {
            System.err.println("Print error: " + e.getMessage());
        }
    }

    private String generateReceipt(OrderDraft draft, int orderId, int staffId, String staffName) {
        StringBuilder sb = new StringBuilder();

        sb.append(center("RESTAURANT ORDER", RECEIPT_WIDTH)).append("\n");
        sb.append(repeatChar("=", RECEIPT_WIDTH)).append("\n\n");

        sb.append("Order ID: ").append(orderId).append("\n");
        sb.append("Staff: ").append(staffName).append(" (").append(staffId).append(")\n");
        sb.append("Date: ").append(dateFormat.format(new Date())).append("\n\n");

        sb.append(repeatChar("-", RECEIPT_WIDTH)).append("\n");
        sb.append(formatRow("Item", "Qty", "Total")).append("\n");
        sb.append(repeatChar("-", RECEIPT_WIDTH)).append("\n");

        for (OrderItem item : draft.getItems()) {
            String itemName = item.getMenuItem().getName();
            String customizations = item.getCustomizationSummary();
            if (!customizations.isEmpty()) {
                itemName = itemName + " (" + customizations + ")";
            }

            List<String> itemNameLines = wrapText(itemName, ITEM_COL_WIDTH);
            String qty = String.valueOf(item.getQuantity());
            String lineTotal = formatCurrency(item.getLineTotal());

            for (int i = 0; i < itemNameLines.size(); i++) {
                if (i == 0) {
                    sb.append(formatRow(itemNameLines.get(i), qty, lineTotal)).append("\n");
                } else {
                    sb.append(formatRow(itemNameLines.get(i), "", "")).append("\n");
                }
            }
        }

        sb.append(repeatChar("-", RECEIPT_WIDTH)).append("\n");

        BigDecimal subtotal = draft.getSubtotal();
        BigDecimal tax = orderService.calculateTax(subtotal);
        BigDecimal fee = orderService.calculateServiceFee(subtotal);
        BigDecimal total = orderService.calculateTotal(subtotal);

        sb.append(formatAmountLine("Subtotal:", subtotal)).append("\n");
        sb.append(formatAmountLine("Tax (10%):", tax)).append("\n");
        sb.append(formatAmountLine("Service Fee (5%):", fee)).append("\n");
        sb.append(repeatChar("=", RECEIPT_WIDTH)).append("\n");
        sb.append(formatAmountLine("TOTAL:", total)).append("\n");
        sb.append(repeatChar("=", RECEIPT_WIDTH)).append("\n\n");

        sb.append(center("Thank you for your order!", RECEIPT_WIDTH)).append("\n");

        return sb.toString();
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,d", amount.longValue()) + " VND";
    }

    private String formatRow(String item, String qty, String total) {
        return padRight(item, ITEM_COL_WIDTH)
            + " " + padLeft(qty, QTY_COL_WIDTH)
            + " " + padLeft(total, TOTAL_COL_WIDTH);
    }

    private String formatAmountLine(String label, BigDecimal amount) {
        String value = formatCurrency(amount);
        int labelWidth = RECEIPT_WIDTH - value.length() - 1;
        if (labelWidth < 1) {
            labelWidth = 1;
        }
        return padRight(label, labelWidth) + " " + value;
    }

    private List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            lines.add("");
            return lines;
        }

        String[] words = text.trim().split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (word.length() > width) {
                if (!current.isEmpty()) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                int start = 0;
                while (start < word.length()) {
                    int end = Math.min(start + width, word.length());
                    lines.add(word.substring(start, end));
                    start = end;
                }
                continue;
            }

            if (current.isEmpty()) {
                current.append(word);
            } else if (current.length() + 1 + word.length() <= width) {
                current.append(' ').append(word);
            } else {
                lines.add(current.toString());
                current.setLength(0);
                current.append(word);
            }
        }

        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private String center(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int padding = width - text.length();
        int left = padding / 2;
        return " ".repeat(left) + text + " ".repeat(padding - left);
    }

    private String padLeft(String text, int width) {
        String safe = text == null ? "" : text;
        if (safe.length() >= width) {
            return safe;
        }
        return " ".repeat(width - safe.length()) + safe;
    }

    private String padRight(String text, int width) {
        String safe = text == null ? "" : text;
        if (safe.length() >= width) {
            return safe;
        }
        return safe + " ".repeat(width - safe.length());
    }

    private String repeatChar(String c, int count) {
        return c.repeat(count);
    }
}
