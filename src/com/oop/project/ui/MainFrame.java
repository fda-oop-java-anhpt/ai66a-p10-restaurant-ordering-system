package com.oop.project.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.oop.project.model.OrderDraft;
import com.oop.project.model.User;
import com.oop.project.service.AuthService;
import com.oop.project.service.OrderService;
import com.oop.project.ui.panels.CartPanel;
import com.oop.project.ui.panels.DashboardPanel;
import com.oop.project.ui.panels.MenuPanel;
import com.oop.project.ui.panels.OrdersPanel;


public class MainFrame extends JFrame {

    private final User currentUser;
    private final AuthService authService = new AuthService();
    private final OrderService orderService = new OrderService();
    private OrderDraft sharedOrderDraft;
    private OrdersPanel ordersPanel;
    private CartPanel cartPanel;
    private DashboardPanel dashboardPanel;

    public MainFrame(User user) {
        this.currentUser = user;
        this.sharedOrderDraft = orderService.createOrder(user.getId());
        
        setTitle("Restaurant POS - " + user.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        ordersPanel = new OrdersPanel(currentUser, sharedOrderDraft, this::refreshCart);
        cartPanel = new CartPanel(currentUser, sharedOrderDraft, this::onCartUpdated);
        if (currentUser.isManager()) {
            dashboardPanel = new DashboardPanel();
        }

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Menu", new MenuPanel(currentUser, () -> {
            ordersPanel.reloadMenuItems();
            ordersPanel.refresh();
        }));
        tabs.addTab("Cart", cartPanel);
        tabs.addTab("Orders", ordersPanel);
        if (currentUser.isManager()) {
            tabs.addTab("Dashboard", dashboardPanel);
            tabs.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int selectedIndex = tabs.getSelectedIndex();
                    if (selectedIndex >= 0 && "Dashboard".equalsIgnoreCase(tabs.getTitleAt(selectedIndex))) {
                        dashboardPanel.refreshDashboardData();
                    }
                }
            });
        }
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        add(buildHeaderPanel(), BorderLayout.NORTH);

        if (!user.isManager()) {
            for (int i = 0; i < tabs.getTabCount(); i++) {
                if ("Dashboard".equalsIgnoreCase(tabs.getTitleAt(i))) {
                    tabs.removeTabAt(i);
                    break;
                }
            }
        }
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> handleLogout());
        panel.add(logoutBtn);
        return panel;
    }

    private void refreshCart() {
        cartPanel.refresh();
    }

    private void onCartUpdated() {
        ordersPanel.refresh();
        if (dashboardPanel != null) {
            dashboardPanel.refreshDashboardData();
        }
    }

    private void handleLogout() {
        authService.logout(currentUser);
        dispose();
        new LoginFrame().setVisible(true);
    }
}
