package com.oop.project.ui.panels;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.oop.project.model.MenuCategory;
import com.oop.project.model.MenuItem;
import com.oop.project.model.OrderDraft;
import com.oop.project.model.User;
import com.oop.project.service.MenuAdminService;
import com.oop.project.service.MenuService;
import com.oop.project.ui.theme.AppTheme;

public class MenuPanel extends JPanel {

    private static final int ALL_CATEGORY_ID = -1;
    private static final MenuCategory ALL_CATEGORY = new MenuCategory(ALL_CATEGORY_ID, "All Categories");
    private static final String SEARCH_PLACEHOLDER = "Search...";

    private final User currentUser;
    private final MenuService menuService;
    private final MenuAdminService adminService;
    private final Runnable onMenuChanged;
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");

    private int currentCategoryId = ALL_CATEGORY_ID;
    private List<MenuItem> allMenuItems;
    private MenuItem selectedItem;
    private boolean searchPromptActive = true;

    private final JList<MenuCategory> categoryList = new JList<>();
    private final JTextField searchField = new JTextField();
    private final JPanel itemGridPanel = new JPanel();

    private final JButton addFoodBtn = new JButton("Add");
    private final JButton editFoodBtn = new JButton("Edit");
    private final JButton deleteFoodBtn = new JButton("Delete");

    private JPanel southPanel;

    public MenuPanel(User user, Runnable onMenuChanged) {
        this(user, null, onMenuChanged, null);
    }

