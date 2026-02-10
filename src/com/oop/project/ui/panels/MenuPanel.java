package com.oop.project.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.oop.project.model.MenuCategory;
import com.oop.project.model.MenuItem;
import com.oop.project.model.User;
import com.oop.project.service.MenuAdminService;
import com.oop.project.service.MenuService;

public class MenuPanel extends JPanel {
    
    private final User currentUser;
    private final MenuService menuService;
    private final MenuAdminService adminService;

    private final JList<MenuCategory> categoryList = new JList<>();
    private final JTable itemTable = new JTable();

    public MenuPanel(User user) {
        this.currentUser = user;
        this.menuService = new MenuService();
        this.adminService = new MenuAdminService();

        setLayout(new BorderLayout(10, 10));

        initCategoryList();
        initItemTable();

        add(new JScrollPane(categoryList), BorderLayout.WEST);
        add(new JScrollPane(itemTable), BorderLayout.CENTER);

        if (user.isManager()) {
            add(buildAdminPanel(), BorderLayout.SOUTH);
        }

        loadCategories();
    }

    private void initCategoryList() {
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                MenuCategory selected = categoryList.getSelectedValue();
                if (selected != null) {
                    loadMenuItems(selected.getId());
                }
            }
        });
        categoryList.setPreferredSize(new Dimension(200, 0));
    }

    private void initItemTable() {
        itemTable.setModel(new DefaultTableModel(
            new Object[]{"ID", "Name", "Description", "Price"}, 0
        ));
    }

    private JPanel buildAdminPanel() {
        JButton editPriceBtn = new JButton("Edit Price");

        editPriceBtn.addActionListener(e -> editSelectedPrice());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(editPriceBtn);
        return panel;
    }

    private void editSelectedPrice() {
        int row = itemTable.getSelectedRow();
        if (row == -1) return;

        int itemId = (int) itemTable.getValueAt(row, 0);
        String currentPrice = itemTable.getValueAt(row, 3).toString();

        String input = JOptionPane.showInputDialog(
            this, 
            "New price",
            currentPrice
        );

        if (input != null) {
            adminService.updatePrice(
                currentUser, 
                itemId, 
                new BigDecimal(input)
            );

            // refresh
            MenuCategory cat = categoryList.getSelectedValue();
            loadMenuItems(cat.getId());
        }
    }

    private void loadCategories() {
        List<MenuCategory> categories = menuService.getAllCategories();
        categoryList.setListData(categories.toArray(new MenuCategory[0]));
    }

    private void loadMenuItems(int categoryId) {
        List<MenuItem> items = menuService.getMenuItemsByCategory(categoryId);
        DefaultTableModel model = (DefaultTableModel) itemTable.getModel();
        model.setRowCount(0);

        for (MenuItem item: items) {
            model.addRow(new Object[] {
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getBasePrice()
            });
        }
    }
}
