package com.oop.project.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.oop.project.model.User;
import com.oop.project.service.AuthService;
import com.oop.project.ui.theme.AppTheme;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private char defaultEchoChar;
    private boolean passwordVisible;

    private JButton managerRoleBtn;
    private JButton staffRoleBtn;
    private String selectedRole = "STAFF";

    private final Icon eyeOpenIcon = new EyeIcon(false);
    private final Icon eyeClosedIcon = new EyeIcon(true);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("Login");
        setSize(460, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.SURFACE);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(AppTheme.SURFACE);

        root.add(buildLoginCard());
        add(root, BorderLayout.CENTER);
    }

    private JPanel buildLoginCard() {
        TonalCardPanel card = new TonalCardPanel(AppTheme.SURFACE_CONTAINER_LOWEST, AppTheme.RADIUS_XL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(360, 430));
        card.setBorderPadding(
            AppTheme.SPACE_6,
            AppTheme.SPACE_6,
            AppTheme.SPACE_6,
            AppTheme.SPACE_6
        );

        JLabel title = new JLabel("Login");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(AppTheme.ON_BACKGROUND);
        title.setFont(headlineFont(24f));

        JLabel subtitle = new JLabel("Use your assigned staff or manager account");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(AppTheme.ON_SURFACE_VARIANT);
        subtitle.setFont(bodyFont(13f));

        JPanel roleRow = buildRoleRow();

        JLabel usernameLabel = createFieldLabel("Username");
        JPanel usernameWrap = buildUsernameField();

        JLabel passwordLabel = createFieldLabel("Password");
        JPanel passwordWrap = buildPasswordField();

        JButton loginBtn = new JButton("Login");
        stylePrimaryButton(loginBtn);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.addActionListener(e -> handleLogin());

        JLabel helper = new JLabel("Role pills are UI-first; authentication still uses account credentials");
        helper.setAlignmentX(Component.CENTER_ALIGNMENT);
        helper.setForeground(AppTheme.ON_SURFACE_VARIANT);
        helper.setFont(bodyFont(11f));

        card.add(title);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_6));
        card.add(roleRow);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_6));
        card.add(usernameLabel);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        card.add(usernameWrap);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_4));
        card.add(passwordLabel);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        card.add(passwordWrap);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_6));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        card.add(helper);

        return card;
    }

    private JPanel buildRoleRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, AppTheme.SPACE_2, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        managerRoleBtn = new JButton("MANAGER");
        staffRoleBtn = new JButton("STAFF");

        managerRoleBtn.addActionListener(e -> {
            selectedRole = "MANAGER";
            updateRoleButtons();
        });

        staffRoleBtn.addActionListener(e -> {
            selectedRole = "STAFF";
            updateRoleButtons();
        });

        updateRoleButtons();

        row.add(managerRoleBtn);
        row.add(staffRoleBtn);

        return row;
    }

    private void updateRoleButtons() {
        styleRoleButton(managerRoleBtn, "MANAGER".equals(selectedRole));
        styleRoleButton(staffRoleBtn, "STAFF".equals(selectedRole));
    }

    private JPanel buildUsernameField() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        wrap.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtUsername = new JTextField();
        txtUsername.setBackground(AppTheme.SURFACE_CONTAINER_LOW);
        txtUsername.setForeground(AppTheme.ON_BACKGROUND);
        txtUsername.setCaretColor(AppTheme.ON_BACKGROUND);
        txtUsername.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        txtUsername.setFont(bodyFont(13f));

        TonalCardPanel fieldCard = new TonalCardPanel(AppTheme.SURFACE_CONTAINER_LOW, 20);
        fieldCard.setLayout(new BorderLayout());
        fieldCard.setBorderPadding(0, 0, 0, 0);
        fieldCard.add(txtUsername, BorderLayout.CENTER);

        wrap.add(fieldCard, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildPasswordField() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        wrap.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.setBackground(AppTheme.SURFACE_CONTAINER_LOW);
        txtPassword.setForeground(AppTheme.ON_BACKGROUND);
        txtPassword.setCaretColor(AppTheme.ON_BACKGROUND);
        txtPassword.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        txtPassword.setFont(bodyFont(13f));

        defaultEchoChar = txtPassword.getEchoChar();
        passwordVisible = false;

        JButton btnTogglePassword = new JButton(eyeClosedIcon);
        btnTogglePassword.setPreferredSize(new Dimension(42, 42));
        btnTogglePassword.setFocusable(false);
        btnTogglePassword.setToolTipText("Show password");
        btnTogglePassword.setBackground(AppTheme.SURFACE_CONTAINER_LOW);
        btnTogglePassword.setForeground(AppTheme.ON_BACKGROUND);
        btnTogglePassword.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btnTogglePassword.addActionListener(e -> togglePasswordVisibility(btnTogglePassword));

        TonalCardPanel fieldCard = new TonalCardPanel(AppTheme.SURFACE_CONTAINER_LOW, 20);
        fieldCard.setLayout(new BorderLayout());
        fieldCard.setBorderPadding(0, 0, 0, 0);
        fieldCard.add(txtPassword, BorderLayout.CENTER);
        fieldCard.add(btnTogglePassword, BorderLayout.EAST);

        wrap.add(fieldCard, BorderLayout.CENTER);
        return wrap;
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        User user = authService.authenticate(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(
                this,
                "Invalid username or password",
                "Login failed",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        dispose();
        new MainFrame(user).setVisible(true);
    }

    private void togglePasswordVisibility(JButton btnTogglePassword) {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            txtPassword.setEchoChar((char) 0);
            btnTogglePassword.setToolTipText("Hide password");
            btnTogglePassword.setIcon(eyeOpenIcon);
            return;
        }

        txtPassword.setEchoChar(defaultEchoChar);
        btnTogglePassword.setToolTipText("Show password");
        btnTogglePassword.setIcon(eyeClosedIcon);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(AppTheme.ON_BACKGROUND);
        label.setFont(headlineFont(13f));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setHorizontalAlignment(JLabel.CENTER);
        return label;
    }

    private void styleRoleButton(JButton button, boolean active) {
        button.setFocusable(false);
        button.setFont(headlineFont(12f));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        if (active) {
            button.setBackground(AppTheme.SECONDARY);
            button.setForeground(AppTheme.ON_SECONDARY);
        } else {
            button.setBackground(AppTheme.SURFACE_CONTAINER);
            button.setForeground(AppTheme.ON_BACKGROUND);
        }
    }

    private void stylePrimaryButton(JButton button) {
        button.setFocusable(false);
        button.setBackground(AppTheme.PRIMARY);
        button.setForeground(AppTheme.ON_PRIMARY);
        button.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        button.setFont(headlineFont(13f));
    }

    private Font headlineFont(float size) {
        return new Font("Segoe UI", Font.BOLD, Math.round(size));
    }

    private Font bodyFont(float size) {
        return new Font("Segoe UI", Font.PLAIN, Math.round(size));
    }

    private static class TonalCardPanel extends JPanel {
        private final Color fillColor;
        private final int radius;

        TonalCardPanel(Color fillColor, int radius) {
            this.fillColor = fillColor;
            this.radius = radius;
            setOpaque(false);
        }

        void setBorderPadding(int top, int left, int bottom, int right) {
            setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class EyeIcon implements Icon {
        private final boolean crossed;

        private EyeIcon(boolean crossed) {
            this.crossed = crossed;
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppTheme.ON_SURFACE_VARIANT);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int left = x + 1;
            int top = y + 2;
            int width = 12;
            int height = 9;

            g2.drawArc(left, top, width, height, 0, 180);
            g2.drawArc(left, top, width, height, 0, -180);
            g2.fillOval(x + 5, y + 5, 4, 4);

            if (crossed) {
                g2.drawLine(x + 2, y + 12, x + 12, y + 2);
            }

            g2.dispose();
        }
    }
}