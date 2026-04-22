package com.oop.project.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.oop.project.model.OrderDraft;
import com.oop.project.model.User;
import com.oop.project.service.AuthService;
import com.oop.project.service.OrderService;
import com.oop.project.ui.panels.CartPanel;
import com.oop.project.ui.panels.DashboardPanel;
import com.oop.project.ui.panels.ManagerOrdersPanel;
import com.oop.project.ui.panels.MenuPanel;
import com.oop.project.ui.panels.OrdersPanel;
import com.oop.project.ui.theme.AppTheme;

public class MainFrame extends JFrame {

    private static final String SCREEN_MENU = "MENU";
    private static final String SCREEN_CART = "CART";
    private static final String SCREEN_ORDERS = "ORDERS";
    private static final String SCREEN_DASHBOARD = "DASHBOARD";

    private final User currentUser;
    private final AuthService authService = new AuthService();
    private final OrderService orderService = new OrderService();

    private final OrderDraft sharedOrderDraft;

    private MenuPanel menuPanel;
    private OrdersPanel staffOrdersPanel;
    private ManagerOrdersPanel managerOrdersPanel;
    private JPanel ordersPanel;
    private CartPanel cartPanel;
    private DashboardPanel dashboardPanel;

    private final JPanel contentPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel sessionBadgeLabel;
    private JPanel topbar;

    private String activeScreen = SCREEN_MENU;
    private boolean internalRefreshing = false;

    public MainFrame(User user) {
        this.currentUser = user;
        this.sharedOrderDraft = orderService.createOrder(user.getId());

        setTitle("Restaurant POS - " + user.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        initPanels();
        initLayout();
        showScreen(SCREEN_MENU);
    }

    private void initPanels() {
        if (currentUser.isManager()) {
            managerOrdersPanel = new ManagerOrdersPanel(currentUser);
            ordersPanel = managerOrdersPanel;
        } else {
            staffOrdersPanel = new OrdersPanel(currentUser, sharedOrderDraft, this::refreshCart);
            ordersPanel = staffOrdersPanel;
        }

        cartPanel = new CartPanel(currentUser, sharedOrderDraft, this::onCartUpdated);

        menuPanel = new MenuPanel(
            currentUser,
            sharedOrderDraft,
            () -> {
                refreshOrdersPanelOnMenuChanged();
            },
            this::refreshCart
        );

        dashboardPanel = currentUser.isManager() ? new DashboardPanel() : null;
    }

    private void initLayout() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BACKGROUND);

