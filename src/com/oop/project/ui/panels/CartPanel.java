package com.oop.project.ui.panels;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.AbstractButton;
import javax.swing.JRadioButton;
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
import com.oop.project.ui.theme.ThemeHelper;
import com.oop.project.ui.theme.ThemeInsets;

public class CartPanel extends JPanel {
    
    private final User currentUser;
    private final OrderDraft currentDraft;
    private final Runnable onUpdate;
    private final OrderService orderService = new OrderService();
    private final OrderRepository orderRepository = new OrderRepository();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");

    private final JPanel itemsListPanel = new JPanel();
    private final JLabel itemCountLabel = new JLabel("0 items");

    private final JLabel subtotalLabel = new JLabel("Subtotal: 0 VND");
    private final JLabel taxLabel = new JLabel("Tax (10%): 0 VND");
    private final JLabel feeLabel = new JLabel("Service Fee (5%): 0 VND");
    private final JLabel totalValueLabel = new JLabel("0 VND");
    private final JLabel paymentBadgeLabel = new JLabel("Payment: CARD");

    private final JButton checkoutBtn = new JButton("Checkout");
    private final JButton emptyBtn = new JButton("Empty Cart");

    private final JRadioButton cardPaymentButton = new JRadioButton("CARD");
    private final JRadioButton cashPaymentButton = new JRadioButton("CASH");

