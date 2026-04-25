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
import java.awt.Image;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.oop.project.model.Order;
import com.oop.project.model.OrderItem;
import com.oop.project.service.DashboardService;
import com.oop.project.ui.theme.ThemeFonts;

public class DashboardPanel extends JPanel {
    private static final Color PAGE_BG = new Color(0xF4F6F8);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(0xE3E8EE);
    private static final Color TEXT_PRIMARY = new Color(0x1B2533);
    private static final Color TEXT_SECONDARY = new Color(0x5D6B7C);
    private static final Color KPI_BLUE = new Color(0x0F2740);
    private static final Color POSITIVE = new Color(0x1A7F37);
    private static final Color NEGATIVE = new Color(0xC62828);
    private static final Color STATUS_OPEN_BG = new Color(0xE8EDF3);
    private static final Color STATUS_OPEN_FG = new Color(0x44546A);
    private static final Color STATUS_PAID_BG = new Color(0xE6F4EA);
    private static final Color STATUS_PAID_FG = new Color(0x1A7F37);
    private static final Color STATUS_CANCEL_BG = new Color(0xFDEBEC);
    private static final Color STATUS_CANCEL_FG = new Color(0xC62828);

    private final DashboardService dashboardService = new DashboardService();
    private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat numberFormat = new DecimalFormat("#,##0");
    private final DateTimeFormatter summaryDateFormat = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private final DateTimeFormatter tableTimeFormat = DateTimeFormatter.ofPattern("HH:mm");

    private final JComboBox<String> periodCombo = new JComboBox<>(new String[] {"Today", "Yesterday", "Last 7 Days"});
    private final JButton exportReportBtn = new JButton("Export Report");

    private final MetricCard totalRevenueCard = new MetricCard("TOTAL REVENUE", KPI_BLUE);
    private final MetricCard orderCountCard = new MetricCard("ORDER COUNT", KPI_BLUE);
    private final MetricCard avgValueCard = new MetricCard("AVG TABLE VALUE", KPI_BLUE);
    private final MetricCard occupancyCard = new MetricCard("OCCUPANCY RATE", POSITIVE);

    private final HourlyChartPanel hourlyChartPanel = new HourlyChartPanel();
    private final JPanel topItemsListPanel = new JPanel();

    private final JTextField searchField = new JTextField();
    private final JButton filterBtn = new JButton("Filter");
    private final JButton exportCsvBtn = new JButton("Export CSV");
    private final JLabel summaryDateLabel = new JLabel("Performance summary");
    private final JLabel updatedAtLabel = new JLabel("Updated -");
    private final JLabel tableCountLabel = new JLabel("Showing 0 orders");

    private final JTable ordersTable = new JTable();
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new Object[] {"ORDER ID", "SERVER", "TABLE", "STATUS", "TIME", "TOTAL", ""}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private List<Order> loadedOrders = new ArrayList<>();
    private List<Order> visibleOrders = new ArrayList<>();

    private BigDecimal minPriceFilter = BigDecimal.ZERO;
    private BigDecimal maxPriceFilter = null;
    private String statusFilter = "ALL";
    private String sortFilter = "LATEST";

    public DashboardPanel() {
        ThemeFonts.initialize();

        setLayout(new BorderLayout());
        setBackground(PAGE_BG);

        buildLayout();
        bindEvents();
        refreshDashboardData();
    }

    public void refreshDashboardData() {
        reloadDataForSelectedPeriod();
    }

