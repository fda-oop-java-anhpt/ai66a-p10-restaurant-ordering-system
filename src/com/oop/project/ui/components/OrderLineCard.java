package com.oop.project.ui.components;

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
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.oop.project.model.CustomizationOption;
import com.oop.project.model.OrderItem;
import com.oop.project.ui.theme.AppTheme;
import com.oop.project.ui.theme.ThemeFonts;

public class OrderLineCard extends JPanel {

    private static final Color CARD_BG       = Color.WHITE;
    private static final Color DIVIDER_COLOR = new Color(0xE8E8E8);
    private static final Color TAG_BG        = new Color(0xEEF0F2);
    private static final Color TAG_FG        = new Color(0x555B62);
    private static final Color EDIT_FG       = new Color(0x666666);
    private static final Color REMOVE_FG     = new Color(0xBA1A1A);
    private static final Color QTY_BORDER    = new Color(0xCCCCCC);

    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");

    public OrderLineCard(
        int index,
        OrderItem item,
        IntConsumer onIncrease,
        IntConsumer onDecrease,
        IntConsumer onEdit,
        IntConsumer onRemove
    ) {
        setLayout(new BorderLayout(AppTheme.SPACE_3, 0));
        setBackground(CARD_BG);
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER_COLOR),
            BorderFactory.createEmptyBorder(
                AppTheme.SPACE_3, AppTheme.SPACE_3,
                AppTheme.SPACE_3, AppTheme.SPACE_3)
        ));

        // ── Left image placeholder ──────────────────────────────────────────
        JPanel imageBox = buildImageBox();

        // ── Right content ───────────────────────────────────────────────────
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Row 1: name + price
        JPanel nameRow = new JPanel(new BorderLayout());
        nameRow.setOpaque(false);
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(item.getMenuItem().getName());
        nameLabel.setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD));
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel priceLabel = new JLabel(formatCurrency(item.getLineTotal()));
        priceLabel.setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD));
        priceLabel.setForeground(AppTheme.TEXT_PRIMARY);

        nameRow.add(nameLabel, BorderLayout.WEST);
        nameRow.add(priceLabel, BorderLayout.EAST);

        // Row 2: customization chip tags
        JPanel tagsRow = buildTagsRow(item);
        tagsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Row 3: qty controls + EDIT / REMOVE
        JPanel actionsRow = buildActionsRow(index, item.getQuantity(),
                onIncrease, onDecrease, onEdit, onRemove);
        actionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(nameRow);
        content.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        content.add(tagsRow);
        content.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        content.add(actionsRow);

        add(imageBox, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    // ── Image placeholder ────────────────────────────────────────────────────
    private JPanel buildImageBox() {
        JPanel box = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Rounded rect background
                g2.setColor(new Color(0xE8EDEA));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Subtle border
                g2.setColor(new Color(0xCDD5D0));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                // Fork & knife icon
                g2.setColor(new Color(0x7A9A85));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                // Fork
                g2.drawLine(cx - 7, cy - 10, cx - 7, cy + 10);
                g2.drawLine(cx - 7, cy - 4, cx - 3, cy - 4);
                g2.drawLine(cx - 3, cy - 10, cx - 3, cy + 5);
                // Knife
                g2.drawLine(cx + 5, cy - 10, cx + 5, cy + 10);
                g2.drawArc(cx + 2, cy - 10, 6, 7, 0, -180);
                g2.dispose();
            }
        };
        box.setPreferredSize(new Dimension(58, 58));
        box.setMaximumSize(new Dimension(58, 58));
        box.setOpaque(false);
        return box;
    }

    // ── Customization chip tags ──────────────────────────────────────────────
    private JPanel buildTagsRow(OrderItem item) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_1, 0));
        row.setOpaque(false);

        for (CustomizationOption opt : item.getCustomizations()) {
            row.add(buildChip(opt.getName().toUpperCase()));
        }
        if (item.getCustomizations().isEmpty()) {
            row.add(buildChip("NO CUSTOMIZATION"));
        }

        String note = item.getNote() == null ? "" : item.getNote().trim();
        if (!note.isBlank()) {
            row.add(buildChip(("NOTE: " + note).toUpperCase()));
        }
        return row;
    }

    private JLabel buildChip(String text) {
        JLabel chip = new JLabel(text);
        chip.setFont(ThemeFonts.labelSm().deriveFont(Font.BOLD));
        chip.setForeground(TAG_FG);
        chip.setBackground(TAG_BG);
        chip.setOpaque(true);
        chip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD4D8DC), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        return chip;
    }

    // ── Actions row: [-] qty [+]   EDIT  REMOVE ─────────────────────────────
    private JPanel buildActionsRow(
        int index, int quantity,
        IntConsumer onIncrease, IntConsumer onDecrease,
        IntConsumer onEdit, IntConsumer onRemove
    ) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        // Qty controls
        JPanel qtyGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        qtyGroup.setOpaque(false);

        JButton decreaseBtn = buildQtyButton("-");
        JLabel  qtyLbl     = new JLabel(String.valueOf(quantity));
        JButton increaseBtn = buildQtyButton("+");

        qtyLbl.setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD));
        qtyLbl.setForeground(AppTheme.TEXT_PRIMARY);
        qtyLbl.setHorizontalAlignment(JLabel.CENTER);
        qtyLbl.setPreferredSize(new Dimension(32, 28));
        qtyLbl.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, QTY_BORDER));

        decreaseBtn.addActionListener(e -> onDecrease.accept(index));
        increaseBtn.addActionListener(e -> onIncrease.accept(index));

        qtyGroup.add(decreaseBtn);
        qtyGroup.add(qtyLbl);
        qtyGroup.add(increaseBtn);

        // Edit / Remove links
        JPanel linkGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACE_3, 0));
        linkGroup.setOpaque(false);

        JButton editBtn   = buildLinkButton("EDIT",   EDIT_FG);
        JButton removeBtn = buildLinkButton("REMOVE", REMOVE_FG);

        editBtn.addActionListener(e   -> onEdit.accept(index));
        removeBtn.addActionListener(e -> onRemove.accept(index));

        linkGroup.add(editBtn);
        linkGroup.add(removeBtn);

        row.add(qtyGroup,  BorderLayout.WEST);
        row.add(linkGroup, BorderLayout.EAST);
        return row;
    }

    private JButton buildQtyButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD));
        btn.setBackground(Color.WHITE);
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(QTY_BORDER, 1));
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setMaximumSize(new Dimension(28, 28));
        return btn;
    }

    private JButton buildLinkButton(String text, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        btn.setForeground(fg);
        btn.setBackground(null);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return btn;
    }

    private String formatCurrency(BigDecimal amount) {
        return priceFormat.format(amount) + " VND";
    }
}