    public MenuPanel(User user, OrderDraft draft, Runnable onMenuChanged, Runnable onItemAdded) {
        this.currentUser = user;
        this.menuService = new MenuService();
        this.adminService = new MenuAdminService();
        this.onMenuChanged = onMenuChanged;

        setLayout(new BorderLayout(AppTheme.SPACE_4, AppTheme.SPACE_4));
        setBackground(AppTheme.SURFACE);
        setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_4,
            AppTheme.SPACE_4,
            AppTheme.SPACE_4,
            AppTheme.SPACE_4
        ));

        initCategoryList();
        setupSearch();
        bindActions();

        add(buildBody(), BorderLayout.CENTER);
        loadCategories();
    }

    private boolean isManagerMode() {
        return currentUser != null && currentUser.isManager();
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(AppTheme.SPACE_4, AppTheme.SPACE_4));
        body.setOpaque(false);
        body.add(buildCategoryCard(), BorderLayout.WEST);
        body.add(buildCenterCard(), BorderLayout.CENTER);
        return body;
    }

    private JPanel buildCategoryCard() {
        TonalCardPanel card = new TonalCardPanel(AppTheme.SURFACE_CONTAINER, AppTheme.RADIUS_XL);
        card.setLayout(new BorderLayout(AppTheme.SPACE_3, AppTheme.SPACE_3));
        card.setBorderPadding(AppTheme.SPACE_4, AppTheme.SPACE_4, AppTheme.SPACE_4, AppTheme.SPACE_4);
        card.setPreferredSize(new Dimension(180, 0));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Categories");
        title.setForeground(AppTheme.ON_BACKGROUND);
        title.setFont(headlineFont(18f));

        JLabel subtitle = new JLabel();
        subtitle.setForeground(AppTheme.ON_SURFACE_VARIANT);
        subtitle.setFont(bodyFont(12f));

        styleTextField(searchField);
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        searchField.setPreferredSize(new Dimension(155, 38));
        searchField.setHorizontalAlignment(SwingConstants.LEFT);
        searchField.setToolTipText("Search by item name or description");

        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(subtitle);
        top.add(Box.createVerticalStrut(10));
        top.add(searchField);

        JScrollPane scrollPane = new JScrollPane(categoryList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(AppTheme.SURFACE_CONTAINER);

        card.add(top, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildCenterCard() {
        TonalCardPanel centerCard = new TonalCardPanel(AppTheme.SURFACE_CONTAINER_LOWEST, AppTheme.RADIUS_XL);
        centerCard.setLayout(new BorderLayout(AppTheme.SPACE_3, AppTheme.SPACE_3));
        centerCard.setBorderPadding(
            AppTheme.SPACE_4,
            AppTheme.SPACE_4,
            AppTheme.SPACE_4,
            AppTheme.SPACE_4
        );

        centerCard.add(buildHeaderSection(), BorderLayout.NORTH);
        centerCard.add(buildGridSection(), BorderLayout.CENTER);

        southPanel = new JPanel();
        southPanel.setOpaque(false);
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        rebuildSouthPanel();

        centerCard.add(southPanel, BorderLayout.SOUTH);
        return centerCard;
    }

    private JPanel buildHeaderSection() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Menu Items");
        title.setForeground(AppTheme.ON_BACKGROUND);
        title.setFont(headlineFont(18f));

        JLabel subtitle = new JLabel(
            isManagerMode()
                ? "Select a card to edit or delete"
                : "Browse menu only. Add and customize items in Orders"
        );
        subtitle.setForeground(AppTheme.ON_SURFACE_VARIANT);
        subtitle.setFont(bodyFont(12f));

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);

        wrap.add(left, BorderLayout.WEST);
        return wrap;
    }

    private JPanel buildGridSection() {
        itemGridPanel.setOpaque(false);
        itemGridPanel.setLayout(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(itemGridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(AppTheme.SURFACE_CONTAINER_LOWEST);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void initCategoryList() {
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setBackground(AppTheme.SURFACE_CONTAINER);
        categoryList.setForeground(AppTheme.ON_BACKGROUND);
        categoryList.setFixedCellHeight(38);

        categoryList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof MenuCategory category) {
                    setText(category.getName());
                }

                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                setFont(bodyFont(13f));

                if (isSelected) {
                    setBackground(AppTheme.SECONDARY);
                    setForeground(AppTheme.ON_SECONDARY);
                } else {
                    setBackground(AppTheme.SURFACE_CONTAINER);
                    setForeground(AppTheme.ON_BACKGROUND);
                }
                return this;
            }
        });

        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                MenuCategory selected = categoryList.getSelectedValue();
                if (selected != null) {
                    loadMenuItems(selected.getId());
                }
            }
        });
    }

    private void setupSearch() {
        setSearchPrompt();

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchPromptActive) {
                    searchField.setText("");
                    searchField.setForeground(AppTheme.ON_BACKGROUND);
                    searchPromptActive = false;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    setSearchPrompt();
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!searchPromptActive) {
                    applyFilter();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!searchPromptActive) {
                    applyFilter();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!searchPromptActive) {
                    applyFilter();
                }
            }
        });
    }

    private void setSearchPrompt() {
        searchPromptActive = true;
        searchField.setForeground(new Color(150, 150, 150));
        searchField.setText(SEARCH_PLACEHOLDER);
    }

    private void bindActions() {
        addFoodBtn.addActionListener(e -> showAddFoodDialog());
        editFoodBtn.addActionListener(e -> editSelectedFood());
        deleteFoodBtn.addActionListener(e -> deleteSelectedFood());
    }

    private void loadCategories() {
        List<MenuCategory> categories = menuService.getAllCategories();
        categories.add(0, ALL_CATEGORY);
        categoryList.setListData(categories.toArray(MenuCategory[]::new));
        categoryList.setSelectedIndex(0);
    }

    private void loadMenuItems(int categoryId) {
        currentCategoryId = categoryId;
        allMenuItems = categoryId == ALL_CATEGORY_ID
            ? menuService.getAllMenuItems()
            : menuService.getMenuItemsByCategory(categoryId);

        setSearchPrompt();

        if (selectedItem != null) {
            boolean stillExists = false;
            for (MenuItem item : allMenuItems) {
                if (item.getId() == selectedItem.getId()) {
                    selectedItem = item;
                    stillExists = true;
                    break;
                }
            }
            if (!stillExists) {
                selectedItem = null;
            }
        }

        applyFilter();
    }

    private void applyFilter() {
        if (allMenuItems == null) {
            return;
        }

        String keyword = searchPromptActive ? "" : searchField.getText().trim().toLowerCase();

        itemGridPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;

        int col = 0;
        int row = 0;
        int columns = 3;

        for (MenuItem item : allMenuItems) {
            String name = item.getName() == null ? "" : item.getName().toLowerCase();
            String description = item.getDescription() == null ? "" : item.getDescription().toLowerCase();

            boolean matchName = keyword.isEmpty() || name.contains(keyword);
            boolean matchDescription = keyword.length() > 1 && description.contains(keyword);

            if (matchName || matchDescription) {
                gbc.gridx = col;
                gbc.gridy = row;

                JPanel holder = new JPanel(new BorderLayout());
                holder.setOpaque(false);
                holder.setPreferredSize(new Dimension(210, 235));
                holder.add(createItemCard(item), BorderLayout.CENTER);

                itemGridPanel.add(holder, gbc);

                col++;
                if (col == columns) {
                    col = 0;
                    row++;
                }
            }
        }

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = row + 1;
        filler.gridwidth = columns;
        filler.weightx = 1.0;
        filler.weighty = 1.0;
        filler.fill = GridBagConstraints.BOTH;

        itemGridPanel.add(Box.createGlue(), filler);

        itemGridPanel.revalidate();
        itemGridPanel.repaint();
        rebuildSouthPanel();
    }

    private JPanel createItemCard(MenuItem item) {
        boolean isSelected = isManagerMode()
            && selectedItem != null
            && selectedItem.getId() == item.getId();

        Color cardColor = isSelected ? AppTheme.SECONDARY : AppTheme.SURFACE_CONTAINER;
        Color textColor = isSelected ? AppTheme.ON_SECONDARY : AppTheme.ON_BACKGROUND;
        Color subTextColor = isSelected ? AppTheme.ON_SECONDARY : AppTheme.ON_SURFACE_VARIANT;

        TonalCardPanel card = new TonalCardPanel(cardColor, AppTheme.RADIUS_XL);
        card.setLayout(new BorderLayout(AppTheme.SPACE_2, AppTheme.SPACE_2));
        card.setBorderPadding(12, 12, 12, 12);
        card.setPreferredSize(new Dimension(210, 255));
        card.setMinimumSize(new Dimension(210, 255));
        card.setMaximumSize(new Dimension(210, 255));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel imageLabel = new JLabel(loadMenuItemImage(item, 185, 125));
        imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel("<html><div style='width:118px;'>" + escapeHtml(item.getName()) + "</div></html>");
        name.setForeground(textColor);
        name.setFont(headlineFont(13f));

        JLabel price = new JLabel(formatCurrency(item.getBasePrice()));
        price.setForeground(textColor);
        price.setFont(headlineFont(13f));
        price.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel titleRow = new JPanel();
        titleRow.setOpaque(false);
        titleRow.setLayout(new BorderLayout(8, 0));
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleRow.add(name, BorderLayout.CENTER);
        titleRow.add(price, BorderLayout.EAST);

        String description = item.getDescription() == null ? "No description available." : item.getDescription();
        JLabel desc = new JLabel("<html><div style='width:185px;'>" + escapeHtml(description) + "</div></html>");
        desc.setForeground(subTextColor);
        desc.setFont(bodyFont(11f));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(imageLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(titleRow);
        content.add(Box.createVerticalStrut(6));
        content.add(desc);

        if (isManagerMode()) {
            card.add(content, BorderLayout.CENTER);

            MouseAdapter selectListener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedItem = item;
                    applyFilter();
                }
            };

            card.addMouseListener(selectListener);
            content.addMouseListener(selectListener);
            imageLabel.addMouseListener(selectListener);
            titleRow.addMouseListener(selectListener);
            name.addMouseListener(selectListener);
            price.addMouseListener(selectListener);
            desc.addMouseListener(selectListener);
        } else {
            JPanel bottom = new JPanel();
            bottom.setOpaque(false);
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
            bottom.add(Box.createVerticalStrut(6));

            JLabel hint = new JLabel("Customize in Orders tab");
            hint.setForeground(subTextColor);
            hint.setFont(bodyFont(11f));
            hint.setAlignmentX(Component.CENTER_ALIGNMENT);
            bottom.add(hint);

            card.add(content, BorderLayout.CENTER);
            card.add(bottom, BorderLayout.SOUTH);
        }

        return card;
    }

    private void rebuildSouthPanel() {
        if (southPanel == null) {
            return;
        }

        southPanel.removeAll();
        southPanel.add(buildInfoStrip());
        southPanel.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        southPanel.add(buildActionSection());
        southPanel.revalidate();
        southPanel.repaint();
    }

    private JPanel buildInfoStrip() {
        JPanel strip = new JPanel(new java.awt.GridLayout(1, 3, AppTheme.SPACE_2, 0));
        strip.setOpaque(false);

        int filteredCount = 0;
        for (Component c : itemGridPanel.getComponents()) {
            if (!(c instanceof Box.Filler)) {
                filteredCount++;
            }
        }

        String rawText = searchField.getText().trim();

        String middleText;
        if (currentCategoryId == ALL_CATEGORY_ID) {
            middleText = rawText.isEmpty() || SEARCH_PLACEHOLDER.equals(rawText)
                ? "All categories"
                : "Search: " + rawText;
        } else {
            MenuCategory selectedCategory = categoryList.getSelectedValue();
            String categoryName = selectedCategory == null ? "Category selected" : selectedCategory.getName();
            middleText = rawText.isEmpty() || SEARCH_PLACEHOLDER.equals(rawText)
                ? categoryName
                : categoryName + " • " + rawText;
        }

        String rightText;
        if (isManagerMode()) {
            rightText = selectedItem == null ? "No item selected" : selectedItem.getName();
        } else {
            rightText = "Use Orders tab to add and customize";
        }

        strip.add(createBadge("Filtered " + filteredCount, AppTheme.SURFACE_CONTAINER, AppTheme.ON_BACKGROUND));
        strip.add(createBadge(middleText, AppTheme.SURFACE_CONTAINER, AppTheme.ON_BACKGROUND));
        strip.add(createBadge(rightText, AppTheme.SURFACE_CONTAINER_LOW, AppTheme.ON_SURFACE_VARIANT));

        return strip;
    }

    private JPanel buildActionSection() {
        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, AppTheme.SPACE_2, 0));
        panel.setOpaque(false);

        if (isManagerMode()) {
            styleGhostButton(editFoodBtn);
            styleDangerButton(deleteFoodBtn);
            stylePrimaryButton(addFoodBtn);

            editFoodBtn.setEnabled(selectedItem != null);
            deleteFoodBtn.setEnabled(selectedItem != null);

            panel.add(editFoodBtn);
            panel.add(deleteFoodBtn);
            panel.add(addFoodBtn);
        }

        return panel;
    }

    private void showAddFoodDialog() {
        if (!isManagerMode()) {
            return;
        }

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
        if (selectedCategory != null && selectedCategory.getId() != ALL_CATEGORY_ID) {
            for (MenuCategory category : categories) {
                if (category.getId() == selectedCategory.getId()) {
                    categoryCombo.setSelectedItem(category);
                    break;
                }
            }
        }

        JPanel formPanel = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
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

            try {
                adminService.addFood(currentUser, name, description, price, category.getId());

                selectedItem = null;
                loadMenuItems(currentCategoryId == ALL_CATEGORY_ID ? ALL_CATEGORY_ID : category.getId());

                if (onMenuChanged != null) {
                    onMenuChanged.run();
                }
                return;
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Add Food", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelectedFood() {
        if (!isManagerMode() || selectedItem == null) {
            return;
        }

        List<MenuCategory> categories = menuService.getAllCategories();
        if (categories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No category available.");
            return;
        }

        JTextField nameField = new JTextField(selectedItem.getName());
        JTextField descriptionField = new JTextField(selectedItem.getDescription() == null ? "" : selectedItem.getDescription());
        JTextField priceField = new JTextField(
            selectedItem.getBasePrice() == null ? "0" : selectedItem.getBasePrice().toPlainString()
        );
        JComboBox<MenuCategory> categoryCombo = new JComboBox<>(categories.toArray(MenuCategory[]::new));

        for (MenuCategory category : categories) {
            if (category.getId() == selectedItem.getCategoryId()) {
                categoryCombo.setSelectedItem(category);
                break;
            }
        }

        JPanel formPanel = new JPanel(new java.awt.GridLayout(0, 1, 0, 8));
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
                "Edit Food",
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

            try {
                int selectedId = selectedItem.getId();
                adminService.updateFood(currentUser, selectedId, name, description, price, category.getId());

                loadMenuItems(currentCategoryId);

                for (MenuItem item : allMenuItems) {
                    if (item.getId() == selectedId) {
                        selectedItem = item;
                        break;
                    }
                }

                applyFilter();

                if (onMenuChanged != null) {
                    onMenuChanged.run();
                }

                JOptionPane.showMessageDialog(this, "Updated successfully!");
                return;
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Edit Food", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedFood() {
        if (!isManagerMode() || selectedItem == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete selected item: " + selectedItem.getName() + "?",
            "Delete Food",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            adminService.deleteFood(currentUser, selectedItem.getId());
            selectedItem = null;
            loadMenuItems(currentCategoryId);

            if (onMenuChanged != null) {
                onMenuChanged.run();
            }

            JOptionPane.showMessageDialog(this, "Food item deleted successfully.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Delete Food",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

    private JPanel createBadge(String text, Color bg, Color fg) {
        TonalCardPanel badge = new TonalCardPanel(bg, 20);
        badge.setLayout(new BorderLayout());
        badge.setBorderPadding(8, 12, 8, 12);

        JLabel label = new JLabel(text);
        label.setForeground(fg);
        label.setFont(headlineFont(12f));
        badge.add(label, BorderLayout.CENTER);

        return badge;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(AppTheme.SURFACE_CONTAINER_LOWEST);
        field.setForeground(AppTheme.ON_BACKGROUND);
        field.setCaretColor(AppTheme.ON_BACKGROUND);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setFont(bodyFont(13f));
    }

    private void stylePrimaryButton(JButton button) {
        button.setFocusable(false);
        button.setBackground(AppTheme.SECONDARY);
        button.setForeground(AppTheme.ON_SECONDARY);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        button.setFont(headlineFont(12f));
    }

    private void styleDangerButton(JButton button) {
        button.setFocusable(false);
        button.setBackground(AppTheme.ERROR);
        button.setForeground(AppTheme.ON_PRIMARY);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        button.setFont(headlineFont(12f));
    }

    private void styleGhostButton(JButton button) {
        button.setFocusable(false);
        button.setBackground(AppTheme.SURFACE_CONTAINER_LOWEST);
        button.setForeground(AppTheme.ON_BACKGROUND);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        button.setFont(headlineFont(12f));
    }

    private Font headlineFont(float size) {
        return new Font("Segoe UI", Font.BOLD, Math.round(size));
    }

    private Font bodyFont(float size) {
        return new Font("Segoe UI", Font.PLAIN, Math.round(size));
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 VND";
        }
        return priceFormat.format(amount) + " VND";
    }

    private ImageIcon loadMenuItemImage(MenuItem item, int w, int h) {
        if (item == null || item.getName() == null || item.getName().trim().isEmpty()) {
            return createPlaceholder(w, h);
        }

        String baseName = item.getName()
            .trim()
            .toLowerCase()
            .replaceAll("\\s+", "_");

        String[] candidatePaths = {
            "images/" + baseName + ".jpg",
            "images/" + baseName + ".png",
            "images/" + baseName + ".jpeg"
        };

        for (String path : candidatePaths) {
            java.io.File file = new java.io.File(path);
            System.out.println("Checking image: " + file.getAbsolutePath());

            if (file.exists() && file.isFile()) {
                try {
                    ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                    if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                        return new ImageIcon(img);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Image not found for item: " + item.getName());
        return createPlaceholder(w, h);
    }

    private ImageIcon createPlaceholder(int w, int h) {
        java.awt.image.BufferedImage img =
            new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);

        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(220, 220, 220));
        g.fillRoundRect(0, 0, w, h, 20, 20);

        g.setColor(new Color(160, 160, 160));
        g.setFont(new Font("Segoe UI", Font.BOLD, 14));

        String text = "No Image";
        java.awt.FontMetrics fm = g.getFontMetrics();
        int textX = (w - fm.stringWidth(text)) / 2;
        int textY = (h + fm.getAscent()) / 2 - 4;

        g.drawString(text, textX, textY);
        g.dispose();

        return new ImageIcon(img);
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
}