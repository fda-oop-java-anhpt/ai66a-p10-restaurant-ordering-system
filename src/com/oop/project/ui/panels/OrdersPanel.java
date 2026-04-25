package com.oop.project.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

    private final JButton newOrderBtn = new JButton("NEW ORDER");
    private final JButton addOrUpdateBtn = new JButton("ADD ITEM TO ORDER");
    private final JButton removeBtn = new JButton("REMOVE SELECTED");

    private final JTable orderTable = new JTable();
    private final DefaultTableModel orderModel = new DefaultTableModel(
        new Object[] {"No.", "Item", "Customizations", "Qty"},
        0
    );

    private final JTextField searchField = new JTextField();
    private final JPanel categoryTabsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_2, 0));
    private final JPanel menuCardsPanel = new JPanel();
    private final JLabel systemNoticeLabel = new JLabel("System ready. Select a menu item to begin.");

    // Pricing labels for the order rail
    private final JLabel unitPriceValueLabel = new JLabel("$0.00");
    private final JLabel lineTotalValueLabel = new JLabel("$0.00");
    private final JLabel subtotalValueLabel = new JLabel("$0.00");

    private final List<MenuItem> allMenuItems = new ArrayList<>();
    private final List<MenuItem> visibleMenuItems = new ArrayList<>();
    private final Map<Integer, MenuCategory> categoriesById = new LinkedHashMap<>();
    private final ButtonGroup categoryGroup = new ButtonGroup();
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

        // Separator line under title
        JPanel titleSeparator = new JPanel();
        titleSeparator.setOpaque(false);
        titleSeparator.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        titleSeparator.setPreferredSize(new Dimension(0, 1));
        titleSeparator.setBackground(AppTheme.OUTLINE);
        titleSeparator.setOpaque(true);

        categoryTabsPanel.setOpaque(false);
        categoryTabsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel searchBar = new JPanel(new BorderLayout(AppTheme.SPACE_2, 0));
        searchBar.setOpaque(false);
        searchBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel searchLabel = new JLabel("Search menu:");
        searchLabel.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        searchLabel.setForeground(AppTheme.TEXT_SECONDARY);

        searchField.setFont(ThemeFonts.bodyMd());
        searchField.setToolTipText("Type menu item name to filter quickly");
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.OUTLINE, 1),
            BorderFactory.createEmptyBorder(AppTheme.SPACE_1, AppTheme.SPACE_2, AppTheme.SPACE_1, AppTheme.SPACE_2)
        ));

        searchBar.add(searchLabel, BorderLayout.WEST);
        searchBar.add(searchField, BorderLayout.CENTER);

        header.add(title);
        header.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        header.add(titleSeparator);
        header.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        header.add(categoryTabsPanel);
        header.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        header.add(searchBar);

        menuCardsPanel.setOpaque(false);
        menuCardsPanel.setLayout(new BoxLayout(menuCardsPanel, BoxLayout.Y_AXIS));

        JScrollPane cardsScroll = new JScrollPane(menuCardsPanel);
        cardsScroll.setBorder(BorderFactory.createEmptyBorder());
        cardsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cardsScroll.getViewport().setBackground(AppTheme.SURFACE_CONTAINER_LOWEST);

        // System notice bar with info icon
        JPanel noticePanel = new JPanel(new BorderLayout(AppTheme.SPACE_2, 0));
        noticePanel.setBackground(new Color(0xE8F0FE));
        noticePanel.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_2, AppTheme.SPACE_3, AppTheme.SPACE_2, AppTheme.SPACE_3));

        JLabel infoIcon = new JLabel("\u2139") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x1565C0));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont().deriveFont(Font.BOLD, 10f));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String txt = "i";
                int tx = (getWidth() - fm.stringWidth(txt)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(txt, tx, ty);
                g2.dispose();
            }
        };
        infoIcon.setPreferredSize(new Dimension(16, 16));

        JPanel noticeTextPanel = new JPanel();
        noticeTextPanel.setOpaque(false);
        noticeTextPanel.setLayout(new BoxLayout(noticeTextPanel, BoxLayout.Y_AXIS));

        JLabel noticeTitleLbl = new JLabel("SYSTEM NOTICE");
        noticeTitleLbl.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        noticeTitleLbl.setForeground(new Color(0x1A237E));

        systemNoticeLabel.setFont(ThemeFonts.labelMd());
        systemNoticeLabel.setForeground(AppTheme.TEXT_SECONDARY);

        noticeTextPanel.add(noticeTitleLbl);
        noticeTextPanel.add(systemNoticeLabel);

        noticePanel.add(infoIcon, BorderLayout.WEST);
        noticePanel.add(noticeTextPanel, BorderLayout.CENTER);

        menuArea.add(header, BorderLayout.NORTH);
        menuArea.add(cardsScroll, BorderLayout.CENTER);
        menuArea.add(noticePanel, BorderLayout.SOUTH);

        return menuArea;
    }

    private JPanel buildActiveOrderRail() {
        TonalCard rail = new TonalCard(AppTheme.RADIUS_XL, AppTheme.SURFACE_CONTAINER_LOW);
        rail.setLayout(new BorderLayout(0, 0));
        rail.setPreferredSize(new Dimension(340, 0));
        rail.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_4, AppTheme.SPACE_4, 0, AppTheme.SPACE_4));

        // ── Rail body (scrollable) ──────────────────────────────────────────────
        JPanel railBody = new JPanel();
        railBody.setOpaque(false);
        railBody.setLayout(new BoxLayout(railBody, BoxLayout.Y_AXIS));

        // Title
        JLabel railTitle = new JLabel("ACTIVE ORDER RAIL");
        railTitle.setFont(ThemeFonts.titleLg());
        railTitle.setForeground(AppTheme.TEXT_PRIMARY);
        railTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Search menu item (combo box)
        JLabel menuLabel = new JLabel("SELECT MENU ITEM");
        menuLabel.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        menuLabel.setForeground(AppTheme.TEXT_SECONDARY);
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        menuCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, menuCombo.getPreferredSize().height));

        // Customization options section
        TonalCard customCard = new TonalCard(AppTheme.RADIUS_MD, AppTheme.SURFACE_CONTAINER);
        customCard.setLayout(new BoxLayout(customCard, BoxLayout.Y_AXIS));
        customCard.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_3, AppTheme.SPACE_3, AppTheme.SPACE_3, AppTheme.SPACE_3));
        customCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel customSectionLabel = new JLabel("CUSTOMIZATION OPTIONS");
        customSectionLabel.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        customSectionLabel.setForeground(AppTheme.TEXT_SECONDARY);
        customSectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        customizationContainer.setLayout(new BoxLayout(customizationContainer, BoxLayout.Y_AXIS));
        customizationContainer.setOpaque(false);
        customizationContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        customizationScroll.setPreferredSize(new Dimension(0, 160));
        customizationScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        customizationScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        customizationScroll.setBorder(BorderFactory.createEmptyBorder());
        customizationScroll.getViewport().setOpaque(false);
        customizationScroll.setOpaque(false);

        customCard.add(customSectionLabel);
        customCard.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        customCard.add(customizationScroll);

        // Quantity row
        JLabel qtyLabel = new JLabel("QUANTITY");
        qtyLabel.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        qtyLabel.setForeground(AppTheme.TEXT_SECONDARY);

        styleQtyButton(decreaseQtyBtn);
        styleQtyButton(increaseQtyBtn);

        quantityField.setColumns(3);
        quantityField.setHorizontalAlignment(SwingConstants.CENTER);
        quantityField.setFont(ThemeFonts.bodyMd());
        quantityField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.OUTLINE, 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        quantityField.setPreferredSize(new Dimension(44, 28));
        quantityField.setMaximumSize(new Dimension(44, 28));

        JPanel quantityRow = new JPanel(new BorderLayout());
        quantityRow.setOpaque(false);
        quantityRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        quantityRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JPanel qtyControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACE_1, 0));
        qtyControls.setOpaque(false);
        qtyControls.add(decreaseQtyBtn);
        qtyControls.add(quantityField);
        qtyControls.add(increaseQtyBtn);

        quantityRow.add(qtyLabel, BorderLayout.WEST);
        quantityRow.add(qtyControls, BorderLayout.EAST);

        JLabel noteLabel = new JLabel("NOTE");
        noteLabel.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        noteLabel.setForeground(AppTheme.TEXT_SECONDARY);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Pricing section
        JPanel pricingPanel = new JPanel();
        pricingPanel.setOpaque(false);
        pricingPanel.setLayout(new BoxLayout(pricingPanel, BoxLayout.Y_AXIS));
        pricingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        pricingPanel.add(buildPriceRow("UNIT PRICE", unitPriceValueLabel, false));
        pricingPanel.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        pricingPanel.add(buildPriceRow("LINE TOTAL", lineTotalValueLabel, false));
        pricingPanel.add(Box.createVerticalStrut(AppTheme.SPACE_2));

        JSeparator priceSep = new JSeparator();
        priceSep.setAlignmentX(Component.LEFT_ALIGNMENT);
        priceSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        priceSep.setForeground(AppTheme.OUTLINE);
        pricingPanel.add(priceSep);
        pricingPanel.add(Box.createVerticalStrut(AppTheme.SPACE_2));

        subtotalValueLabel.setFont(ThemeFonts.bodyLg().deriveFont(Font.BOLD));
        subtotalValueLabel.setForeground(new Color(0x1565C0));
        pricingPanel.add(buildPriceRow("SUBTOTAL", subtotalValueLabel, true));

        // Style pricing labels
        unitPriceValueLabel.setFont(ThemeFonts.bodyMd());
        unitPriceValueLabel.setForeground(AppTheme.TEXT_PRIMARY);
        lineTotalValueLabel.setFont(ThemeFonts.bodyMd());
        lineTotalValueLabel.setForeground(AppTheme.TEXT_PRIMARY);

        // Hidden components still needed for logic
        noteArea.setFont(ThemeFonts.bodyMd());
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_1, AppTheme.SPACE_1, AppTheme.SPACE_1, AppTheme.SPACE_1));

        noteScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        noteScroll.setPreferredSize(new Dimension(0, 68));
        noteScroll.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE, 1));

        JLabel activeOrderLabel = new JLabel("ACTIVE ORDER ITEMS");
        activeOrderLabel.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        activeOrderLabel.setForeground(AppTheme.TEXT_SECONDARY);
        activeOrderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        orderTable.setModel(orderModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ThemeHelper.applyTableStyle(orderTable);
        configureOrderRailTableColumns();

        JScrollPane orderTableScroll = new JScrollPane(orderTable);
        orderTableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        orderTableScroll.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE, 1));
        orderTableScroll.setPreferredSize(new Dimension(0, 150));
        orderTableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Build rail body
        railBody.add(railTitle);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_4));
        railBody.add(menuLabel);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        railBody.add(menuCombo);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        railBody.add(customCard);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        railBody.add(quantityRow);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        railBody.add(noteLabel);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        railBody.add(noteScroll);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_4));
        railBody.add(pricingPanel);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_3));
        railBody.add(activeOrderLabel);
        railBody.add(Box.createVerticalStrut(AppTheme.SPACE_1));
        railBody.add(orderTableScroll);
        railBody.add(Box.createVerticalGlue());

        // ── Bottom action bar ──────────────────────────────────────────────────
        JPanel bottomBar = new JPanel();
        bottomBar.setOpaque(false);
        bottomBar.setLayout(new BoxLayout(bottomBar, BoxLayout.Y_AXIS));
        bottomBar.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.SPACE_3, 0, AppTheme.SPACE_4, 0));

        ThemeHelper.applyGhostButton(newOrderBtn);
        ThemeHelper.applyGhostButton(removeBtn);
        ThemeHelper.applyPrimaryButton(addOrUpdateBtn);

        newOrderBtn.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        removeBtn.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        addOrUpdateBtn.setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD));

        JPanel actionButtons = new JPanel(new GridLayout(1, 2, AppTheme.SPACE_2, 0));
        actionButtons.setOpaque(false);
        actionButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        actionButtons.add(newOrderBtn);
        actionButtons.add(removeBtn);

        JPanel primaryActionPanel = new JPanel(new BorderLayout());
        primaryActionPanel.setOpaque(false);
        primaryActionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        primaryActionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        primaryActionPanel.add(addOrUpdateBtn, BorderLayout.CENTER);

        bottomBar.add(actionButtons);
        bottomBar.add(Box.createVerticalStrut(AppTheme.SPACE_2));
        bottomBar.add(primaryActionPanel);

        JPanel railContent = new JPanel();
        railContent.setOpaque(false);
        railContent.setLayout(new BoxLayout(railContent, BoxLayout.Y_AXIS));
        railContent.add(railBody);
        railContent.add(bottomBar);

        JScrollPane railScroll = new JScrollPane(railContent);
        railScroll.setBorder(BorderFactory.createEmptyBorder());
        railScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        railScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        railScroll.getVerticalScrollBar().setUnitIncrement(16);
        railScroll.getViewport().setOpaque(false);
        railScroll.setOpaque(false);

        rail.add(railScroll, BorderLayout.CENTER);
        return rail;
    }

    /** Build a two-column price row: label on left, value on right */
    private JPanel buildPriceRow(String labelText, JLabel valueLabel, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(bold
            ? ThemeFonts.bodyLg().deriveFont(Font.BOLD)
            : ThemeFonts.labelMd());
        lbl.setForeground(bold ? new Color(0x1565C0) : AppTheme.TEXT_SECONDARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    /** Style -/+ quantity buttons to match the mockup (bordered, compact) */
    private void styleQtyButton(JButton btn) {
        btn.setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD));
        btn.setBackground(AppTheme.SURFACE_CONTAINER_HIGH);
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.OUTLINE, 1),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));
        btn.setPreferredSize(new Dimension(32, 28));
        btn.setMaximumSize(new Dimension(32, 28));
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

            if (!keyword.isEmpty()) {
                String itemName = item.getName() == null ? "" : item.getName().toLowerCase();
                String itemDescription = item.getDescription() == null ? "" : item.getDescription().toLowerCase();
                if (!itemName.contains(keyword) && !itemDescription.contains(keyword)) {
                    continue;
                }
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
        final int cardHeight = 96;

        // Outer card — white background, bottom border only
        JPanel card = new JPanel(new BorderLayout(AppTheme.SPACE_3, 0));
        card.setBackground(AppTheme.SURFACE_CONTAINER_LOWEST);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.OUTLINE),
            BorderFactory.createEmptyBorder(
                AppTheme.SPACE_3, AppTheme.SPACE_3,
                AppTheme.SPACE_3, AppTheme.SPACE_3)
        ));
            card.setPreferredSize(new Dimension(0, cardHeight));
            card.setMinimumSize(new Dimension(220, cardHeight));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardHeight));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (selected) {
            card.setBackground(new Color(0xEFF3FB));
        }

        // Square icon placeholder
        JPanel iconBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.SURFACE_CONTAINER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(AppTheme.OUTLINE);
                g2.setStroke(new java.awt.BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                // Simple fork-and-knife icon lines
                g2.setColor(AppTheme.TEXT_SECONDARY);
                g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.drawLine(cx - 6, cy - 8, cx - 6, cy + 8);
                g2.drawLine(cx - 6, cy - 4, cx - 2, cy - 4);
                g2.drawLine(cx - 2, cy - 8, cx - 2, cy + 4);
                g2.drawOval(cx - 2, cy + 2, 1, 3);
                g2.drawLine(cx + 4, cy - 8, cx + 4, cy + 8);
                g2.drawArc(cx + 1, cy - 8, 6, 6, 0, -180);
                g2.dispose();
            }
        };
        iconBox.setPreferredSize(new Dimension(40, 40));
        iconBox.setMaximumSize(new Dimension(40, 40));
        iconBox.setOpaque(false);

        JPanel iconWrap = new JPanel(new java.awt.GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.setPreferredSize(new Dimension(52, cardHeight));
        iconWrap.add(iconBox);

        // Item info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(item.getName());
        name.setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD));
        name.setForeground(AppTheme.TEXT_PRIMARY);

        String descriptionText = item.getDescription() == null || item.getDescription().isBlank()
            ? "No description"
            : item.getDescription();

        JLabel description = new JLabel(descriptionText);
        description.setFont(ThemeFonts.labelMd());
        description.setForeground(AppTheme.TEXT_SECONDARY);
        description.putClientProperty("html.disable", Boolean.TRUE);

        JLabel price = new JLabel(formatCurrency(item.getBasePrice()));
        price.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        price.setForeground(AppTheme.SECONDARY);

        info.add(name);
        info.add(Box.createVerticalStrut(2));
        info.add(description);
        info.add(Box.createVerticalStrut(2));
        info.add(price);

        // CUSTOMIZE button — uppercase, outline style
        JButton customizeBtn = new JButton("CUSTOMIZE");
        customizeBtn.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        customizeBtn.setBackground(AppTheme.SURFACE_CONTAINER_LOWEST);
        customizeBtn.setForeground(AppTheme.TEXT_PRIMARY);
        customizeBtn.setOpaque(true);
        customizeBtn.setFocusPainted(false);
        customizeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        customizeBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
            BorderFactory.createEmptyBorder(
                AppTheme.SPACE_1, AppTheme.SPACE_3,
                AppTheme.SPACE_1, AppTheme.SPACE_3)
        ));
        customizeBtn.setPreferredSize(new Dimension(120, 36));
        customizeBtn.setMinimumSize(new Dimension(120, 36));
        customizeBtn.setMaximumSize(new Dimension(120, 36));
        customizeBtn.addActionListener(e -> selectMenuItemById(item.getId()));

        JPanel actionWrap = new JPanel(new java.awt.GridBagLayout());
        actionWrap.setOpaque(false);
        actionWrap.setPreferredSize(new Dimension(132, cardHeight));
        actionWrap.add(customizeBtn);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectMenuItemById(item.getId());
            }
        });

        card.add(iconWrap, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        card.add(actionWrap, BorderLayout.EAST);
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
        if (!currentDraft.getItems().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Create a new draft now? Current draft items will be cleared. Submitted orders are not affected.",
                "New Order",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        currentDraft.clearItems();
        orderModel.setRowCount(0);
        orderTable.clearSelection();
        addOrUpdateBtn.setText("Add Item to Order");
        quantityField.setText("1");
        noteArea.setText("");
        clearCustomizationSelection();
        updatePreviewAndSubtotal();
        showSystemNotice("Started a new order draft. Previous submitted orders remain in history.");
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
        String normalizedNote = noteArea.getText() == null ? "" : noteArea.getText().trim();
        int selectedRow = orderTable.getSelectedRow();

        if (selectedRow >= 0) {
            orderService.replaceItem(currentDraft, selectedRow, selectedItem, selectedCustomizations, quantity, normalizedNote);
            showSystemNotice("Item updated in active order.");
        } else {
            int existingIndex = findMatchingItemIndex(selectedItem, selectedCustomizations, normalizedNote);
            if (existingIndex >= 0) {
                OrderItem existingItem = currentDraft.getItems().get(existingIndex);
                int mergedQuantity = existingItem.getQuantity() + quantity;
                orderService.replaceItem(currentDraft, existingIndex,
                    existingItem.getMenuItem(), existingItem.getCustomizations(), mergedQuantity, existingItem.getNote());
                showSystemNotice("Item merged into cart and quantity updated.");
            } else {
                orderService.addItem(currentDraft, selectedItem, selectedCustomizations, quantity, normalizedNote);
                showSystemNotice("Item added to active order.");
            }
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
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            String customizationText = item.getCustomizationSummary();
            String note = item.getNote() == null ? "" : item.getNote().trim();
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
            noteArea.setText(selectedOrderItem.getNote());
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
        String normalizedNote = noteArea.getText() == null ? "" : noteArea.getText().trim();
        orderService.replaceItem(currentDraft, selectedRow, selectedItem, selectedCustomizations, quantity, normalizedNote);
        refreshTable();
        orderTable.setRowSelectionInterval(selectedRow, selectedRow);
        updatePreviewAndSubtotal();
    }

    private void updatePreviewAndSubtotal() {
        java.math.BigDecimal draftTotal = java.math.BigDecimal.ZERO;
        for (com.oop.project.model.OrderItem oi : currentDraft.getItems()) {
            draftTotal = draftTotal.add(oi.getLineTotal());
        }

        MenuItem item = (MenuItem) menuCombo.getSelectedItem();
        if (item == null) {
            unitPriceValueLabel.setText("$0.00");
            lineTotalValueLabel.setText("$0.00");
            subtotalValueLabel.setText(formatCurrency(draftTotal));
            return;
        }

        // Calculate unit price with selected customizations
        java.math.BigDecimal unitPrice = item.getBasePrice();
        for (CustomizationOption opt : getSelectedCustomizations()) {
            unitPrice = unitPrice.add(opt.getPriceDelta());
        }

        int qty = parseQuantityOrDefault();
        java.math.BigDecimal lineTotal = unitPrice.multiply(java.math.BigDecimal.valueOf(qty));

        unitPriceValueLabel.setText(formatCurrency(unitPrice));
        lineTotalValueLabel.setText(formatCurrency(lineTotal));
        subtotalValueLabel.setText(formatCurrency(draftTotal));
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

    private int findMatchingItemIndex(MenuItem targetItem, List<CustomizationOption> targetCustomizations, String targetNote) {
        List<OrderItem> items = currentDraft.getItems();
        String targetCustomizationKey = buildCustomizationKey(targetCustomizations);

        for (int i = 0; i < items.size(); i++) {
            OrderItem existing = items.get(i);
            if (existing.getMenuItem().getId() != targetItem.getId()) {
                continue;
            }

            String existingCustomizationKey = buildCustomizationKey(existing.getCustomizations());
            if (!Objects.equals(existingCustomizationKey, targetCustomizationKey)) {
                continue;
            }

            String existingNote = existing.getNote() == null ? "" : existing.getNote().trim();
            if (Objects.equals(existingNote, targetNote)) {
                return i;
            }
        }

        return -1;
    }

    private String buildCustomizationKey(List<CustomizationOption> customizations) {
        return customizations.stream()
            .map(CustomizationOption::getId)
            .sorted()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
    }

    private void updateSelectedNoteIfPossible() {
        if (applyingSelection) {
            return;
        }

        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentDraft.getItems().size()) {
            return;
        }

        currentDraft.getItems().get(selectedRow).setNote(noteArea.getText());
        refreshTable();
        if (selectedRow < orderModel.getRowCount()) {
            orderTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
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
