package com.oop.project.ui.panels;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class DashboardPanel extends JPanel {
    public DashboardPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Dashboard – Coming soon"), BorderLayout.CENTER);
    }
}
