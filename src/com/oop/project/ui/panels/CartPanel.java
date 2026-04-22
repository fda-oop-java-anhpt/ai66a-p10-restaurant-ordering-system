package com.oop.project.ui.panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.oop.project.ui.components.OrderLineCard;
import com.oop.project.ui.components.TonalCard;
import com.oop.project.model.OrderDraft;
import com.oop.project.model.OrderItem;
import com.oop.project.model.User;
import com.oop.project.repository.OrderRepository;
import com.oop.project.service.OrderService;
import com.oop.project.ui.theme.AppTheme;
import com.oop.project.ui.theme.ThemeFonts;
import com.oop.project.ui.theme.ThemeInsets;

public class CartPanel extends JPanel {

    // ── Summary colours ──────────────────────────────────────────────────────
    private static final Color SUMMARY_BG      = new Color(0xF7F8FA);
    private static final Color DIVIDER_COLOR   = new Color(0xE2E4E8);
    private static final Color TOTAL_BLUE      = new Color(0x1565C0);
    private static final Color CONFIRM_GREEN   = new Color(0x1B8A3C);
    private static final Color CONFIRM_HOVER   = new Color(0x166B31);
    private static final Color CANCEL_BORDER   = new Color(0xCCCCCC);
    private static final Color ICON_BTN_BORDER = new Color(0xDDDDDD);

    private final User currentUser;
    private final OrderDraft currentDraft;
    private final Runnable onUpdate;
    private final OrderService orderService = new OrderService();
    private final OrderRepository orderRepository = new OrderRepository();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");

    private final JPanel itemsListPanel = new JPanel();
    private final JLabel itemCountLabel = new JLabel("0 items");

    // Summary value labels
    private final JLabel subtotalValueLabel  = new JLabel("0 VND");
    private final JLabel taxValueLabel       = new JLabel("0 VND");
    private final JLabel feeValueLabel       = new JLabel("0 VND");
    private final JLabel totalValueLabel     = new JLabel("0 VND");

    // Hidden legacy labels (keep for existing updateCalculations logic)
    private final JLabel subtotalLabel = new JLabel();
    private final JLabel taxLabel      = new JLabel();
    private final JLabel feeLabel      = new JLabel();

    private final JButton checkoutBtn  = new JButton("Confirm Order");
    private final JButton emptyBtn     = new JButton("Cancel Transaction");

    private final JRadioButton cardPaymentButton = new JRadioButton("  Card");
    private final JRadioButton cashPaymentButton = new JRadioButton("  Cash");

    private String selectedPaymentMethod = "CARD";

    // Badge kept for internal state — no longer shown visually
    private final JLabel paymentBadgeLabel = new JLabel("Payment: CARD");

    public CartPanel(User user, OrderDraft draft, Runnable onUpdate) {
        this.currentUser = user;
        this.currentDraft = draft;
        this.onUpdate = onUpdate;

        setLayout(new BorderLayout(AppTheme.SPACE_4, AppTheme.SPACE_4));
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(ThemeInsets.section()));

