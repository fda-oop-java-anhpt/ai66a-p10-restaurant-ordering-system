package com.oop.project.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.oop.project.model.User;
import com.oop.project.ui.panels.CartPanel;
import com.oop.project.ui.panels.DashboardPanel;
import com.oop.project.ui.panels.MenuPanel;
import com.oop.project.ui.panels.OrdersPanel;


public class MainFrame extends JFrame {
    public MainFrame(User user) {
        setTitle("Restaurant POS" + user.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Menu", new MenuPanel());
        tabs.addTab("Cart", new CartPanel());
        tabs.addTab("Orders", new OrdersPanel());
        tabs.addTab("Dashboard", new DashboardPanel());

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);

        if (!user.isManager()) {
            tabs.removeTabAt(3);
        }
    }
}
