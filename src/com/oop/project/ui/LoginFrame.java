package com.oop.project.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

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

public class LoginFrame extends JFrame{

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private char defaultEchoChar;
    private boolean passwordVisible;
    private final Icon eyeOpenIcon = new EyeIcon(false);
    private final Icon eyeClosedIcon = new EyeIcon(true);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("Login");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        defaultEchoChar = txtPassword.getEchoChar();
        passwordVisible = false;

        JButton btnTogglePassword = new JButton(eyeClosedIcon);
        btnTogglePassword.setPreferredSize(new Dimension(34, 24));
        btnTogglePassword.setMinimumSize(new Dimension(34, 24));
        btnTogglePassword.setMaximumSize(new Dimension(34, 24));
        btnTogglePassword.setFocusable(false);
        btnTogglePassword.setToolTipText("Show password");
        btnTogglePassword.addActionListener(e -> togglePasswordVisibility(btnTogglePassword));

        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        passwordPanel.add(txtPassword, BorderLayout.CENTER);
        passwordPanel.add(btnTogglePassword, BorderLayout.EAST);
        
        JButton btnlogin = new JButton("Login");
        
        btnlogin.addActionListener(e -> handleLogin());

        panel.add(new JLabel("Username"));
        panel.add(txtUsername);
        panel.add(new JLabel("Password"));
        panel.add(passwordPanel);
        panel.add(btnlogin);

        add(panel);
    }

    private void handleLogin() {
        String username = txtUsername.getText();
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
        public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.DARK_GRAY);
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