        getContentPane().add(buildSidebar(), BorderLayout.WEST);
        getContentPane().add(buildMainArea(), BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(190, 0));
        sidebar.setBackground(AppTheme.PRIMARY);
        sidebar.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_4,
            AppTheme.SPACE_3,
            AppTheme.SPACE_4,
            AppTheme.SPACE_3
        ));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel appLabel = new JLabel("Restaurant POS");
        appLabel.setForeground(AppTheme.ON_PRIMARY);
        appLabel.setFont(AppTheme.FONT_DISPLAY_MEDIUM);

        JLabel userLabel = new JLabel(currentUser.getUsername());
        userLabel.setForeground(AppTheme.ON_PRIMARY);
        userLabel.setFont(AppTheme.FONT_BODY);

        JLabel roleLabel = new JLabel(currentUser.isManager() ? "Manager" : "Staff");
        roleLabel.setOpaque(true);
        roleLabel.setBackground(AppTheme.PRIMARY_CONTAINER);
        roleLabel.setForeground(AppTheme.ON_PRIMARY);
        roleLabel.setFont(AppTheme.FONT_LABEL);
        roleLabel.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_1,
            AppTheme.SPACE_2,
            AppTheme.SPACE_1,
            AppTheme.SPACE_2
        ));

        top.add(appLabel);
        top.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        top.add(userLabel);
        top.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        top.add(roleLabel);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_6, 0, 0, 0));

        nav.add(createNavButton("Menu", SCREEN_MENU));
        nav.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        nav.add(createNavButton("Orders", SCREEN_ORDERS));

        if (!currentUser.isManager()) {
            nav.add(Box.createVerticalStrut(AppTheme.SPACE_2));
            nav.add(createNavButton("Cart", SCREEN_CART));
        }

        if (currentUser.isManager()) {
            nav.add(Box.createVerticalStrut(AppTheme.SPACE_2));
            nav.add(createNavButton("Dashboard", SCREEN_DASHBOARD));
        }

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        logoutBtn.setBackground(AppTheme.ERROR);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(AppTheme.FONT_BUTTON);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> handleLogout());

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(logoutBtn);

        sidebar.add(top, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_4,
            AppTheme.SPACE_4,
            AppTheme.SPACE_4,
            AppTheme.SPACE_4
        ));

        main.add(buildTopbar(), BorderLayout.NORTH);
        main.add(buildContentArea(), BorderLayout.CENTER);

        return main;
    }

    private JPanel buildTopbar() {
        topbar = new JPanel(new BorderLayout());
        topbar.setBackground(AppTheme.SURFACE_CONTAINER_LOWEST);
        topbar.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_4,
            AppTheme.SPACE_4,
            AppTheme.SPACE_4,
            AppTheme.SPACE_4
        ));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        titleLabel = new JLabel();
        titleLabel.setFont(AppTheme.FONT_DISPLAY_MEDIUM);
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);

        subtitleLabel = new JLabel();
        subtitleLabel.setFont(AppTheme.FONT_BODY_SECONDARY);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);

        left.add(titleLabel);
        left.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        left.add(subtitleLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);

        sessionBadgeLabel = new JLabel();
        sessionBadgeLabel.setOpaque(true);
        sessionBadgeLabel.setBackground(AppTheme.SURFACE_CONTAINER);
        sessionBadgeLabel.setForeground(AppTheme.TEXT_PRIMARY);
        sessionBadgeLabel.setFont(AppTheme.FONT_BUTTON);
        sessionBadgeLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        right.add(sessionBadgeLabel);

        topbar.add(left, BorderLayout.WEST);
        topbar.add(right, BorderLayout.EAST);

        return topbar;
    }

    private JPanel buildContentArea() {
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(AppTheme.BACKGROUND);

        contentPanel.add(menuPanel, SCREEN_MENU);
        contentPanel.add(cartPanel, SCREEN_CART);
        contentPanel.add(ordersPanel, SCREEN_ORDERS);

        if (dashboardPanel != null) {
            contentPanel.add(dashboardPanel, SCREEN_DASHBOARD);
        }

        return contentPanel;
    }

    private JButton createNavButton(String text, String key) {
        JButton btn = new JButton(text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setFont(AppTheme.FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_3,
            AppTheme.SPACE_4,
            AppTheme.SPACE_3,
            AppTheme.SPACE_4
        ));
        btn.setOpaque(true);

        btn.addActionListener(e -> showScreen(key));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!key.equals(activeScreen)) {
                    btn.setBackground(AppTheme.PRIMARY_CONTAINER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateNavButtonStyle(btn, key.equals(activeScreen));
            }
        });

        navButtons.put(key, btn);
        updateNavButtonStyle(btn, false);

        return btn;
    }

    private void showScreen(String key) {
        if (SCREEN_DASHBOARD.equals(key) && !currentUser.isManager()) {
            return;
        }

        if (SCREEN_CART.equals(key) && currentUser.isManager()) {
            return;
        }

        activeScreen = key;
        cardLayout.show(contentPanel, key);

        if (SCREEN_ORDERS.equals(key)) {
            refreshOrdersPanel();
        } else if (SCREEN_CART.equals(key)) {
            cartPanel.refresh();
        } else if (SCREEN_DASHBOARD.equals(key) && dashboardPanel != null) {
            dashboardPanel.refreshDashboardData();
        }

        updateTopbar();
        updateNavState();
    }

    private void updateTopbar() {
        if (topbar != null) {
            topbar.setVisible(true);
        }

        if (sessionBadgeLabel != null) {
            sessionBadgeLabel.setText(currentUser.isManager() ? "Manager Session" : "Staff Session");
        }

        switch (activeScreen) {
            case SCREEN_MENU:
                titleLabel.setText("Menu");
                subtitleLabel.setText(
                    currentUser.isManager()
                        ? "Manage menu items, descriptions, and pricing"
                        : "Browse menu items and add them to the active draft order"
                );
                break;

            case SCREEN_CART:
                titleLabel.setText("Cart");
                subtitleLabel.setText("Review the current draft order");
                break;

            case SCREEN_ORDERS:
                titleLabel.setText("Orders");
                subtitleLabel.setText(
                    currentUser.isManager()
                        ? "Track order queue, view details, and update order status"
                        : "Build and customize active draft orders"
                );
                break;

            case SCREEN_DASHBOARD:
                titleLabel.setText("Dashboard");
                subtitleLabel.setText("Revenue and best-seller analytics overview");
                break;

            default:
                titleLabel.setText("Restaurant POS");
                subtitleLabel.setText("");
                break;
        }

        revalidate();
        repaint();
    }

    private void updateNavState() {
        navButtons.forEach((k, v) -> updateNavButtonStyle(v, k.equals(activeScreen)));
    }

    private void updateNavButtonStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(AppTheme.PRIMARY_CONTAINER);
            btn.setForeground(AppTheme.ON_PRIMARY);
        } else {
            btn.setBackground(AppTheme.PRIMARY);
            btn.setForeground(AppTheme.ON_PRIMARY);
        }
    }

    private void refreshCart() {
        if (internalRefreshing) {
            return;
        }

        internalRefreshing = true;
        try {
            cartPanel.refresh();
        } finally {
            internalRefreshing = false;
        }
    }

    private void onCartUpdated() {
        if (internalRefreshing) {
            return;
        }

        refreshOrdersPanel();

        if (SCREEN_DASHBOARD.equals(activeScreen) && dashboardPanel != null) {
            dashboardPanel.refreshDashboardData();
        }
    }

    private void handleLogout() {
        authService.logout(currentUser);
        dispose();
        new LoginFrame().setVisible(true);
    }

    private void refreshOrdersPanelOnMenuChanged() {
        if (currentUser.isManager()) {
            if (managerOrdersPanel != null) {
                managerOrdersPanel.refresh();
            }
            return;
        }

        if (staffOrdersPanel != null) {
            staffOrdersPanel.reloadMenuItems();
            staffOrdersPanel.refresh();
        }
    }

    private void refreshOrdersPanel() {
        if (currentUser.isManager()) {
            if (managerOrdersPanel != null) {
                managerOrdersPanel.refresh();
            }
            return;
        }

        if (staffOrdersPanel != null) {
            staffOrdersPanel.refresh();
        }
    }
}