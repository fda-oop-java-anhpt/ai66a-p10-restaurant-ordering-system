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

    private final User currentUser;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("Restaurant POS - " + user.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Menu", new MenuPanel(currentUser));
        tabs.addTab("Cart", new CartPanel(/*currentUser */));
        tabs.addTab("Orders", new OrdersPanel(/*currentUser */));
        if (currentUser.isManager()) {
            tabs.addTab("Dashboard", new DashboardPanel(/*currentUser */));
        }
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);

        if (!user.isManager()) {
            for (int i = 0; i < tabs.getTabCount(); i++) {
                if ("Dashboard".equalsIgnoreCase(tabs.getTitleAt(i))) {
                    tabs.removeTabAt(i);
                    break;
                }
            }
        }
    }
}
