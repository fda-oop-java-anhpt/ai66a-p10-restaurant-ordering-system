package com.oop.project.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import com.oop.project.model.CustomizationOption;
import com.oop.project.model.MenuCategory;
import com.oop.project.model.MenuItem;
import com.oop.project.model.OrderDraft;
import com.oop.project.model.OrderItem;
import com.oop.project.model.User;
import com.oop.project.service.MenuService;
import com.oop.project.service.OrderService;
import com.oop.project.ui.components.TonalCard;
import com.oop.project.ui.theme.AppTheme;
import com.oop.project.ui.theme.ThemeFonts;
import com.oop.project.ui.theme.ThemeHelper;
import com.oop.project.ui.theme.ThemeInsets;

public class OrdersPanel extends JPanel {

    private static final Icon CUSTOM_OPTION_OFF_ICON = new DotSelectionIcon(false);
    private static final Icon CUSTOM_OPTION_ON_ICON = new DotSelectionIcon(true);

    private final User currentUser;
    private final OrderService orderService = new OrderService();
    private final MenuService menuService = new MenuService();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");
    private OrderDraft currentDraft;
    private Runnable onUpdate;

    private final JComboBox<MenuItem> menuCombo = new JComboBox<>();
    private final JPanel customizationContainer = new JPanel();
    private final JScrollPane customizationScroll = new JScrollPane(customizationContainer);
    private final Map<Integer, CustomizationOption> customizationById = new LinkedHashMap<>();
    private final Map<Integer, AbstractButton> customizationButtons = new LinkedHashMap<>();
    private final JTextField quantityField = new JTextField("1");
    private final JButton decreaseQtyBtn = new JButton("-");
    private final JButton increaseQtyBtn = new JButton("+");
    private final JTextArea noteArea = new JTextArea(3, 20);
    private final JScrollPane noteScroll = new JScrollPane(noteArea);

    private final JButton newOrderBtn = new JButton("New Order");
    private final JButton addOrUpdateBtn = new JButton("Add Item to Order");
    private final JButton removeBtn = new JButton("Remove Selected");

    private final JTable orderTable = new JTable();
    private final DefaultTableModel orderModel = new DefaultTableModel(
        new Object[] {"No.", "Item", "Customizations", "Qty"},
        0
    );

