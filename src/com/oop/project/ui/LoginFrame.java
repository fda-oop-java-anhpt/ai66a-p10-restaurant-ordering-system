package com.oop.project.ui;

import javax.swing.BoxLayout;
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
        
        JButton btnlogin = new JButton("Login");
        
        btnlogin.addActionListener(e -> handleLogin());

        panel.add(new JLabel("Username"));
        panel.add(txtUsername);
        panel.add(new JLabel("Password"));
        panel.add(txtPassword);
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
}
