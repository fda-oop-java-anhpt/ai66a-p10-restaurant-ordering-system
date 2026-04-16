package com.oop.project.ui.theme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public final class ThemeHelper {

    private static final String PRIMARY_HOVER_MARKER = "theme.primary.hover";
    private static final String GHOST_HOVER_MARKER = "theme.ghost.hover";

    private ThemeHelper() {
    }

    public static void applyPrimaryButton(JButton button) {
        button.setFont(ThemeFonts.bodyLg().deriveFont(Font.BOLD));
        button.setBackground(AppTheme.SECONDARY);
        button.setForeground(AppTheme.ON_SECONDARY);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_3,
            AppTheme.SPACE_6,
            AppTheme.SPACE_3,
            AppTheme.SPACE_6
        ));

        if (button.getClientProperty(PRIMARY_HOVER_MARKER) == null) {
            Color base = AppTheme.SECONDARY;
            Color hover = base.darker();
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(hover);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(base);
                }
            });
            button.putClientProperty(PRIMARY_HOVER_MARKER, Boolean.TRUE);
        }
    }

    public static void applyGhostButton(JButton button) {
        button.setFont(ThemeFonts.bodyMd());
        button.setBackground(AppTheme.SURFACE_CONTAINER_HIGH);
        button.setForeground(AppTheme.PRIMARY);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_2,
            AppTheme.SPACE_4,
            AppTheme.SPACE_2,
            AppTheme.SPACE_4
        ));

        if (button.getClientProperty(GHOST_HOVER_MARKER) == null) {
            Color base = AppTheme.SURFACE_CONTAINER_HIGH;
            Color hover = AppTheme.SURFACE_CONTAINER;
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(hover);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(base);
                }
            });
            button.putClientProperty(GHOST_HOVER_MARKER, Boolean.TRUE);
        }
    }

    public static void applyTableStyle(JTable table) {
        table.setFont(ThemeFonts.bodyMd());
        table.setRowHeight(AppTheme.SPACE_8 + AppTheme.SPACE_2);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(AppTheme.SURFACE);
        table.setForeground(AppTheme.ON_BACKGROUND);
        table.setGridColor(AppTheme.SURFACE_CONTAINER_LOW);

        JTableHeader header = table.getTableHeader();
        header.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        header.setBackground(AppTheme.SURFACE_CONTAINER);
        header.setForeground(AppTheme.ON_SURFACE_VARIANT);
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new ZebraTableRenderer());
    }

    private static final class ZebraTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
        ) {
            Component component = super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column
            );

            if (component instanceof DefaultTableCellRenderer renderer) {
                renderer.setHorizontalAlignment(SwingConstants.LEFT);
                renderer.setBorder(BorderFactory.createEmptyBorder(0, AppTheme.SPACE_2, 0, AppTheme.SPACE_2));
            }

            if (component != null && !isSelected) {
                component.setBackground(row % 2 == 0 ? AppTheme.SURFACE : AppTheme.SURFACE_CONTAINER_LOW);
                component.setForeground(AppTheme.ON_BACKGROUND);
            }

            return component;
        }
    }
}