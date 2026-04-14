package com.oop.project.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    private final JButton addFoodBtn = new JButton("Add");
    private final JButton editPriceBtn = new JButton("Edit Price");
    private final JTextField searchField = new JTextField();
    
    private int currentCategoryId = -1;
    private List<MenuItem> allMenuItems;

    public MenuPanel(User user) {
        this.currentUser = user;
        this.menuService = new MenuService();
        this.adminService = new MenuAdminService();

        setLayout(new BorderLayout(10, 10));

        initCategoryList();
        initItemTable();
        setupSearch();

        add(new JScrollPane(categoryList), BorderLayout.WEST);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);

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

    private void setupSearch() {
        searchField.setColumns(20);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(buildSearchPanel(), BorderLayout.NORTH);
        panel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        return panel;
    }

    private JPanel buildActionPanel() {
        addFoodBtn.addActionListener(e -> showAddFoodDialog());
        editPriceBtn.addActionListener(e -> editSelectedPrice());
        editPriceBtn.setEnabled(currentUser.isManager());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(addFoodBtn);
        panel.add(editPriceBtn);
        return panel;
    }

    private void applyFilter() {
        if (allMenuItems == null) return;
        
        String keyword = searchField.getText().toLowerCase().trim();
        DefaultTableModel model = (DefaultTableModel) itemTable.getModel();
        model.setRowCount(0);
        
        for (MenuItem item : allMenuItems) {
            if (item.getName().toLowerCase().contains(keyword) 
                || item.getDescription().toLowerCase().contains(keyword)) {
                model.addRow(new Object[] {
                    item.getId(),
                    item.getName(),
                    item.getDescription(),
                    item.getBasePrice()
                });
            }
        }
    }

    private void showAddFoodDialog() {
        List<MenuCategory> categories = menuService.getAllCategories();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No category available for new food.",
                "Add Food",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JTextField nameField = new JTextField();
        JTextField descriptionField = new JTextField();
        JTextField priceField = new JTextField();
        JComboBox<MenuCategory> categoryCombo = new JComboBox<>(categories.toArray(MenuCategory[]::new));

        MenuCategory selectedCategory = categoryList.getSelectedValue();
        if (selectedCategory != null) {
            for (MenuCategory category : categories) {
                if (category.getId() == selectedCategory.getId()) {
                    categoryCombo.setSelectedItem(category);
                    break;
                }
            }
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        formPanel.add(new JLabel("Food name"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Description"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel("Base price"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Category"));
        formPanel.add(categoryCombo);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Add Food",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            String priceText = priceField.getText().trim();
            MenuCategory category = (MenuCategory) categoryCombo.getSelectedItem();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Food name is required.");
                continue;
            }

            if (category == null) {
                JOptionPane.showMessageDialog(this, "Please select a category.");
                continue;
            }

            BigDecimal price;
            try {
                price = new BigDecimal(priceText);
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(this, "Base price must be greater than or equal to 0.");
                    continue;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Base price must be a valid number.");
                continue;
            }

            menuService.addFood(name, description, price, category.getId());
            selectCategory(category.getId());
            loadMenuItems(category.getId());
            return;
        }
    }

    private void editSelectedPrice() {
        int row = itemTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a food item first.");
            return;
        }

        int itemId = (int) itemTable.getValueAt(row, 0);
        String currentPrice = itemTable.getValueAt(row, 3).toString();

        String input = JOptionPane.showInputDialog(
            this, 
            "New price",
            currentPrice
        );

        if (input == null) {
            return;
        }

        try {
            adminService.updatePrice(
                currentUser,
                itemId,
                new BigDecimal(input.trim())
            );

            MenuCategory cat = categoryList.getSelectedValue();
            if (cat != null) {
                loadMenuItems(cat.getId());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Price", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCategories() {
        List<MenuCategory> categories = menuService.getAllCategories();
        categoryList.setListData(categories.toArray(MenuCategory[]::new));
    }

    private void loadMenuItems(int categoryId) {
        currentCategoryId = categoryId;
        allMenuItems = menuService.getMenuItemsByCategory(categoryId);
        searchField.setText("");
        applyFilter();
    }

    private void selectCategory(int categoryId) {
        ListModel<MenuCategory> model = categoryList.getModel();
        for (int index = 0; index < model.getSize(); index++) {
            MenuCategory category = model.getElementAt(index);
            if (category.getId() == categoryId) {
                categoryList.setSelectedIndex(index);
                return;
            }
        }
    }
}