    private void buildLayout() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(8, 8, 12, 8));

        page.add(buildHeaderRow());
        page.add(Box.createVerticalStrut(12));
        page.add(buildKpiRow());
        page.add(Box.createVerticalStrut(12));
        page.add(buildMiddleRow());
        page.add(Box.createVerticalStrut(12));
        page.add(buildOrderHistoryCard());

        JScrollPane pageScroll = new JScrollPane(page);
        pageScroll.setBorder(BorderFactory.createEmptyBorder());
        pageScroll.getViewport().setBackground(PAGE_BG);
        pageScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pageScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(pageScroll, BorderLayout.CENTER);
    }

    private JPanel buildHeaderRow() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Executive Analytics");
        title.setFont(ThemeFonts.titleLg().deriveFont(Font.BOLD, 22f));
        title.setForeground(TEXT_PRIMARY);

        summaryDateLabel.setFont(ThemeFonts.labelMd());
        summaryDateLabel.setForeground(TEXT_SECONDARY);

        updatedAtLabel.setFont(ThemeFonts.labelSm());
        updatedAtLabel.setForeground(TEXT_SECONDARY);

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(summaryDateLabel);
        left.add(Box.createVerticalStrut(2));
        left.add(updatedAtLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        stylePeriodCombo(periodCombo);
        stylePrimaryButton(exportReportBtn, KPI_BLUE);
        exportReportBtn.setPreferredSize(new Dimension(118, 34));

        right.add(periodCombo);
        right.add(exportReportBtn);

        row.add(left, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);
        row.add(totalRevenueCard);
        row.add(orderCountCard);
        row.add(avgValueCard);
        row.add(occupancyCard);
        return row;
    }

    private JPanel buildMiddleRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        CardPanel chartCard = new CardPanel(CARD_BG, 14);
        chartCard.setLayout(new BorderLayout(0, 8));
        chartCard.setBorderPadding(14, 14, 14, 14);

        JPanel chartHeader = new JPanel();
        chartHeader.setOpaque(false);
        chartHeader.setLayout(new BoxLayout(chartHeader, BoxLayout.Y_AXIS));

        JLabel chartTitle = new JLabel("Hourly Performance");
        chartTitle.setFont(ThemeFonts.titleLg().deriveFont(Font.BOLD, 16f));
        chartTitle.setForeground(TEXT_PRIMARY);

        JLabel chartSub = new JLabel("Revenue distribution through the day");
        chartSub.setFont(ThemeFonts.labelSm());
        chartSub.setForeground(TEXT_SECONDARY);

        chartHeader.add(chartTitle);
        chartHeader.add(Box.createVerticalStrut(2));
        chartHeader.add(chartSub);

        chartCard.add(chartHeader, BorderLayout.NORTH);
        chartCard.add(hourlyChartPanel, BorderLayout.CENTER);

        CardPanel topItemsCard = new CardPanel(CARD_BG, 14);
        topItemsCard.setLayout(new BorderLayout(0, 8));
        topItemsCard.setBorderPadding(14, 14, 14, 14);
        topItemsCard.setPreferredSize(new Dimension(245, 0));

        JLabel topItemsTitle = new JLabel("Top Selling Items");
        topItemsTitle.setFont(ThemeFonts.titleLg().deriveFont(Font.BOLD, 16f));
        topItemsTitle.setForeground(TEXT_PRIMARY);

        topItemsListPanel.setOpaque(false);
        topItemsListPanel.setLayout(new BoxLayout(topItemsListPanel, BoxLayout.Y_AXIS));

        topItemsCard.add(topItemsTitle, BorderLayout.NORTH);
        topItemsCard.add(topItemsListPanel, BorderLayout.CENTER);

        row.add(chartCard, BorderLayout.CENTER);
        row.add(topItemsCard, BorderLayout.EAST);
        return row;
    }

    private JPanel buildOrderHistoryCard() {
        CardPanel card = new CardPanel(CARD_BG, 14);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorderPadding(14, 14, 10, 14);

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Order History");
        title.setFont(ThemeFonts.titleLg().deriveFont(Font.BOLD, 16f));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Real-time transaction log");
        subtitle.setFont(ThemeFonts.labelSm());
        subtitle.setForeground(TEXT_SECONDARY);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        styleSearchField(searchField);
        searchField.setPreferredSize(new Dimension(170, 34));

        styleGhostButton(filterBtn);
        stylePrimaryButton(exportCsvBtn, KPI_BLUE);

        controls.add(searchField);
        controls.add(filterBtn);
        controls.add(exportCsvBtn);

        header.add(titleBlock, BorderLayout.CENTER);
        header.add(controls, BorderLayout.EAST);

        buildOrdersTable();

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        tableCountLabel.setFont(ThemeFonts.labelMd());
        tableCountLabel.setForeground(TEXT_SECONDARY);

        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pagination.setOpaque(false);
        pagination.add(createPagerButton("<"));
        pagination.add(createPagerButton("1", true));
        pagination.add(createPagerButton("2"));
        pagination.add(createPagerButton("3"));
        pagination.add(createPagerButton(">"));

        footer.add(tableCountLabel, BorderLayout.WEST);
        footer.add(pagination, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        card.add(new JScrollPane(ordersTable), BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JButton createPagerButton(String text) {
        return createPagerButton(text, false);
    }

    private JButton createPagerButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        btn.setPreferredSize(new Dimension(30, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        if (active) {
            btn.setBackground(KPI_BLUE);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(CARD_BG);
            btn.setForeground(TEXT_SECONDARY);
        }
        return btn;
    }

    private void buildOrdersTable() {
        ordersTable.setModel(tableModel);
        ordersTable.setRowHeight(38);
        ordersTable.setShowVerticalLines(false);
        ordersTable.setShowHorizontalLines(true);
        ordersTable.setGridColor(new Color(0xEEF2F6));
        ordersTable.setIntercellSpacing(new Dimension(0, 0));
        ordersTable.setBackground(CARD_BG);
        ordersTable.setFont(ThemeFonts.bodyMd());
        ordersTable.setSelectionBackground(new Color(0xE9F0F8));
        ordersTable.setSelectionForeground(TEXT_PRIMARY);
        ordersTable.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()));
        ordersTable.setRowSelectionAllowed(true);

        ordersTable.getTableHeader().setReorderingAllowed(false);
        ordersTable.getTableHeader().setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        ordersTable.getTableHeader().setBackground(new Color(0xF6F8FB));
        ordersTable.getTableHeader().setForeground(TEXT_SECONDARY);
        ordersTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));

        ordersTable.setDefaultRenderer(Object.class, new OrderRowRenderer());
        ordersTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        ordersTable.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());

        int[] widths = {130, 150, 110, 100, 90, 120, 40};
        for (int i = 0; i < widths.length; i++) {
            ordersTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void bindEvents() {
        periodCombo.addActionListener(e -> reloadDataForSelectedPeriod());
        exportReportBtn.addActionListener(e -> exportVisibleOrdersCsv(true));
        exportCsvBtn.addActionListener(e -> exportVisibleOrdersCsv(false));
        filterBtn.addActionListener(e -> showFilterDialog());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyActiveFiltersAndRefreshTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyActiveFiltersAndRefreshTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyActiveFiltersAndRefreshTable();
            }
        });
    }

    private void reloadDataForSelectedPeriod() {
        DateRange range = getSelectedDateRange();
        summaryDateLabel.setText(buildSummaryText(range));

        loadedOrders = dashboardService.getOrdersByDateRange(range.startDate, range.endDate).stream()
            .map(dashboardService::getOrderWithItems)
            .filter(this::isDashboardActiveOrder)
            .collect(Collectors.toList());

        updateKpiCards(range);
        updateHourlyPerformanceChart();
        updateTopSellingItems();
        applyActiveFiltersAndRefreshTable();

        updatedAtLabel.setText("Updated " + LocalDateTime.now().withNano(0).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private boolean isDashboardActiveOrder(Order order) {
        if (order == null) {
            return false;
        }

        String status = order.getOrderStatus();
        return !(Order.STATUS_CANCELLED.equals(status)
            || "CANCELED".equals(status)
            || "VOID".equals(status));
    }

    private DateRange getSelectedDateRange() {
        LocalDate today = LocalDate.now();
        int index = periodCombo.getSelectedIndex();

        if (index == 1) {
            LocalDate yesterday = today.minusDays(1);
            return new DateRange(yesterday, yesterday);
        }
        if (index == 2) {
            return new DateRange(today.minusDays(6), today);
        }
        return new DateRange(today, today);
    }

    private String buildSummaryText(DateRange range) {
        if (range.startDate.equals(range.endDate)) {
            return "Performance summary for " + range.startDate.format(summaryDateFormat);
        }
        return "Performance summary for "
            + range.startDate.format(summaryDateFormat)
            + " - "
            + range.endDate.format(summaryDateFormat);
    }

    private void updateKpiCards(DateRange range) {
        DateRange prevRange = range.previousWindow();

        List<Order> previousOrders = dashboardService.getOrdersByDateRange(prevRange.startDate, prevRange.endDate).stream()
            .filter(this::isDashboardActiveOrder)
            .collect(Collectors.toList());

        BigDecimal revenue = sumRevenue(loadedOrders);
        BigDecimal previousRevenue = sumRevenue(previousOrders);

        int orderCount = loadedOrders.size();
        int previousOrderCount = previousOrders.size();

        BigDecimal avgValue = averageOrderValue(revenue, orderCount);
        BigDecimal previousAvg = averageOrderValue(previousRevenue, previousOrderCount);

        int occupancyRate = calculateOccupancy(orderCount, range);
        int previousOccupancy = calculateOccupancy(previousOrderCount, prevRange);

        Trend revenueTrend = percentTrend(revenue, previousRevenue);
        Trend countTrend = percentTrend(BigDecimal.valueOf(orderCount), BigDecimal.valueOf(previousOrderCount));
        Trend avgTrend = percentTrend(avgValue, previousAvg);

        totalRevenueCard.setValue(formatKpiCurrency(revenue));
        totalRevenueCard.setTrend(revenueTrend.text + " vs last period", revenueTrend.positive);

        orderCountCard.setValue(numberFormat.format(orderCount));
        orderCountCard.setTrend(countTrend.text + " vs last period", countTrend.positive);

        avgValueCard.setValue(formatKpiCurrency(avgValue));
        avgValueCard.setTrend(avgTrend.text + " vs last period", avgTrend.positive);

        occupancyCard.setValue(occupancyRate + "%");
        String occupancyHint = occupancyRate >= 80 ? "Peak Demand" : (occupancyRate >= 60 ? "Busy Hours" : "Normal Flow");
        boolean occupancyPositive = occupancyRate >= previousOccupancy;
        occupancyCard.setTrend(occupancyHint, occupancyPositive);
    }

    private BigDecimal sumRevenue(List<Order> orders) {
        return orders.stream()
            .map(Order::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal averageOrderValue(BigDecimal revenue, int count) {
        if (count <= 0) {
            return BigDecimal.ZERO;
        }
        return revenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private int calculateOccupancy(int orderCount, DateRange range) {
        int capacityPerDay = 40;
        long days = range.dayCount();
        int denominator = Math.max(1, (int) (capacityPerDay * days));
        int rate = (int) Math.round((orderCount * 100.0) / denominator);
        return Math.min(100, Math.max(0, rate));
    }

    private Trend percentTrend(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current == null || current.compareTo(BigDecimal.ZERO) == 0) {
                return new Trend("0.0%", true);
            }
            return new Trend("+100.0%", true);
        }

        BigDecimal delta = current.subtract(previous);
        BigDecimal percent = delta
            .divide(previous.abs(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        boolean positive = percent.compareTo(BigDecimal.ZERO) >= 0;
        String sign = positive ? "+" : "";
        return new Trend(sign + percent.setScale(1, RoundingMode.HALF_UP) + "%", positive);
    }

    private void updateHourlyPerformanceChart() {
        Map<Integer, BigDecimal> hourlyRevenue = new LinkedHashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            hourlyRevenue.put(hour, BigDecimal.ZERO);
        }

        for (Order order : loadedOrders) {
            LocalDateTime createdAt = order.getCreatedAt();
            if (createdAt == null) {
                continue;
            }
            int hour = createdAt.getHour();
            BigDecimal total = order.getTotal() == null ? BigDecimal.ZERO : order.getTotal();
            hourlyRevenue.put(hour, hourlyRevenue.get(hour).add(total));
        }

        hourlyChartPanel.setHourlyRevenue(hourlyRevenue);
    }

    private void updateTopSellingItems() {
        Map<String, ItemStats> statsByName = new HashMap<>();

        for (Order order : loadedOrders) {
            List<OrderItem> items = order.getItems();
            if (items == null) {
                continue;
            }
            for (OrderItem item : items) {
                String itemName = item.getMenuItemName();
                ItemStats stats = statsByName.computeIfAbsent(itemName, key -> new ItemStats());
                stats.quantity += item.getQuantity();
                stats.revenue = stats.revenue.add(item.getLineTotal());
            }
        }

        List<Map.Entry<String, ItemStats>> sorted = statsByName.entrySet().stream()
            .sorted((a, b) -> {
                int cmpQty = Integer.compare(b.getValue().quantity, a.getValue().quantity);
                if (cmpQty != 0) {
                    return cmpQty;
                }
                return b.getValue().revenue.compareTo(a.getValue().revenue);
            })
            .limit(5)
            .collect(Collectors.toList());

        topItemsListPanel.removeAll();

        if (sorted.isEmpty()) {
            JLabel empty = new JLabel("No sales data for selected period.");
            empty.setForeground(TEXT_SECONDARY);
            empty.setFont(ThemeFonts.bodyMd());
            topItemsListPanel.add(empty);
        } else {
            for (Map.Entry<String, ItemStats> entry : sorted) {
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setOpaque(false);
                row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

                JLabel thumb = new JLabel(loadTopItemThumbnail(entry.getKey(), 40, 40));
                thumb.setPreferredSize(new Dimension(40, 40));

                JPanel center = new JPanel();
                center.setOpaque(false);
                center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

                JLabel name = new JLabel("<html><div style='width:95px;'>" + escapeHtml(entry.getKey()) + "</div></html>");
                name.setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD));
                name.setForeground(TEXT_PRIMARY);

                JLabel qty = new JLabel(entry.getValue().quantity + " orders");
                qty.setFont(ThemeFonts.labelSm());
                qty.setForeground(TEXT_SECONDARY);

                center.add(name);
                center.add(Box.createVerticalStrut(1));
                center.add(qty);

                JLabel revenue = new JLabel("+" + formatCompactCurrency(entry.getValue().revenue));
                revenue.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
                revenue.setForeground(POSITIVE);

                row.add(thumb, BorderLayout.WEST);
                row.add(center, BorderLayout.CENTER);
                row.add(revenue, BorderLayout.EAST);

                topItemsListPanel.add(row);
            }

            JLabel link = new JLabel("View Detailed Inventory ->");
            link.setFont(ThemeFonts.labelSm().deriveFont(Font.BOLD));
            link.setForeground(KPI_BLUE);
            link.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            topItemsListPanel.add(link);
        }

        topItemsListPanel.revalidate();
        topItemsListPanel.repaint();
    }

    private ImageIcon loadTopItemThumbnail(String itemName, int w, int h) {
        if (itemName == null || itemName.isBlank()) {
            return createThumbPlaceholder(w, h);
        }

        String baseName = itemName.trim().toLowerCase().replaceAll("\\s+", "_");
        String[] paths = {
            "images/" + baseName + ".jpg",
            "images/" + baseName + ".jpeg",
            "images/" + baseName + ".png"
        };

        for (String path : paths) {
            java.io.File file = new java.io.File(path);
            if (!file.exists()) {
                continue;
            }

            ImageIcon raw = new ImageIcon(file.getAbsolutePath());
            if (raw.getIconWidth() <= 0) {
                continue;
            }

            Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }

        return createThumbPlaceholder(w, h);
    }

    private ImageIcon createThumbPlaceholder(int w, int h) {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0xDDE4EC));
        g2.fillRoundRect(0, 0, w, h, 8, 8);
        g2.setColor(new Color(0x8A98AA));
        g2.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        g2.drawString("IMG", Math.max(4, w / 2 - 10), h / 2 + 4);
        g2.dispose();
        return new ImageIcon(image);
    }

    private void applyActiveFiltersAndRefreshTable() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<Order> filtered = loadedOrders.stream()
            .filter(order -> filterByKeyword(order, keyword))
            .filter(this::filterByPriceRange)
            .filter(this::filterByStatus)
            .collect(Collectors.toCollection(ArrayList::new));

        sortFilteredOrders(filtered);
        visibleOrders = filtered;
        refreshOrdersTable();
    }

    private boolean filterByKeyword(Order order, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }

        if (String.valueOf(order.getId()).contains(keyword)) {
            return true;
        }
        if (order.getStaffName() != null && order.getStaffName().toLowerCase().contains(keyword)) {
            return true;
        }
        if (order.getOrderStatus() != null && order.getOrderStatus().toLowerCase().contains(keyword)) {
            return true;
        }

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getMenuItemName() != null && item.getMenuItemName().toLowerCase().contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean filterByPriceRange(Order order) {
        BigDecimal total = order.getTotal() == null ? BigDecimal.ZERO : order.getTotal();
        if (minPriceFilter != null && total.compareTo(minPriceFilter) < 0) {
            return false;
        }
        if (maxPriceFilter != null && total.compareTo(maxPriceFilter) > 0) {
            return false;
        }
        return true;
    }

    private boolean filterByStatus(Order order) {
        if ("ALL".equals(statusFilter)) {
            return true;
        }
        return statusFilter.equalsIgnoreCase(order.getOrderStatus());
    }

    private void sortFilteredOrders(List<Order> filtered) {
        Comparator<Order> comparator;
        switch (sortFilter) {
            case "OLDEST" -> comparator = Comparator.comparing(
                Order::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "TOTAL_DESC" -> comparator = Comparator.comparing(
                Order::getTotal,
                Comparator.nullsLast(Comparator.reverseOrder())
            );
            case "TOTAL_ASC" -> comparator = Comparator.comparing(
                Order::getTotal,
                Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "LATEST" -> comparator = Comparator.comparing(
                Order::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())
            );
            default -> comparator = Comparator.comparing(
                Order::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())
            );
        }
        filtered.sort(comparator);
    }

    private void refreshOrdersTable() {
        tableModel.setRowCount(0);

        for (Order order : visibleOrders) {
            String formattedOrderId = "#ORD-" + String.format("%04d", order.getId());
            String formattedTime = order.getCreatedAt() == null ? "--:--" : order.getCreatedAt().format(tableTimeFormat);

            tableModel.addRow(new Object[] {
                formattedOrderId,
                order.getStaffName(),
                pseudoTableLabel(order.getId()),
                order.getOrderStatus(),
                formattedTime,
                formatCurrency(order.getTotal()),
                "..."
            });
        }

        int count = visibleOrders.size();
        if (count == 0) {
            tableCountLabel.setText("Showing 0 orders");
        } else {
            tableCountLabel.setText("Showing 1-" + count + " of " + count + " orders");
        }
    }

    private void showFilterDialog() {
        JTextField minField = new JTextField(minPriceFilter == null ? "" : minPriceFilter.toPlainString());
        JTextField maxField = new JTextField(maxPriceFilter == null ? "" : maxPriceFilter.toPlainString());

        JComboBox<String> statusCombo = new JComboBox<>(new String[] {"ALL", "OPEN", "PAID"});
        statusCombo.setSelectedItem(statusFilter);

        JComboBox<String> sortCombo = new JComboBox<>(new String[] {
            "Time (Latest)",
            "Time (Oldest)",
            "Total (High to Low)",
            "Total (Low to High)"
        });
        sortCombo.setSelectedIndex(sortFilterToIndex(sortFilter));

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.add(new JLabel("Min Price (VND)"));
        form.add(minField);
        form.add(new JLabel("Max Price (VND)"));
        form.add(maxField);
        form.add(new JLabel("Status"));
        form.add(statusCombo);
        form.add(new JLabel("Sort"));
        form.add(sortCombo);

        int result = JOptionPane.showConfirmDialog(
            this,
            form,
            "Filter Orders",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            minPriceFilter = parseOptionalPrice(minField.getText(), BigDecimal.ZERO);
            maxPriceFilter = parseOptionalPrice(maxField.getText(), null);
            if (maxPriceFilter != null && minPriceFilter.compareTo(maxPriceFilter) > 0) {
                JOptionPane.showMessageDialog(this, "Min price must be <= max price.");
                return;
            }

            statusFilter = String.valueOf(statusCombo.getSelectedItem());
            sortFilter = indexToSortFilter(sortCombo.getSelectedIndex());
            applyActiveFiltersAndRefreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price filters must be valid numbers.", "Filter", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int sortFilterToIndex(String sort) {
        return switch (sort) {
            case "OLDEST" -> 1;
            case "TOTAL_DESC" -> 2;
            case "TOTAL_ASC" -> 3;
            case "LATEST" -> 0;
            default -> 0;
        };
    }

    private String indexToSortFilter(int index) {
        return switch (index) {
            case 1 -> "OLDEST";
            case 2 -> "TOTAL_DESC";
            case 3 -> "TOTAL_ASC";
            case 0 -> "LATEST";
            default -> "LATEST";
        };
    }

    private BigDecimal parseOptionalPrice(String raw, BigDecimal fallback) {
        String normalized = raw == null ? "" : raw.trim().replace(",", "").replace(" ", "");
        if (normalized.isEmpty()) {
            return fallback;
        }
        return new BigDecimal(normalized);
    }

    private void exportVisibleOrdersCsv(boolean isReportMode) {
        if (visibleOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No orders available to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(isReportMode ? "Export Executive Report" : "Export Orders CSV");
        chooser.setSelectedFile(new java.io.File(isReportMode ? "dashboard-report.csv" : "orders.csv"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File targetFile = chooser.getSelectedFile();

        try (java.io.PrintWriter out = new java.io.PrintWriter(targetFile, java.nio.charset.StandardCharsets.UTF_8)) {
            out.println("order_id,server,table,status,time,total_vnd,item_count");
            for (Order order : visibleOrders) {
                String time = order.getCreatedAt() == null ? "" : order.getCreatedAt().format(tableTimeFormat);
                out.println(csv(order.getId()) + ","
                    + csv(order.getStaffName()) + ","
                    + csv(pseudoTableLabel(order.getId())) + ","
                    + csv(order.getOrderStatus()) + ","
                    + csv(time) + ","
                    + csv(order.getTotal()) + ","
                    + csv(order.getItemCount()));
            }

            JOptionPane.showMessageDialog(
                this,
                (isReportMode ? "Executive report" : "CSV") + " exported successfully:\n" + targetFile.getAbsolutePath(),
                "Export",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to export file: " + ex.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private String pseudoTableLabel(int orderId) {
        int tableNumber = (Math.abs(orderId) % 24) + 1;
        return "Table " + String.format("%02d", tableNumber);
    }

    private void styleSearchField(JTextField field) {
        field.setFont(ThemeFonts.labelMd());
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(CARD_BG);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setToolTipText("Search orders by id, staff, status, or item name");
    }

    private void stylePrimaryButton(JButton button, Color bg) {
        button.setFocusable(false);
        button.setFont(ThemeFonts.labelSm().deriveFont(Font.BOLD));
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
    }

    private void styleGhostButton(JButton button) {
        button.setFocusable(false);
        button.setFont(ThemeFonts.labelSm().deriveFont(Font.BOLD));
        button.setBackground(CARD_BG);
        button.setForeground(TEXT_PRIMARY);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(72, 34));
    }

    private void stylePeriodCombo(JComboBox<String> combo) {
        combo.setFocusable(false);
        combo.setFont(ThemeFonts.labelSm());
        combo.setBackground(CARD_BG);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        combo.setPreferredSize(new Dimension(130, 34));
    }

    private String formatKpiCurrency(BigDecimal amount) {
        BigDecimal safe = amount == null ? BigDecimal.ZERO : amount;
        return numberFormat.format(safe);
    }

    private String formatCompactCurrency(BigDecimal amount) {
        BigDecimal safe = amount == null ? BigDecimal.ZERO : amount;
        return numberFormat.format(safe);
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

    private String formatCurrency(BigDecimal amount) {
        BigDecimal safe = amount == null ? BigDecimal.ZERO : amount;
        return currencyFormat.format(safe) + " VND";
    }

    private static final class Trend {
        private final String text;
        private final boolean positive;

        private Trend(String text, boolean positive) {
            this.text = text;
            this.positive = positive;
        }
    }

    private static final class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;

        private DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        private long dayCount() {
            return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }

        private DateRange previousWindow() {
            long days = dayCount();
            LocalDate prevEnd = startDate.minusDays(1);
            LocalDate prevStart = prevEnd.minusDays(days - 1);
            return new DateRange(prevStart, prevEnd);
        }
    }

    private static final class ItemStats {
        private int quantity;
        private BigDecimal revenue = BigDecimal.ZERO;
    }

    private static class CardPanel extends JPanel {
        private final Color fill;
        private final int radius;

        CardPanel(Color fill, int radius) {
            this.fill = fill;
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
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class MetricCard extends CardPanel {
        private final JLabel valueLabel = new JLabel("0");
        private final JLabel trendLabel = new JLabel("0% vs last period");
        private final JPanel accentBar = new JPanel();

        MetricCard(String title, Color accent) {
            super(CARD_BG, 12);
            setLayout(new BorderLayout(0, 8));
            setBorderPadding(12, 14, 10, 14);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
            titleLabel.setForeground(TEXT_SECONDARY);

            valueLabel.setFont(ThemeFonts.displayMd().deriveFont(Font.BOLD, 22f));
            valueLabel.setForeground(TEXT_PRIMARY);

            trendLabel.setFont(ThemeFonts.labelSm().deriveFont(Font.BOLD));
            trendLabel.setForeground(POSITIVE);

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.add(titleLabel);
            center.add(Box.createVerticalStrut(8));
            center.add(valueLabel);
            center.add(Box.createVerticalStrut(6));
            center.add(trendLabel);

            accentBar.setBackground(accent);
            accentBar.setPreferredSize(new Dimension(0, 3));

            add(center, BorderLayout.CENTER);
            add(accentBar, BorderLayout.SOUTH);
        }

        void setValue(String text) {
            valueLabel.setText(text);
        }

        void setTrend(String text, boolean positive) {
            trendLabel.setText(text);
            trendLabel.setForeground(positive ? POSITIVE : NEGATIVE);
        }
    }

    private static class HourlyChartPanel extends JPanel {
        private Map<Integer, BigDecimal> hourlyRevenue = new LinkedHashMap<>();

        HourlyChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 250));
            for (int hour = 0; hour < 24; hour++) {
                hourlyRevenue.put(hour, BigDecimal.ZERO);
            }
        }

        void setHourlyRevenue(Map<Integer, BigDecimal> hourlyRevenue) {
            this.hourlyRevenue = new LinkedHashMap<>(hourlyRevenue);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int left = 14;
            int right = 14;
            int bottom = 34;
            int top = 18;

            List<Map.Entry<Integer, BigDecimal>> entries = new ArrayList<>(hourlyRevenue.entrySet());
            int n = entries.size();
            if (n == 0) {
                g2.dispose();
                return;
            }

            int chartW = Math.max(1, width - left - right);
            int chartH = Math.max(1, height - top - bottom);

            g2.setColor(new Color(0xE3E9EF));
            g2.drawLine(left, top + chartH, left + chartW, top + chartH);

            BigDecimal max = BigDecimal.ZERO;
            int peakIndex = -1;
            for (int i = 0; i < n; i++) {
                BigDecimal value = entries.get(i).getValue();
                BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
                if (safeValue.compareTo(max) > 0) {
                    max = safeValue;
                    peakIndex = i;
                }
            }

            boolean hasRevenue = max.compareTo(BigDecimal.ZERO) > 0;
            if (!hasRevenue) {
                max = BigDecimal.ONE;
            }

            double slotWidth = chartW / (double) Math.max(1, n);
            int barW = Math.max(2, (int) Math.floor(slotWidth * 0.68));
            int labelStep = n > 18 ? 3 : (n > 12 ? 2 : 1);

            Color zeroBar = new Color(0xD8E0E8);
            Color upBar = new Color(0x2E7D32);
            Color downBar = new Color(0xC62828);
            Color flatBar = new Color(0x8899AA);
            Color peakOutline = new Color(0x132A40);

            for (int i = 0; i < n; i++) {
                BigDecimal value = entries.get(i).getValue();
                BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
                double ratio = safeValue.divide(max, 4, RoundingMode.HALF_UP).doubleValue();
                BigDecimal previousValue = i == 0
                    ? null
                    : (entries.get(i - 1).getValue() == null ? BigDecimal.ZERO : entries.get(i - 1).getValue());

                int barH;
                if (!hasRevenue || safeValue.compareTo(BigDecimal.ZERO) <= 0) {
                    barH = 4;
                } else {
                    barH = Math.max(8, (int) Math.round(chartH * ratio));
                }

                int x = left + (int) Math.round(i * slotWidth + (slotWidth - barW) / 2.0);
                int y = top + chartH - barH;

                Color barColor;
                if (!hasRevenue || safeValue.compareTo(BigDecimal.ZERO) <= 0) {
                    barColor = zeroBar;
                } else {
                    barColor = resolveTrendColor(safeValue, previousValue, upBar, downBar, flatBar);
                }

                g2.setColor(barColor);
                g2.fillRoundRect(x, y, barW, barH, 4, 4);

                if (hasRevenue && i == peakIndex) {
                    g2.setColor(peakOutline);
                    g2.drawRoundRect(x, y, Math.max(1, barW - 1), Math.max(1, barH - 1), 4, 4);
                }

                if (shouldDrawHourLabel(i, labelStep, peakIndex, n)) {
                    int hour = entries.get(i).getKey();
                    g2.setColor(TEXT_SECONDARY);
                    g2.setFont(ThemeFonts.labelSm());
                    String label = formatHour(hour);
                    int tw = g2.getFontMetrics().stringWidth(label);
                    int labelCenter = left + (int) Math.round(i * slotWidth + slotWidth / 2.0);
                    g2.drawString(label, labelCenter - tw / 2, top + chartH + 18);
                }
            }

            if (hasRevenue && peakIndex >= 0 && peakIndex < entries.size()) {
                int peakHour = entries.get(peakIndex).getKey();
                String badge = formatHour(peakHour) + " Peak";
                g2.setFont(ThemeFonts.labelSm().deriveFont(Font.BOLD));
                int tw = g2.getFontMetrics().stringWidth(badge);
                int badgeW = tw + 10;
                int badgeH = 18;

                int peakCenter = left + (int) Math.round(peakIndex * slotWidth + slotWidth / 2.0);
                int x = peakCenter - (badgeW / 2);
                x = Math.max(left, Math.min(x, left + chartW - badgeW));
                int y = Math.max(2, top - badgeH - 6);

                g2.setColor(new Color(0x1C2837));
                g2.fillRoundRect(x, y, badgeW, badgeH, 8, 8);
                g2.setColor(Color.WHITE);
                g2.drawString(badge, x + 5, y + 13);
            } else {
                String emptyText = "No orders in selected period";
                g2.setFont(ThemeFonts.labelMd());
                g2.setColor(TEXT_SECONDARY);
                int tw = g2.getFontMetrics().stringWidth(emptyText);
                g2.drawString(emptyText, left + Math.max(0, (chartW - tw) / 2), top + chartH / 2);
            }

            g2.dispose();
        }

        private boolean shouldDrawHourLabel(int index, int step, int peakIndex, int total) {
            if (index == 0 || index == total - 1 || index == peakIndex) {
                return true;
            }
            return index % Math.max(1, step) == 0;
        }

        private Color resolveTrendColor(BigDecimal current,
                                        BigDecimal previous,
                                        Color upBar,
                                        Color downBar,
                                        Color flatBar) {
            if (previous == null) {
                return flatBar;
            }

            int trend = current.compareTo(previous);
            if (trend > 0) {
                return upBar;
            }
            if (trend < 0) {
                return downBar;
            }
            return flatBar;
        }

        private String formatHour(int hour) {
            int normalized = hour % 12;
            if (normalized == 0) {
                normalized = 12;
            }
            return normalized + (hour >= 12 ? "pm" : "am");
        }
    }

    private static class OrderRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(ThemeFonts.bodyMd());
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            if (column == 5 || column == 6) {
                setHorizontalAlignment(RIGHT);
            } else {
                setHorizontalAlignment(LEFT);
            }

            if (isSelected) {
                setBackground(new Color(0xEAF1F9));
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFAFBFD));
            }
            setForeground(TEXT_PRIMARY);
            return this;
        }
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String status = value == null ? "" : value.toString();
            setHorizontalAlignment(CENTER);
            setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
            setText(status);
            setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            setOpaque(true);

            if (Order.STATUS_PAID.equals(status)) {
                setBackground(STATUS_PAID_BG);
                setForeground(STATUS_PAID_FG);
            } else if (Order.STATUS_OPEN.equals(status)) {
                setBackground(STATUS_OPEN_BG);
                setForeground(STATUS_OPEN_FG);
            } else {
                setBackground(STATUS_CANCEL_BG);
                setForeground(STATUS_CANCEL_FG);
            }

            return this;
        }
    }

    private static class ActionCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD));
            setForeground(TEXT_SECONDARY);
            if (isSelected) {
                setBackground(new Color(0xEAF1F9));
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFAFBFD));
            }
            return this;
        }
    }
}
