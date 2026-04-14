package com.oop.project.ui.panels;

import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.oop.project.model.OrderItem;

public class EditItemDialog extends JDialog {
    private final OrderItem orderItem;
    private JSpinner quantitySpinner;
    private boolean confirmed = false;

    public EditItemDialog(JFrame parent, OrderItem item) {
        super(parent, "Edit Item", true);
        this.orderItem = item;
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        
        buildDialog();
    }

    private void buildDialog() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel itemPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        itemPanel.add(new JLabel("Item: " + orderItem.getMenuItem().getName()));

        JPanel qtyPanel = new JPanel();
        qtyPanel.add(new JLabel("Quantity:"));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(
            orderItem.getQuantity(), 1, 999, 1
        ));
        qtyPanel.add(quantitySpinner);
        itemPanel.add(qtyPanel);

        mainPanel.add(itemPanel);

        JPanel buttonPanel = new JPanel();
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel);

        add(mainPanel);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getQuantity() {
        return (Integer) quantitySpinner.getValue();
    }
}