    private final JTextField searchField = new JTextField();
    private final JPanel categoryTabsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_2, 0));
    private final JPanel menuCardsPanel = new JPanel();
    private final JLabel systemNoticeLabel = new JLabel("System ready. Select a menu item to begin.");

    private final List<MenuItem> allMenuItems = new ArrayList<>();
    private final List<MenuItem> visibleMenuItems = new ArrayList<>();
    private final Map<Integer, MenuCategory> categoriesById = new LinkedHashMap<>();
    private final ButtonGroup categoryGroup = new ButtonGroup();
    private final List<String> draftNotes = new ArrayList<>();

    private Integer activeCategoryId = null;
    private boolean applyingSelection = false;

    public OrdersPanel(User currentUser) {
        this(currentUser, new OrderService().createOrder(currentUser.getId()), null);
    }

    public OrdersPanel(User currentUser, OrderDraft sharedDraft, Runnable onUpdate) {
        this.currentUser = currentUser;
        this.currentDraft = sharedDraft;
        this.onUpdate = onUpdate;

        setLayout(new BorderLayout(AppTheme.SPACE_4, AppTheme.SPACE_4));
        setBorder(new EmptyBorder(ThemeInsets.section()));
        setBackground(AppTheme.BACKGROUND);

        configureMenuComboRenderer();
        configureQuantityField();
        configureSearchField();
        buildLayout();
        bindEvents();

        loadCategories();
        loadMenuItems();
        refreshTable();
        updatePreviewAndSubtotal();
    }

    public void refresh() {
        refreshTable();
        updatePreviewAndSubtotal();
        renderMenuCards();
    }

    public void reloadMenuItems() {
        MenuItem selected = (MenuItem) menuCombo.getSelectedItem();
        Integer selectedItemId = selected != null ? selected.getId() : null;

        loadCategories();
        loadMenuItems();

        if (selectedItemId != null) {
            selectMenuItemById(selectedItemId);
            loadCustomizationsForSelectedItem();
        }
        updatePreviewAndSubtotal();
        renderMenuCards();
    }

    private void buildLayout() {
        add(buildMenuOrderingArea(), BorderLayout.CENTER);
        add(buildActiveOrderRail(), BorderLayout.EAST);
    }

    private JPanel buildMenuOrderingArea() {
        TonalCard menuArea = new TonalCard(AppTheme.RADIUS_XL, AppTheme.SURFACE_CONTAINER_LOWEST);
        menuArea.setLayout(new BorderLayout(AppTheme.SPACE_3, AppTheme.SPACE_3));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("MENU ORDERING");
        title.setFont(ThemeFonts.titleLg());
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        categoryTabsPanel.setOpaque(false);
        categoryTabsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel searchLabel = new JLabel("Search menu item");
        searchLabel.setFont(ThemeFonts.labelMd());
        searchLabel.setForeground(AppTheme.TEXT_SECONDARY);

        searchField.setFont(ThemeFonts.bodyMd());
        searchField.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_2,
            AppTheme.SPACE_2,
            AppTheme.SPACE_2,
            AppTheme.SPACE_2
        ));

        TonalCard searchCard = new TonalCard(AppTheme.RADIUS_MD, AppTheme.SURFACE_CONTAINER_LOW);
        searchCard.setLayout(new BorderLayout());
        searchCard.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        searchCard.add(searchField, BorderLayout.CENTER);

        searchPanel.add(searchLabel, BorderLayout.NORTH);
        searchPanel.add(searchCard, BorderLayout.CENTER);

        header.add(title);
        header.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        header.add(categoryTabsPanel);
        header.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        header.add(searchPanel);

        menuCardsPanel.setOpaque(false);
        menuCardsPanel.setLayout(new BoxLayout(menuCardsPanel, BoxLayout.Y_AXIS));

        JScrollPane cardsScroll = new JScrollPane(menuCardsPanel);
        cardsScroll.setBorder(BorderFactory.createEmptyBorder());
        cardsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cardsScroll.getViewport().setBackground(AppTheme.SURFACE_CONTAINER_LOWEST);

        TonalCard noticeCard = new TonalCard(AppTheme.RADIUS_MD, AppTheme.SURFACE_CONTAINER_LOW);
        noticeCard.setLayout(new BorderLayout());
        noticeCard.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_2,
            AppTheme.SPACE_3,
            AppTheme.SPACE_2,
            AppTheme.SPACE_3
        ));

        systemNoticeLabel.setFont(ThemeFonts.labelMd());
        systemNoticeLabel.setForeground(AppTheme.TEXT_SECONDARY);
        noticeCard.add(systemNoticeLabel, BorderLayout.CENTER);

        menuArea.add(header, BorderLayout.NORTH);
        menuArea.add(cardsScroll, BorderLayout.CENTER);
        menuArea.add(noticeCard, BorderLayout.SOUTH);

        return menuArea;
    }

    private JPanel buildActiveOrderRail() {
        TonalCard rail = new TonalCard(AppTheme.RADIUS_XL, AppTheme.SURFACE_CONTAINER_LOW);
        rail.setLayout(new BorderLayout(AppTheme.SPACE_3, AppTheme.SPACE_3));
        rail.setPreferredSize(new Dimension(420, 0));

        JPanel railBody = new JPanel();
        railBody.setOpaque(false);
        railBody.setLayout(new BoxLayout(railBody, BoxLayout.Y_AXIS));

        JLabel railTitle = new JLabel("ACTIVE ORDER RAIL");
        railTitle.setFont(ThemeFonts.titleLg());
        railTitle.setForeground(AppTheme.TEXT_PRIMARY);
        railTitle.setHorizontalAlignment(SwingConstants.LEFT);
        railTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel menuLabel = new JLabel("Search menu item");
        menuLabel.setFont(ThemeFonts.labelMd());
        menuLabel.setForeground(AppTheme.TEXT_SECONDARY);
        menuLabel.setHorizontalAlignment(SwingConstants.LEFT);
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel customLabel = new JLabel("Customization options");
        customLabel.setFont(ThemeFonts.labelMd());
        customLabel.setForeground(AppTheme.TEXT_SECONDARY);
        customLabel.setHorizontalAlignment(SwingConstants.LEFT);
        customLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        customizationContainer.setLayout(new BoxLayout(customizationContainer, BoxLayout.Y_AXIS));
        customizationContainer.setOpaque(false);
        customizationContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        customizationScroll.setPreferredSize(new Dimension(0, 170));
        customizationScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel qtyLabel = new JLabel("Quantity");
        qtyLabel.setFont(ThemeFonts.labelMd());
        qtyLabel.setForeground(AppTheme.TEXT_SECONDARY);
        qtyLabel.setHorizontalAlignment(SwingConstants.LEFT);
        qtyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        quantityField.setColumns(5);
        quantityField.setHorizontalAlignment(SwingConstants.CENTER);
        quantityField.setPreferredSize(new Dimension(72, quantityField.getPreferredSize().height));

        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_2, 0));
        quantityPanel.setOpaque(false);
        quantityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel noteLabel = new JLabel("Additional note");
        noteLabel.setFont(ThemeFonts.labelMd());
        noteLabel.setForeground(AppTheme.TEXT_SECONDARY);
        noteLabel.setHorizontalAlignment(SwingConstants.LEFT);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        noteArea.setFont(ThemeFonts.bodyMd());
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_2,
            AppTheme.SPACE_2,
            AppTheme.SPACE_2,
            AppTheme.SPACE_2
        ));
        noteScroll.setPreferredSize(new Dimension(0, 72));
        noteScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        ThemeHelper.applyGhostButton(decreaseQtyBtn);
        ThemeHelper.applyGhostButton(increaseQtyBtn);

        quantityPanel.add(decreaseQtyBtn);
        quantityPanel.add(quantityField);
        quantityPanel.add(increaseQtyBtn);

        orderTable.setModel(orderModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ThemeHelper.applyTableStyle(orderTable);
        configureOrderRailTableColumns();
        orderTable.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane tableScroll = new JScrollPane(orderTable);
        tableScroll.setPreferredSize(new Dimension(0, 170));
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel actionButtons = new JPanel(new GridLayout(0, 2, AppTheme.SPACE_2, AppTheme.SPACE_2));
        actionButtons.setOpaque(false);
        actionButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        ThemeHelper.applyGhostButton(newOrderBtn);
        ThemeHelper.applyGhostButton(removeBtn);
        ThemeHelper.applyPrimaryButton(addOrUpdateBtn);

        actionButtons.add(newOrderBtn);
        actionButtons.add(removeBtn);

        JPanel primaryActionPanel = new JPanel(new BorderLayout());
        primaryActionPanel.setOpaque(false);
        primaryActionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        primaryActionPanel.add(addOrUpdateBtn, BorderLayout.CENTER);

        menuCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        railBody.add(railTitle);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        railBody.add(menuLabel);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        railBody.add(menuCombo);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        railBody.add(customLabel);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        railBody.add(customizationScroll);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        railBody.add(qtyLabel);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        railBody.add(quantityPanel);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        railBody.add(noteLabel);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        railBody.add(noteScroll);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        railBody.add(tableScroll);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        railBody.add(actionButtons);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        railBody.add(primaryActionPanel);

        rail.add(railBody, BorderLayout.CENTER);
        return rail;
    }

    private void configureOrderRailTableColumns() {
        orderTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {45, 180, 220, 55};
        for (int i = 0; i < widths.length && i < orderTable.getColumnModel().getColumnCount(); i++) {
            TableColumn column = orderTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(widths[i]);
        }
    }

    private void configureSearchField() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                renderMenuCards();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                renderMenuCards();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                renderMenuCards();
            }
        });
    }

    private void bindEvents() {
        menuCombo.addActionListener(e -> {
            if (applyingSelection) {
                return;
            }
            loadCustomizationsForSelectedItem();
            updatePreviewAndSubtotal();
            applyEditorToSelectedRowIfPossible();
            renderMenuCards();
        });

        quantityField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onQuantityChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onQuantityChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onQuantityChanged();
            }

            private void onQuantityChanged() {
                updatePreviewAndSubtotal();
                applyEditorToSelectedRowIfPossible();
            }
        });

        noteArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSelectedNoteIfPossible();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSelectedNoteIfPossible();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSelectedNoteIfPossible();
            }
        });

        newOrderBtn.addActionListener(e -> startNewOrder());
        addOrUpdateBtn.addActionListener(e -> addOrUpdateItem());
        removeBtn.addActionListener(e -> removeSelectedItem());
        decreaseQtyBtn.addActionListener(e -> decreaseQuantity());
        increaseQtyBtn.addActionListener(e -> increaseQuantity());

        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                syncEditorFromSelectedRow();
            }
        });
    }

    private void configureMenuComboRenderer() {
        menuCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MenuItem item) {
                    setText(item.getName());
                }
                return this;
            }
        });
    }

    private void configureQuantityField() {
        ((AbstractDocument) quantityField.getDocument()).setDocumentFilter(new PositiveQuantityFilter());
        quantityField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                quantityField.setText(String.valueOf(parseQuantityOrDefault()));
            }
        });

        InputMap inputMap = quantityField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = quantityField.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "increaseQty");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "decreaseQty");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "increaseQty");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "decreaseQty");
        inputMap.put(KeyStroke.getKeyStroke('+'), "increaseQty");
        inputMap.put(KeyStroke.getKeyStroke('-'), "decreaseQty");

        actionMap.put("increaseQty", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                increaseQuantity();
            }
        });
        actionMap.put("decreaseQty", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                decreaseQuantity();
            }
        });
    }

    private void loadCategories() {
        categoriesById.clear();
        for (MenuCategory category : menuService.getAllCategories()) {
            categoriesById.put(category.getId(), category);
        }
        buildCategoryTabs();
    }

    private void buildCategoryTabs() {
        categoryTabsPanel.removeAll();
        categoryGroup.clearSelection();

        JToggleButton allButton = createCategoryButton("ALL", null);
        allButton.setSelected(true);
        categoryTabsPanel.add(allButton);
        categoryGroup.add(allButton);

        for (MenuCategory category : categoriesById.values()) {
            JToggleButton button = createCategoryButton(category.getName().toUpperCase(), category.getId());
            categoryTabsPanel.add(button);
            categoryGroup.add(button);
        }

        categoryTabsPanel.revalidate();
        categoryTabsPanel.repaint();
    }

    private JToggleButton createCategoryButton(String text, Integer categoryId) {
        JToggleButton button = new JToggleButton(text);
        button.setFont(ThemeFonts.labelMd());
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_1,
            AppTheme.SPACE_2,
            AppTheme.SPACE_1,
            AppTheme.SPACE_2
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        applyCategoryButtonStyle(button, false);

        button.addActionListener(e -> {
            activeCategoryId = categoryId;
            updateCategoryButtonStyles();
            renderMenuCards();
        });

        return button;
    }

    private void updateCategoryButtonStyles() {
        for (Component component : categoryTabsPanel.getComponents()) {
            if (component instanceof JToggleButton toggleButton) {
                applyCategoryButtonStyle(toggleButton, toggleButton.isSelected());
            }
        }
    }

    private void applyCategoryButtonStyle(AbstractButton button, boolean selected) {
        if (selected) {
            button.setBackground(AppTheme.PRIMARY_CONTAINER);
            button.setForeground(AppTheme.ON_PRIMARY);
            return;
        }

        button.setBackground(AppTheme.SURFACE_CONTAINER);
        button.setForeground(AppTheme.TEXT_SECONDARY);
    }

    private void loadMenuItems() {
        allMenuItems.clear();
        allMenuItems.addAll(orderService.getAllMenuItems());

        menuCombo.removeAllItems();
        for (MenuItem item : allMenuItems) {
            menuCombo.addItem(item);
        }

        if (!allMenuItems.isEmpty()) {
            menuCombo.setSelectedIndex(0);
            loadCustomizationsForSelectedItem();
        }

        renderMenuCards();
    }

    private void renderMenuCards() {
        menuCardsPanel.removeAll();
        visibleMenuItems.clear();

        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        for (MenuItem item : allMenuItems) {
            if (activeCategoryId != null && item.getCategoryId() != activeCategoryId.intValue()) {
                continue;
            }
            if (!keyword.isEmpty() && !item.getName().toLowerCase().contains(keyword)) {
                continue;
            }
            visibleMenuItems.add(item);
        }

        visibleMenuItems.sort(Comparator.comparing(MenuItem::getName));

        if (visibleMenuItems.isEmpty()) {
            JLabel empty = new JLabel("No menu items match this filter.");
            empty.setFont(ThemeFonts.bodyMd());
            empty.setForeground(AppTheme.TEXT_SECONDARY);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            menuCardsPanel.add(empty);
        } else {
            for (MenuItem item : visibleMenuItems) {
                JPanel card = buildMenuCard(item);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                menuCardsPanel.add(card);
                menuCardsPanel.add(Box.createVerticalStrut(AppTheme.SPACE_2));
            }
        }

        menuCardsPanel.revalidate();
        menuCardsPanel.repaint();
    }

    private JPanel buildMenuCard(MenuItem item) {
        boolean selected = isSelectedMenuItem(item.getId());
        TonalCard card = new TonalCard(
            AppTheme.RADIUS_MD,
            selected ? AppTheme.SURFACE_CONTAINER_LOW : AppTheme.SURFACE_CONTAINER_LOWEST
        );
        card.setLayout(new BorderLayout(AppTheme.SPACE_2, AppTheme.SPACE_2));
        card.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_3,
            AppTheme.SPACE_3,
            AppTheme.SPACE_3,
            AppTheme.SPACE_3
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(item.getName());
        name.setFont(ThemeFonts.bodyLg());
        name.setForeground(AppTheme.TEXT_PRIMARY);

        String descriptionText = item.getDescription() == null || item.getDescription().isBlank()
            ? "No description"
            : item.getDescription();

        JLabel description = new JLabel(descriptionText);
        description.setFont(ThemeFonts.labelMd());
        description.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel price = new JLabel(formatCurrency(item.getBasePrice()));
        price.setFont(ThemeFonts.labelMd());
        price.setForeground(AppTheme.PRIMARY);

        info.add(name);
        info.add(description);
        info.add(price);

        JButton customizeBtn = new JButton("Customize");
        ThemeHelper.applyGhostButton(customizeBtn);
        customizeBtn.addActionListener(e -> selectMenuItemById(item.getId()));

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectMenuItemById(item.getId());
            }
        });

        card.add(info, BorderLayout.CENTER);
        card.add(customizeBtn, BorderLayout.EAST);
        return card;
    }

    private boolean isSelectedMenuItem(int menuItemId) {
        MenuItem selected = (MenuItem) menuCombo.getSelectedItem();
        return selected != null && selected.getId() == menuItemId;
    }

    private void loadCustomizationsForSelectedItem() {
        MenuItem selected = (MenuItem) menuCombo.getSelectedItem();
        if (selected == null) {
            buildCustomizationControls(List.of());
            return;
        }

        List<CustomizationOption> options = orderService.getCustomizationOptions(selected.getId());
        buildCustomizationControls(options);
    }

    private void startNewOrder() {
        currentDraft.clearItems();
        draftNotes.clear();
        orderModel.setRowCount(0);
        orderTable.clearSelection();
        addOrUpdateBtn.setText("Add Item to Order");
        quantityField.setText("1");
        noteArea.setText("");
        clearCustomizationSelection();
        updatePreviewAndSubtotal();
        showSystemNotice("Started a new order draft.");
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    private void addOrUpdateItem() {
        MenuItem selectedItem = (MenuItem) menuCombo.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select a menu item.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = OrderService.parseQuantity(quantityField.getText());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
                return;
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<CustomizationOption> selectedCustomizations = orderService.copyOfSelected(getSelectedCustomizations());
        int selectedRow = orderTable.getSelectedRow();

        if (selectedRow >= 0) {
            orderService.replaceItem(currentDraft, selectedRow, selectedItem, selectedCustomizations, quantity);
            updateNoteAt(selectedRow, noteArea.getText());
            showSystemNotice("Item updated in active order.");
        } else {
            orderService.addItem(currentDraft, selectedItem, selectedCustomizations, quantity);
            draftNotes.add(normalizeNote(noteArea.getText()));
            showSystemNotice("Item added to active order.");
            noteArea.setText("");
        }

        refreshTable();
        updatePreviewAndSubtotal();
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    private void removeSelectedItem() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        orderService.removeItem(currentDraft, selectedRow);
        if (selectedRow >= 0 && selectedRow < draftNotes.size()) {
            draftNotes.remove(selectedRow);
        }
        refreshTable();
        addOrUpdateBtn.setText("Add Item to Order");
        updatePreviewAndSubtotal();
        showSystemNotice("Item removed from active order.");
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    private void refreshTable() {
        int previousSelection = orderTable.getSelectedRow();

        orderModel.setRowCount(0);
        List<OrderItem> items = currentDraft.getItems();
        ensureNotesSize(items.size());
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            String customizationText = item.getCustomizationSummary();
            String note = draftNotes.get(i);
            if (!note.isBlank()) {
                customizationText = customizationText + " | Note: " + note;
            }

            orderModel.addRow(new Object[] {
                i + 1,
                item.getMenuItem().getName(),
                customizationText,
                item.getQuantity()
            });
        }

        if (previousSelection >= 0 && previousSelection < orderModel.getRowCount()) {
            orderTable.setRowSelectionInterval(previousSelection, previousSelection);
        }
    }

    private void syncEditorFromSelectedRow() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentDraft.getItems().size()) {
            addOrUpdateBtn.setText("Add Item to Order");
            return;
        }

        OrderItem selectedOrderItem = currentDraft.getItems().get(selectedRow);
        applyingSelection = true;
        try {
            selectMenuItemById(selectedOrderItem.getMenuItem().getId());
            loadCustomizationsForSelectedItem();
            selectCustomizations(selectedOrderItem.getCustomizations());
            quantityField.setText(String.valueOf(selectedOrderItem.getQuantity()));
            noteArea.setText(getNoteAt(selectedRow));
        } finally {
            applyingSelection = false;
        }

        addOrUpdateBtn.setText("Update Item");
        updatePreviewAndSubtotal();
    }

    private void applyEditorToSelectedRowIfPossible() {
        if (applyingSelection) {
            return;
        }

        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentDraft.getItems().size()) {
            return;
        }

        MenuItem selectedItem = (MenuItem) menuCombo.getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        int quantity;
        try {
            quantity = OrderService.parseQuantity(quantityField.getText());
            if (quantity <= 0) {
                return;
            }
        } catch (IllegalArgumentException ex) {
            return;
        }

        List<CustomizationOption> selectedCustomizations = new ArrayList<>(getSelectedCustomizations());
        orderService.replaceItem(currentDraft, selectedRow, selectedItem, selectedCustomizations, quantity);
        refreshTable();
        orderTable.setRowSelectionInterval(selectedRow, selectedRow);
        updatePreviewAndSubtotal();
    }

    private void updatePreviewAndSubtotal() {
        MenuItem item = (MenuItem) menuCombo.getSelectedItem();
        if (item == null) {
            return;
        }

        // Keep this method as an update hook for existing flow without showing price summary in rail.
    }

    private void increaseQuantity() {
        int qty = parseQuantityOrDefault();
        quantityField.setText(String.valueOf(qty + 1));
    }

    private void decreaseQuantity() {
        int qty = parseQuantityOrDefault();
        if (qty > 1) {
            quantityField.setText(String.valueOf(qty - 1));
        }
    }

    private int parseQuantityOrDefault() {
        try {
            int qty = OrderService.parseQuantity(quantityField.getText());
            return Math.max(1, qty);
        } catch (IllegalArgumentException ex) {
            return 1;
        }
    }

    private static class PositiveQuantityFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String replacement = text == null ? "" : text;
            String candidate = current.substring(0, offset) + replacement + current.substring(offset + length);

            if (candidate.matches("([1-9]\\d*)?")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            replace(fb, offset, length, "", null);
        }
    }

    private void selectMenuItemById(int menuItemId) {
        for (int i = 0; i < menuCombo.getItemCount(); i++) {
            MenuItem item = menuCombo.getItemAt(i);
            if (item.getId() == menuItemId) {
                menuCombo.setSelectedIndex(i);
                renderMenuCards();
                return;
            }
        }
    }

    private void selectCustomizations(List<CustomizationOption> selectedCustomizations) {
        clearCustomizationSelection();

        Set<Integer> selectedIds = new HashSet<>();
        for (CustomizationOption selected : selectedCustomizations) {
            selectedIds.add(selected.getId());
        }

        for (Map.Entry<Integer, AbstractButton> entry : customizationButtons.entrySet()) {
            if (selectedIds.contains(entry.getKey())) {
                entry.getValue().setSelected(true);
            }
        }
    }

    private void buildCustomizationControls(List<CustomizationOption> options) {
        customizationContainer.removeAll();
        customizationById.clear();
        customizationButtons.clear();

        if (options.isEmpty()) {
            JLabel emptyLabel = new JLabel("No customization available.");
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            customizationContainer.add(emptyLabel);
            customizationContainer.revalidate();
            customizationContainer.repaint();
            return;
        }

        List<CustomizationOption> sorted = new ArrayList<>(options);
        sorted.sort(Comparator.comparingInt(option -> groupPriority(classifyGroupTitle(option.getName()))));

        Map<String, List<CustomizationOption>> groupedOptions = new LinkedHashMap<>();
        for (CustomizationOption option : sorted) {
            String groupTitle = classifyGroupTitle(option.getName());
            groupedOptions.computeIfAbsent(groupTitle, key -> new ArrayList<>()).add(option);
        }

        ActionListener optionSelectionListener = e -> {
            if (applyingSelection) {
                return;
            }
            updatePreviewAndSubtotal();
            applyEditorToSelectedRowIfPossible();
        };

        for (Map.Entry<String, List<CustomizationOption>> entry : groupedOptions.entrySet()) {
            String groupTitle = entry.getKey();
            List<CustomizationOption> groupOptions = entry.getValue();

            JPanel groupPanel = new JPanel();
            groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
            groupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            groupPanel.setBorder(BorderFactory.createTitledBorder(groupTitle));
            groupPanel.setOpaque(false);

            for (CustomizationOption option : groupOptions) {
                AbstractButton optionButton = new JCheckBox(formatOptionLabel(option));

                optionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                optionButton.setOpaque(false);
                optionButton.setIcon(CUSTOM_OPTION_OFF_ICON);
                optionButton.setSelectedIcon(CUSTOM_OPTION_ON_ICON);
                optionButton.setIconTextGap(AppTheme.SPACE_2);
                optionButton.addActionListener(optionSelectionListener);

                customizationById.put(option.getId(), option);
                customizationButtons.put(option.getId(), optionButton);

                groupPanel.add(optionButton);
            }

            customizationContainer.add(groupPanel);
        }

        customizationContainer.revalidate();
        customizationContainer.repaint();
    }

    private int groupPriority(String groupTitle) {
        return switch (groupTitle) {
            case "Size" -> 0;
            case "Extra Proteins" -> 1;
            case "Sauce" -> 2;
            case "Extras" -> 3;
            case "Spice Level" -> 4;
            case "Removals" -> 5;
            default -> 6;
        };
    }

    private String classifyGroupTitle(String optionName) {
        String lower = optionName.toLowerCase();

        if (lower.contains("size:") || lower.matches(".*(small|medium|large|extra large|s|m|l|xl).*")) {
            return "Size";
        }
        if (lower.contains("sauce:") || lower.matches(".*(gravy|sauce|dressing).*")) {
            return "Sauce";
        }
        if (lower.contains("protein:") || lower.matches(".*(chicken|beef|pork|shrimp|tofu).*")) {
            return "Extra Proteins";
        }
        if (lower.contains("spic") || lower.matches(".*(mild|medium|hot|extra hot|spicy).*")) {
            return "Spice Level";
        }
        if (lower.startsWith("no ") || lower.startsWith("without ")) {
            return "Removals";
        }

        return "Extras";
    }

    private String formatOptionLabel(CustomizationOption option) {
        BigDecimal priceDelta = option.getPriceDelta();
        if (priceDelta.compareTo(BigDecimal.ZERO) == 0) {
            return option.getName();
        }
        String sign = priceDelta.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return option.getName() + " (" + sign + formatCurrency(priceDelta.abs()) + ")";
    }

    private String formatCurrency(BigDecimal amount) {
        return priceFormat.format(amount) + " VND";
    }

    private List<CustomizationOption> getSelectedCustomizations() {
        List<CustomizationOption> selected = new ArrayList<>();
        for (Map.Entry<Integer, AbstractButton> entry : customizationButtons.entrySet()) {
            if (entry.getValue().isSelected()) {
                CustomizationOption option = customizationById.get(entry.getKey());
                if (option != null) {
                    selected.add(option);
                }
            }
        }
        return selected;
    }

    private void clearCustomizationSelection() {
        for (AbstractButton button : customizationButtons.values()) {
            button.setSelected(false);
        }
    }

    private void updateSelectedNoteIfPossible() {
        if (applyingSelection) {
            return;
        }

        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentDraft.getItems().size()) {
            return;
        }

        updateNoteAt(selectedRow, noteArea.getText());
        refreshTable();
        if (selectedRow < orderModel.getRowCount()) {
            orderTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }

    private void ensureNotesSize(int targetSize) {
        while (draftNotes.size() < targetSize) {
            draftNotes.add("");
        }
        while (draftNotes.size() > targetSize) {
            draftNotes.remove(draftNotes.size() - 1);
        }
    }

    private String normalizeNote(String note) {
        return note == null ? "" : note.trim();
    }

    private void updateNoteAt(int index, String note) {
        ensureNotesSize(currentDraft.getItems().size());
        if (index >= 0 && index < draftNotes.size()) {
            draftNotes.set(index, normalizeNote(note));
        }
    }

    private String getNoteAt(int index) {
        ensureNotesSize(currentDraft.getItems().size());
        if (index < 0 || index >= draftNotes.size()) {
            return "";
        }
        return draftNotes.get(index);
    }

    private void showSystemNotice(String message) {
        systemNoticeLabel.setText(message);
    }

    private static final class DotSelectionIcon implements Icon {
        private final boolean selected;

        private DotSelectionIcon(boolean selected) {
            this.selected = selected;
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

            g2.setColor(AppTheme.OUTLINE_VARIANT);
            g2.drawOval(x + 1, y + 1, 11, 11);

            if (selected) {
                g2.setColor(AppTheme.SECONDARY);
                g2.fillOval(x + 4, y + 4, 6, 6);
            } else {
                g2.setColor(new Color(0, 0, 0, 0));
                g2.fillOval(x + 4, y + 4, 6, 6);
            }

            g2.dispose();
        }
    }
}
