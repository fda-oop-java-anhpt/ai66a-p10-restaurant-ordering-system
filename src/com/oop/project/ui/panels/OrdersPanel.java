package com.oop.project.ui.panels;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class OrdersPanel extends JPanel {
    public OrdersPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Orders – Coming soon"), BorderLayout.CENTER);
    }
}