    private String selectedPaymentMethod = "CARD";

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
        add(buildItemsColumn(), BorderLayout.CENTER);
        add(buildSummaryColumn(), BorderLayout.EAST);
    }

    private JPanel buildItemsColumn() {
        TonalCard itemsColumn = new TonalCard(AppTheme.RADIUS_XL, AppTheme.SURFACE_CONTAINER_LOW);
        itemsColumn.setLayout(new BorderLayout(AppTheme.SPACE_3, AppTheme.SPACE_3));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Review Order");
        titleLabel.setFont(ThemeFonts.titleLg());
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);

        itemCountLabel.setFont(ThemeFonts.labelMd());
        itemCountLabel.setForeground(AppTheme.TEXT_SECONDARY);
        itemCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(itemCountLabel, BorderLayout.EAST);

        itemsListPanel.setOpaque(false);
        itemsListPanel.setLayout(new BoxLayout(itemsListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(itemsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(AppTheme.SURFACE_CONTAINER_LOW);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        itemsColumn.add(headerPanel, BorderLayout.NORTH);
        itemsColumn.add(scrollPane, BorderLayout.CENTER);

        return itemsColumn;
    }

    private JPanel buildSummaryColumn() {
        TonalCard summaryColumn = new TonalCard(AppTheme.RADIUS_XL, AppTheme.SURFACE_CONTAINER_LOWEST);
        summaryColumn.setLayout(new BorderLayout(AppTheme.SPACE_4, AppTheme.SPACE_4));
        summaryColumn.setPreferredSize(new Dimension(320, 0));

        JPanel bodyPanel = new JPanel();
        bodyPanel.setOpaque(false);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));

        JLabel summaryTitle = new JLabel("Order Summary");
        summaryTitle.setFont(ThemeFonts.titleLg());
        summaryTitle.setForeground(AppTheme.TEXT_PRIMARY);

        JPanel paymentPanel = buildPaymentToggle();
        configurePaymentBadge();
        JPanel amountsPanel = buildSummaryLines();

        bodyPanel.add(summaryTitle);
        bodyPanel.add(Box.createVerticalStrut(AppTheme.SPACE_4));
        bodyPanel.add(paymentPanel);
        bodyPanel.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        bodyPanel.add(paymentBadgeLabel);
        bodyPanel.add(Box.createVerticalStrut(AppTheme.SPACE_4));
        bodyPanel.add(amountsPanel);

        JPanel actionPanel = new JPanel(new GridLayout(0, 1, 0, AppTheme.SPACE_2));
        actionPanel.setOpaque(false);

        ThemeHelper.applyGhostButton(emptyBtn);
        ThemeHelper.applyPrimaryButton(checkoutBtn);

        emptyBtn.addActionListener(e -> emptyCart());
        checkoutBtn.addActionListener(e -> checkout());

        actionPanel.add(emptyBtn);
        actionPanel.add(checkoutBtn);

        summaryColumn.add(bodyPanel, BorderLayout.CENTER);
        summaryColumn.add(actionPanel, BorderLayout.SOUTH);

        return summaryColumn;
    }

    private JPanel buildPaymentToggle() {
        JPanel panel = new JPanel(new GridLayout(1, 2, AppTheme.SPACE_2, 0));
        panel.setOpaque(false);

        stylePaymentToggle(cardPaymentButton);
        stylePaymentToggle(cashPaymentButton);

        cardPaymentButton.setHorizontalAlignment(SwingConstants.CENTER);
        cashPaymentButton.setHorizontalAlignment(SwingConstants.CENTER);
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
        button.setBackground(AppTheme.SURFACE_CONTAINER_HIGH);
        button.setForeground(AppTheme.PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_2,
            AppTheme.SPACE_2,
            AppTheme.SPACE_2,
            AppTheme.SPACE_2
        ));
    }

    private void configurePaymentBadge() {
        paymentBadgeLabel.setOpaque(true);
        paymentBadgeLabel.setFont(ThemeFonts.labelMd());
        paymentBadgeLabel.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_1,
            AppTheme.SPACE_2,
            AppTheme.SPACE_1,
            AppTheme.SPACE_2
        ));
        paymentBadgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void updatePaymentSelectionUI() {
        boolean isCard = "CARD".equals(selectedPaymentMethod);
        applyPaymentButtonState(cardPaymentButton, isCard);
        applyPaymentButtonState(cashPaymentButton, !isCard);

        paymentBadgeLabel.setText("Payment: " + selectedPaymentMethod);
        paymentBadgeLabel.setBackground(isCard ? AppTheme.PRIMARY_CONTAINER : AppTheme.SECONDARY);
        paymentBadgeLabel.setForeground(isCard ? AppTheme.ON_PRIMARY : AppTheme.ON_SECONDARY);
    }

    private void applyPaymentButtonState(AbstractButton button, boolean active) {
        if (active) {
            button.setBackground(AppTheme.PRIMARY_CONTAINER);
            button.setForeground(AppTheme.ON_PRIMARY);
            return;
        }

        button.setBackground(AppTheme.SURFACE_CONTAINER_HIGH);
        button.setForeground(AppTheme.PRIMARY);
    }

    private JPanel buildSummaryLines() {
        JPanel linesPanel = new JPanel(new GridLayout(0, 1, 0, AppTheme.SPACE_2));
        linesPanel.setOpaque(false);

        subtotalLabel.setFont(ThemeFonts.bodyMd());
        taxLabel.setFont(ThemeFonts.bodyMd());
        feeLabel.setFont(ThemeFonts.bodyMd());

        totalValueLabel.setFont(totalValueLabel.getFont().deriveFont(Font.BOLD, 20f));
        totalValueLabel.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel totalTextLabel = new JLabel("Grand Total");
        totalTextLabel.setFont(ThemeFonts.labelMd());
        totalTextLabel.setForeground(AppTheme.TEXT_SECONDARY);

        linesPanel.add(subtotalLabel);
        linesPanel.add(taxLabel);
        linesPanel.add(feeLabel);
        linesPanel.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        linesPanel.add(totalTextLabel);
        linesPanel.add(totalValueLabel);

        return linesPanel;
    }

    private void editItemAt(int index) {
        if (index < 0 || index >= currentDraft.getItems().size()) {
            return;
        }

        OrderItem item = currentDraft.getItems().get(index);

        EditItemDialog dialog = new EditItemDialog(
            (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this),
            item
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            int newQty = dialog.getQuantity();
            orderService.replaceItem(
                currentDraft,
                index,
                item.getMenuItem(),
                item.getCustomizations(),
                newQty
            );
            refresh();
        }
    }

    private void removeItemAt(int index) {
        if (index < 0 || index >= currentDraft.getItems().size()) {
            return;
        }

        orderService.removeItem(currentDraft, index);
        refresh();
    }

    private void increaseQuantity(int index) {
        if (index < 0 || index >= currentDraft.getItems().size()) {
            return;
        }

        OrderItem item = currentDraft.getItems().get(index);
        orderService.replaceItem(
            currentDraft,
            index,
            item.getMenuItem(),
            item.getCustomizations(),
            item.getQuantity() + 1
        );
        refresh();
    }

    private void decreaseQuantity(int index) {
        if (index < 0 || index >= currentDraft.getItems().size()) {
            return;
        }

        OrderItem item = currentDraft.getItems().get(index);
        if (item.getQuantity() <= 1) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Quantity is 1. Remove this item from cart?",
                "Remove Item",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                removeItemAt(index);
            }
            return;
        }

        orderService.replaceItem(
            currentDraft,
            index,
            item.getMenuItem(),
            item.getCustomizations(),
            item.getQuantity() - 1
        );
        refresh();
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
            (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this),
            currentDraft,
            currentUser.getId(),
            currentUser.getUsername(),
            orderService,
            orderRepository,
            onUpdate
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
        refreshItemsList();
        updateCalculations();
        updateButtonsState();
        revalidate();
        repaint();
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    private void refreshItemsList() {
        itemsListPanel.removeAll();
        List<OrderItem> items = currentDraft.getItems();

        itemCountLabel.setText(items.size() + (items.size() == 1 ? " item" : " items"));

        if (items.isEmpty()) {
            itemsListPanel.add(buildEmptyStateCard());
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);

            OrderLineCard lineCard = new OrderLineCard(
                i,
                item,
                this::increaseQuantity,
                this::decreaseQuantity,
                this::editItemAt,
                this::removeItemAt
            );
            lineCard.setAlignmentX(LEFT_ALIGNMENT);
            itemsListPanel.add(lineCard);
            itemsListPanel.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        }
    }

    private TonalCard buildEmptyStateCard() {
        TonalCard emptyCard = new TonalCard(AppTheme.RADIUS_LG, AppTheme.SURFACE_CONTAINER_LOWEST);
        emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));
        emptyCard.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_8,
            AppTheme.SPACE_4,
            AppTheme.SPACE_8,
            AppTheme.SPACE_4
        ));

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
        BigDecimal tax = orderService.calculateTax(subtotal);
        BigDecimal fee = orderService.calculateServiceFee(subtotal);
        BigDecimal total = orderService.calculateTotal(subtotal);

        subtotalLabel.setText("Subtotal: " + formatCurrency(subtotal));
        taxLabel.setText("Tax (10%): " + formatCurrency(tax));
        feeLabel.setText("Service Fee (5%): " + formatCurrency(fee));
        totalValueLabel.setText(formatCurrency(total));
    }

    private void updateButtonsState() {
        boolean hasItems = !currentDraft.getItems().isEmpty();
        emptyBtn.setEnabled(hasItems);
        checkoutBtn.setEnabled(hasItems);
    }

    private String formatCurrency(BigDecimal amount) {
        return priceFormat.format(amount) + " VND";
    }
}