        buildCartLayout();
        refresh();
    }

    private void buildCartLayout() {
        add(buildItemsColumn(),   BorderLayout.CENTER);
        add(buildSummaryColumn(), BorderLayout.EAST);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Left column – Review Order list
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildItemsColumn() {
        JPanel column = new JPanel(new BorderLayout(0, AppTheme.SPACE_3));
        column.setOpaque(false);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(
            0, 0, AppTheme.SPACE_2, 0));

        JLabel titleLabel = new JLabel("Review Order");
        titleLabel.setFont(ThemeFonts.displayMd());
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(titleLabel);

        // Items list (white bg, no card border)
        itemsListPanel.setOpaque(true);
        itemsListPanel.setBackground(Color.WHITE);
        itemsListPanel.setLayout(new BoxLayout(itemsListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(itemsListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(DIVIDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        column.add(headerPanel,  BorderLayout.NORTH);
        column.add(scrollPane,   BorderLayout.CENTER);
        return column;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Right column – Summary panel
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildSummaryColumn() {
        JPanel summary = new JPanel(new BorderLayout(0, 0));
        summary.setBackground(SUMMARY_BG);
        summary.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DIVIDER_COLOR, 1),
            BorderFactory.createEmptyBorder(
                AppTheme.SPACE_4, AppTheme.SPACE_4,
                AppTheme.SPACE_4, AppTheme.SPACE_4)
        ));
        summary.setOpaque(true);
        summary.setPreferredSize(new Dimension(300, 0));

        // Body
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // Title
        JLabel summaryTitle = new JLabel("Summary");
        summaryTitle.setFont(ThemeFonts.titleLg());
        summaryTitle.setForeground(AppTheme.TEXT_PRIMARY);
        summaryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Price rows
        JPanel priceRows = buildPriceRows();
        priceRows.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Total row
        JPanel totalRow = buildTotalRow();
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Payment toggle
        JPanel paymentToggle = buildPaymentToggle();
        paymentToggle.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(summaryTitle);
        body.add(Box.createVerticalStrut(AppTheme.SPACE_4));
        body.add(priceRows);
        body.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        body.add(sep);
        body.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        body.add(totalRow);
        body.add(Box.createVerticalStrut(AppTheme.SPACE_4));
        body.add(paymentToggle);
        body.add(Box.createVerticalGlue());

        // Bottom action panel
        JPanel actionArea = buildActionArea();

        summary.add(body,       BorderLayout.CENTER);
        summary.add(actionArea, BorderLayout.SOUTH);
        return summary;
    }

    // ── Price rows: Subtotal / Tax / Service Fee ─────────────────────────────
    private JPanel buildPriceRows() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(buildSummaryLine("Subtotal",          subtotalValueLabel, false));
        panel.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        panel.add(buildSummaryLine("Tax (10%)",         taxValueLabel,      false));
        panel.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        panel.add(buildSummaryLine("Service Fee (5%)",  feeValueLabel,      false));
        return panel;
    }

    private JPanel buildSummaryLine(String labelText, JLabel valueLabel, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(bold ? ThemeFonts.bodyMd().deriveFont(Font.BOLD) : ThemeFonts.bodyMd());
        lbl.setForeground(AppTheme.TEXT_SECONDARY);

        valueLabel.setFont(bold ? ThemeFonts.bodyMd().deriveFont(Font.BOLD) : ThemeFonts.bodyMd());
        valueLabel.setForeground(bold ? AppTheme.TEXT_PRIMARY : AppTheme.TEXT_SECONDARY);

        row.add(lbl,        BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    // ── Total row ────────────────────────────────────────────────────────────
    private JPanel buildTotalRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel totalLbl = new JLabel("Total");
        totalLbl.setFont(ThemeFonts.bodyLg().deriveFont(Font.BOLD));
        totalLbl.setForeground(AppTheme.TEXT_PRIMARY);

        totalValueLabel.setFont(ThemeFonts.titleLg().deriveFont(Font.BOLD, 24f));
        totalValueLabel.setForeground(AppTheme.TEXT_PRIMARY);

        row.add(totalLbl,       BorderLayout.WEST);
        row.add(totalValueLabel, BorderLayout.EAST);
        return row;
    }

    // ── Payment toggle: Card | Cash ──────────────────────────────────────────
    private JPanel buildPaymentToggle() {
        JPanel panel = new JPanel(new GridLayout(1, 2, AppTheme.SPACE_2, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        stylePaymentToggle(cardPaymentButton);
        stylePaymentToggle(cashPaymentButton);
        cardPaymentButton.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(cardPaymentButton);
        group.add(cashPaymentButton);

        cardPaymentButton.addActionListener(e -> {
            selectedPaymentMethod = "CARD";
            updatePaymentSelectionUI();
        });
        cashPaymentButton.addActionListener(e -> {
            selectedPaymentMethod = "CASH";
            updatePaymentSelectionUI();
        });

        updatePaymentSelectionUI();
        panel.add(cardPaymentButton);
        panel.add(cashPaymentButton);
        return panel;
    }

    private void stylePaymentToggle(AbstractButton button) {
        button.setFont(ThemeFonts.bodyMd());
        button.setBackground(Color.WHITE);
        button.setForeground(AppTheme.PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createLineBorder(CANCEL_BORDER, 1));
        button.setHorizontalAlignment(SwingConstants.CENTER);
    }

    // ── Action area: Confirm Order / Cancel Transaction / icons ─────────────
    private JPanel buildActionArea() {
        JPanel area = new JPanel();
        area.setOpaque(false);
        area.setLayout(new BoxLayout(area, BoxLayout.Y_AXIS));
        area.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_4, 0, 0, 0));

        // Confirm Order button (green + checkmark icon)
        checkoutBtn.setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD));
        checkoutBtn.setBackground(CONFIRM_GREEN);
        checkoutBtn.setForeground(Color.WHITE);
        checkoutBtn.setOpaque(true);
        checkoutBtn.setBorderPainted(false);
        checkoutBtn.setFocusPainted(false);
        checkoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        checkoutBtn.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_3, AppTheme.SPACE_4,
            AppTheme.SPACE_3, AppTheme.SPACE_4));
        checkoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        checkoutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (checkoutBtn.isEnabled()) checkoutBtn.setBackground(CONFIRM_HOVER);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (checkoutBtn.isEnabled()) checkoutBtn.setBackground(CONFIRM_GREEN);
            }
        });
        checkoutBtn.addActionListener(e -> checkout());

        // Cancel Transaction button (outlined)
        emptyBtn.setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD));
        emptyBtn.setBackground(Color.WHITE);
        emptyBtn.setForeground(AppTheme.TEXT_PRIMARY);
        emptyBtn.setOpaque(true);
        emptyBtn.setFocusPainted(false);
        emptyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        emptyBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CANCEL_BORDER, 1),
            BorderFactory.createEmptyBorder(
                AppTheme.SPACE_2, AppTheme.SPACE_4,
                AppTheme.SPACE_2, AppTheme.SPACE_4)
        ));
        emptyBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        emptyBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        emptyBtn.addActionListener(e -> emptyCart());

        area.add(checkoutBtn);
        area.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        area.add(emptyBtn);
        return area;
    }





    // ════════════════════════════════════════════════════════════════════════
    //  Payment toggle state
    // ════════════════════════════════════════════════════════════════════════
    private void updatePaymentSelectionUI() {
        boolean isCard = "CARD".equals(selectedPaymentMethod);
        applyPaymentButtonState(cardPaymentButton, isCard);
        applyPaymentButtonState(cashPaymentButton, !isCard);
        paymentBadgeLabel.setText("Payment: " + selectedPaymentMethod);
    }

    private void applyPaymentButtonState(AbstractButton button, boolean active) {
        if (active) {
            button.setBackground(AppTheme.PRIMARY_CONTAINER);
            button.setForeground(AppTheme.ON_PRIMARY);
            button.setBorder(BorderFactory.createLineBorder(AppTheme.PRIMARY_CONTAINER, 1));
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(AppTheme.TEXT_PRIMARY);
            button.setBorder(BorderFactory.createLineBorder(CANCEL_BORDER, 1));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Business logic (unchanged)
    // ════════════════════════════════════════════════════════════════════════
    private void editItemAt(int index) {
        if (index < 0 || index >= currentDraft.getItems().size()) return;
        OrderItem item = currentDraft.getItems().get(index);
        EditItemDialog dialog = new EditItemDialog(
            (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this), item);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            orderService.replaceItem(currentDraft, index,
                item.getMenuItem(), item.getCustomizations(), dialog.getQuantity(), dialog.getNote());
            refresh();
        }
    }

    private void removeItemAt(int index) {
        if (index < 0 || index >= currentDraft.getItems().size()) return;
        orderService.removeItem(currentDraft, index);
        refresh();
    }

    private void increaseQuantity(int index) {
        if (index < 0 || index >= currentDraft.getItems().size()) return;
        OrderItem item = currentDraft.getItems().get(index);
        orderService.replaceItem(currentDraft, index,
            item.getMenuItem(), item.getCustomizations(), item.getQuantity() + 1, item.getNote());
        refresh();
    }

    private void decreaseQuantity(int index) {
        if (index < 0 || index >= currentDraft.getItems().size()) return;
        OrderItem item = currentDraft.getItems().get(index);
        if (item.getQuantity() <= 1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Quantity is 1. Remove this item from cart?",
                "Remove Item", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) removeItemAt(index);
            return;
        }
        orderService.replaceItem(currentDraft, index,
            item.getMenuItem(), item.getCustomizations(), item.getQuantity() - 1, item.getNote());
        refresh();
    }

    private void emptyCart() {
        if (currentDraft.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is already empty.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Clear all items from cart?", "Empty Cart", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            currentDraft.clearItems();
            refresh();
        }
    }

    private void checkout() {
        if (currentDraft.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Cart is empty. Add items before checkout.",
                "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }
        OrderConfirmationDialog dialog = new OrderConfirmationDialog(
            (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this),
            currentDraft, currentUser.getId(), currentUser.getUsername(),
            orderService, orderRepository, onUpdate);
        dialog.setVisible(true);
        if (dialog.isSubmitted()) {
            int orderId = dialog.getSubmittedOrderId();
            JOptionPane.showMessageDialog(this,
                "Order #" + orderId + " submitted successfully!",
                "Checkout Complete", JOptionPane.INFORMATION_MESSAGE);
            currentDraft.clearItems();
            refresh();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Refresh
    // ════════════════════════════════════════════════════════════════════════
    public void refresh() {
        refreshItemsList();
        updateCalculations();
        updateButtonsState();
        revalidate();
        repaint();
        if (onUpdate != null) onUpdate.run();
    }

    private void refreshItemsList() {
        itemsListPanel.removeAll();
        List<OrderItem> items = currentDraft.getItems();

        if (items.isEmpty()) {
            itemsListPanel.add(buildEmptyStateCard());
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            OrderLineCard lineCard = new OrderLineCard(
                i, item,
                this::increaseQuantity, this::decreaseQuantity,
                this::editItemAt,       this::removeItemAt);

            Dimension preferred = lineCard.getPreferredSize();
            lineCard.setPreferredSize(new Dimension(preferred.width, preferred.height));
            lineCard.setMinimumSize(new Dimension(0, preferred.height));
            lineCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));
            lineCard.setAlignmentX(LEFT_ALIGNMENT);

            itemsListPanel.add(lineCard);
        }
    }

    private JPanel buildEmptyStateCard() {
        JPanel emptyCard = new JPanel();
        emptyCard.setOpaque(false);
        emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));
        emptyCard.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_8, AppTheme.SPACE_4,
            AppTheme.SPACE_8, AppTheme.SPACE_4));

        JLabel title = new JLabel("Your cart is empty");
        title.setFont(ThemeFonts.titleLg());
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Add items from Orders to start checkout.");
        hint.setFont(ThemeFonts.bodyMd());
        hint.setForeground(AppTheme.TEXT_SECONDARY);
        hint.setAlignmentX(CENTER_ALIGNMENT);

        emptyCard.add(title);
        emptyCard.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        emptyCard.add(hint);
        return emptyCard;
    }

    private void updateCalculations() {
        BigDecimal subtotal = currentDraft.getSubtotal();
        BigDecimal tax      = orderService.calculateTax(subtotal);
        BigDecimal fee      = orderService.calculateServiceFee(subtotal);
        BigDecimal total    = orderService.calculateTotal(subtotal);

        subtotalValueLabel.setText(formatCurrency(subtotal));
        taxValueLabel.setText(formatCurrency(tax));
        feeValueLabel.setText(formatCurrency(fee));
        totalValueLabel.setText(formatCurrency(total));

        // Keep legacy labels in sync (in case any other code reads them)
        subtotalLabel.setText("Subtotal: " + formatCurrency(subtotal));
        taxLabel.setText("Tax (10%): " + formatCurrency(tax));
        feeLabel.setText("Service Fee (5%): " + formatCurrency(fee));
    }

    private void updateButtonsState() {
        boolean hasItems = !currentDraft.getItems().isEmpty();
        emptyBtn.setEnabled(hasItems);
        checkoutBtn.setEnabled(hasItems);
        checkoutBtn.setBackground(hasItems ? CONFIRM_GREEN : new Color(0xA5D6B0));
    }

    private String formatCurrency(BigDecimal amount) {
        return priceFormat.format(amount) + " VND";
    }
}
