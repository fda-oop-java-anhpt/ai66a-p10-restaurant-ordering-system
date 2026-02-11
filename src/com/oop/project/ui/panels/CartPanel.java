package com.oop.project.ui.panels;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class CartPanel extends JPanel {
    public CartPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Cart – Coming soon"), BorderLayout.CENTER);
    }
}
