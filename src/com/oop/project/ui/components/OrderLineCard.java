package com.oop.project.ui.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.oop.project.model.OrderItem;
import com.oop.project.ui.theme.AppTheme;
import com.oop.project.ui.theme.ThemeFonts;
import com.oop.project.ui.theme.ThemeHelper;

public class OrderLineCard extends TonalCard {

    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");

    public OrderLineCard(
        int index,
        OrderItem item,
        IntConsumer onIncrease,
        IntConsumer onDecrease,
        IntConsumer onEdit,
        IntConsumer onRemove
    ) {
        super(AppTheme.RADIUS_LG, AppTheme.SURFACE_CONTAINER_LOWEST);
        setLayout(new BorderLayout(AppTheme.SPACE_3, AppTheme.SPACE_2));
        setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_4,
            AppTheme.SPACE_4,
            AppTheme.SPACE_4,
            AppTheme.SPACE_4
        ));

        add(buildInfoSection(item), BorderLayout.CENTER);
        add(buildActionsSection(index, item.getQuantity(), onIncrease, onDecrease, onEdit, onRemove), BorderLayout.SOUTH);
    }

    private JPanel buildInfoSection(OrderItem item) {
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(item.getMenuItem().getName());
        nameLabel.setFont(ThemeFonts.titleLg());
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel customizationLabel = new JLabel("Options: " + item.getCustomizationSummary());
        customizationLabel.setFont(ThemeFonts.labelMd());
        customizationLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel pricingLabel = new JLabel(
            formatCurrency(item.getUnitPrice()) + " x " + item.getQuantity() + " = " + formatCurrency(item.getLineTotal())
        );
        pricingLabel.setFont(ThemeFonts.bodyMd());
        pricingLabel.setForeground(AppTheme.TEXT_PRIMARY);

        infoPanel.add(nameLabel);
        infoPanel.add(customizationLabel);
        infoPanel.add(pricingLabel);

        return infoPanel;
    }

    private JPanel buildActionsSection(
        int index,
        int quantity,
        IntConsumer onIncrease,
        IntConsumer onDecrease,
        IntConsumer onEdit,
        IntConsumer onRemove
    ) {
        JPanel actionsPanel = new JPanel(new BorderLayout());
        actionsPanel.setOpaque(false);

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_2, 0));
        qtyPanel.setOpaque(false);

        JButton decreaseButton = new JButton("-");
        JButton increaseButton = new JButton("+");
        JLabel qtyLabel = new JLabel("Qty: " + quantity);
        qtyLabel.setFont(ThemeFonts.bodyMd());
        qtyLabel.setForeground(AppTheme.TEXT_PRIMARY);

        ThemeHelper.applyGhostButton(decreaseButton);
        ThemeHelper.applyGhostButton(increaseButton);

        decreaseButton.addActionListener(e -> onDecrease.accept(index));
        increaseButton.addActionListener(e -> onIncrease.accept(index));

        qtyPanel.add(decreaseButton);
        qtyPanel.add(qtyLabel);
        qtyPanel.add(increaseButton);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACE_2, 0));
        actionButtons.setOpaque(false);

        JButton editButton = new JButton("Edit");
        JButton removeButton = new JButton("Remove");
        ThemeHelper.applyGhostButton(editButton);
        ThemeHelper.applyGhostButton(removeButton);
        removeButton.setForeground(AppTheme.ERROR);

        editButton.addActionListener(e -> onEdit.accept(index));
        removeButton.addActionListener(e -> onRemove.accept(index));

        actionButtons.add(editButton);
        actionButtons.add(removeButton);

        actionsPanel.add(qtyPanel, BorderLayout.WEST);
        actionsPanel.add(actionButtons, BorderLayout.EAST);

        return actionsPanel;
    }

    private String formatCurrency(BigDecimal amount) {
        return priceFormat.format(amount) + " VND";
    }
}