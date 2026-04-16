package com.oop.project;

import java.sql.Connection;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.oop.project.db.DBConnection;
import com.oop.project.ui.LoginFrame;
import com.oop.project.ui.theme.ThemeFonts;

public class Main {

    public static void main(String[] args) {
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
            System.out.println("Database connected");
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage() + "\n\nFix by setting DB_URL/DB_USER/DB_PASSWORD.",
                    "Database connection failed",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {
                }
            }
        }

        SwingUtilities.invokeLater(() -> {
            ThemeFonts.initialize();
            new LoginFrame().setVisible(true);
        });
    }
}
