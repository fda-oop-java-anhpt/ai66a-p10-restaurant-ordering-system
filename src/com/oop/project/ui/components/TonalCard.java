package com.oop.project.ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.oop.project.ui.theme.AppTheme;
import com.oop.project.ui.theme.ThemeInsets;

public class TonalCard extends JPanel {

    private int radius;
    private Color backgroundColor;

    public TonalCard() {
        this(AppTheme.RADIUS_MD, AppTheme.SURFACE_CONTAINER_LOWEST);
    }

    public TonalCard(int radius, Color backgroundColor) {
        this.radius = radius;
        this.backgroundColor = backgroundColor;
        setOpaque(false);
        setBorder(new EmptyBorder(ThemeInsets.card()));
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = Math.max(0, radius);
        repaint();
    }

    public Color getCardBackgroundColor() {
        return backgroundColor;
    }

    public void setCardBackgroundColor(Color backgroundColor) {
        if (backgroundColor == null) {
            return;
        }
        this.backgroundColor = backgroundColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2);
        g2.dispose();
        super.paintComponent(g);
    }
}