package com.oop.project.ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.oop.project.model.Order;
import com.oop.project.service.DashboardService;

public class DashboardPanel extends JPanel {
    private DashboardService dashboardService;
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");
    
    // Analytics components
    private JLabel totalRevenueLabel;
    private JLabel ordersCountLabel;
    private JLabel avgOrderValueLabel;
    
    // Filter components
    private JSpinner dateSpinner;
    private JTextField minPriceField;
    private JTextField maxPriceField;
    private JTextField searchField;
    
    // Table components
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    
    // Best sellers components
    private JLabel topItemsLabel;
    private JLabel topCategoriesLabel;
    private JLabel lastUpdatedLabel;
    
    private List<Order> currentOrders;
    
    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        this.dashboardService = new DashboardService();
        this.currentOrders = new ArrayList<>();
        
        initializeComponents();
        loadDashboardData();
    }

    public void refreshDashboardData() {
        loadDashboardData();
    }
    
    private void initializeComponents() {
        // Top panel - Daily Analytics
        JPanel analyticsPanel = createAnalyticsPanel();
        add(analyticsPanel, BorderLayout.NORTH);
        
        // Center panel - Filters and Orders Table
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        // Left side - Filters
        JPanel filtersPanel = createFiltersPanel();
        centerPanel.add(filtersPanel, BorderLayout.WEST);
        
        // Right side - Orders Table
        JPanel tablePanel = createTablePanel();
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel - Best Sellers
        JPanel bestSellersPanel = createBestSellersPanel();
        add(bestSellersPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createAnalyticsPanel() {
        JPanel analyticsPanel = new JPanel(new BorderLayout(0, 8));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(1, 3, 10, 0));
        contentPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Daily Analytics"));
        
        // Total Revenue
        JPanel revenuePanel = new JPanel(new BorderLayout());
        revenuePanel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK));
        revenuePanel.add(new JLabel("Total Revenue"), BorderLayout.NORTH);
        totalRevenueLabel = new JLabel(formatCurrency(BigDecimal.ZERO));
        totalRevenueLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
        revenuePanel.add(totalRevenueLabel, BorderLayout.CENTER);
        contentPanel.add(revenuePanel);
        
        // Orders Count
        JPanel ordersPanel = new JPanel(new BorderLayout());
        ordersPanel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK));
        ordersPanel.add(new JLabel("Orders"), BorderLayout.NORTH);
        ordersCountLabel = new JLabel("0");
        ordersCountLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
        ordersPanel.add(ordersCountLabel, BorderLayout.CENTER);
        contentPanel.add(ordersPanel);
        
        // Average Order Value
        JPanel avgPanel = new JPanel(new BorderLayout());
        avgPanel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK));
        avgPanel.add(new JLabel("Avg Order Value"), BorderLayout.NORTH);
        avgOrderValueLabel = new JLabel(formatCurrency(BigDecimal.ZERO));
        avgOrderValueLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
        avgPanel.add(avgOrderValueLabel, BorderLayout.CENTER);
        contentPanel.add(avgPanel);

        lastUpdatedLabel = new JLabel("Last updated: -");
        analyticsPanel.add(contentPanel, BorderLayout.CENTER);
        analyticsPanel.add(lastUpdatedLabel, BorderLayout.SOUTH);
        analyticsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Daily Analytics"));
        
        return analyticsPanel;
    }
    
    private JPanel createFiltersPanel() {
        JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new javax.swing.BoxLayout(filtersPanel, javax.swing.BoxLayout.Y_AXIS));
        filtersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Filters"));
        filtersPanel.setMaximumSize(new java.awt.Dimension(150, Integer.MAX_VALUE));
        
        // Date filter
        JLabel dateLabel = new JLabel("Date:");
        filtersPanel.add(dateLabel);
        SpinnerDateModel dateModel = new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        filtersPanel.add(dateSpinner);
        
        filtersPanel.add(javax.swing.Box.createVerticalStrut(10));
        
        // Min Price filter
        JLabel minLabel = new JLabel("Min Price (VND):");
        filtersPanel.add(minLabel);
        minPriceField = new JTextField("0");
        filtersPanel.add(minPriceField);
        
        filtersPanel.add(javax.swing.Box.createVerticalStrut(2));
        
        // Max Price filter
        JLabel maxLabel = new JLabel("Max Price (VND):");
        filtersPanel.add(maxLabel);
        maxPriceField = new JTextField("999999");
        filtersPanel.add(maxPriceField);
        
        filtersPanel.add(javax.swing.Box.createVerticalStrut(2));
        
        // Sort by
        JLabel sortLabel = new JLabel("Sort By:");
        filtersPanel.add(sortLabel);
        javax.swing.JComboBox<String> sortCombo = new javax.swing.JComboBox<>(new String[]{"Time (Latest)", "Time (Oldest)", "Total (High to Low)", "Total (Low to High)"});
        sortCombo.setSelectedIndex(0);
        filtersPanel.add(sortCombo);
        
        filtersPanel.add(javax.swing.Box.createVerticalStrut(10));
        
        // Apply Filters Button
        JButton applyButton = new JButton("Apply Filters");
        applyButton.addActionListener(e -> applyFilters(sortCombo.getSelectedIndex()));
        filtersPanel.add(applyButton);
        
        filtersPanel.add(javax.swing.Box.createVerticalGlue());
        
        return filtersPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Orders"));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchOrders());
        searchPanel.add(searchButton);
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> loadDashboardData());
        searchPanel.add(resetButton);
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"Order ID", "Time", "Staff", "Total", "Items"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(tableModel);
        ordersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createBestSellersPanel() {
        JPanel bestSellersPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        bestSellersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Best Sellers"));
        
        // Top Items
        JPanel topItemsPanel = new JPanel(new BorderLayout());
        topItemsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.GRAY));
        topItemsPanel.add(new JLabel("Top Items (by quantity):"), BorderLayout.NORTH);
        topItemsLabel = new JLabel("Loading...");
        topItemsPanel.add(topItemsLabel, BorderLayout.CENTER);
        bestSellersPanel.add(topItemsPanel);
        
        // Top Categories
        JPanel topCategoriesPanel = new JPanel(new BorderLayout());
        topCategoriesPanel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.GRAY));
        topCategoriesPanel.add(new JLabel("Top Categories (by revenue):"), BorderLayout.NORTH);
        topCategoriesLabel = new JLabel("Loading...");
        topCategoriesPanel.add(topCategoriesLabel, BorderLayout.CENTER);
        bestSellersPanel.add(topCategoriesPanel);
        
        return bestSellersPanel;
    }
    
    private void loadDashboardData() {
        LocalDate today = LocalDate.now();
        
        // Load daily analytics
        Map<String, Object> analytics = dashboardService.getDailyAnalytics(today);
        totalRevenueLabel.setText(formatCurrency((BigDecimal) analytics.get("totalRevenue")));
        ordersCountLabel.setText(String.valueOf(analytics.get("orderCount")));
        avgOrderValueLabel.setText(formatCurrency((BigDecimal) analytics.get("averageOrderValue")));
        
        // Load orders
        currentOrders = dashboardService.getTodaysOrders();
        refreshOrdersTable(currentOrders);
        
        // Load best sellers
        Map<String, Integer> topItems = dashboardService.getBestSellingItems(today);
        Map<String, BigDecimal> topCategories = dashboardService.getBestSellingCategories(today);
        
        topItemsLabel.setText(formatBestSellers(topItems));
        topCategoriesLabel.setText(formatBestSellerCategories(topCategories));
        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Last updated: " + java.time.LocalDateTime.now().withNano(0));
        }
    }
    
    private void applyFilters(int sortByIndex) {
        try {
            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            LocalDate date = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            
            BigDecimal minPrice = parsePrice(minPriceField.getText(), "0");
            BigDecimal maxPrice = parsePrice(maxPriceField.getText(), "999999");
            if (minPrice.compareTo(maxPrice) > 0) {
                javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Min price must be less than or equal to max price",
                    "Invalid Filter",
                    javax.swing.JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            
            List<Order> filtered = dashboardService.filterOrders(date, minPrice, maxPrice);
            
            // Determine sort order
            boolean ascending;
            String sortField;
            switch (sortByIndex) {
                case 1 -> {
                    ascending = true;
                    sortField = "time";
                }
                case 2 -> {
                    ascending = false;
                    sortField = "total";
                }
                case 3 -> {
                    ascending = true;
                    sortField = "total";
                }
                case 0 -> {
                    ascending = false;
                    sortField = "time";
                }
                default -> {
                    ascending = false;
                    sortField = "time";
                }
            }
            
            filtered = dashboardService.sortOrders(filtered, sortField, ascending);
            currentOrders = filtered;
            refreshOrdersTable(filtered);
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Invalid filter values", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private BigDecimal parsePrice(String raw, String defaultValue) {
        String normalized = raw == null ? "" : raw.trim().replace(",", "").replace(" ", "");
        return new BigDecimal(normalized.isEmpty() ? defaultValue : normalized);
    }
    
    private void searchOrders() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadDashboardData();
            return;
        }
        
        List<Order> results = dashboardService.searchOrders(keyword);
        currentOrders = results;
        refreshOrdersTable(results);
    }
    
    private void refreshOrdersTable(List<Order> orders) {
        tableModel.setRowCount(0);
        
        for (Order order : orders) {
            Order orderWithItems = dashboardService.getOrderWithItems(order);
            Object[] row = {
                orderWithItems.getId(),
                orderWithItems.getCreatedAt(),
                orderWithItems.getStaffName(),
                formatCurrency(orderWithItems.getTotal()),
                orderWithItems.getItemCount()
            };
            tableModel.addRow(row);
        }
    }
    
    private String formatBestSellers(Map<String, Integer> items) {
        if (items.isEmpty()) {
            return "No data";
        }
        StringBuilder sb = new StringBuilder("<html>");
        items.entrySet().stream().limit(5).forEach(e -> 
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("<br>")
        );
        sb.append("</html>");
        return sb.toString();
    }
    
    private String formatBestSellerCategories(Map<String, BigDecimal> categories) {
        if (categories.isEmpty()) {
            return "No data";
        }
        StringBuilder sb = new StringBuilder("<html>");
        categories.entrySet().stream().limit(5).forEach(e -> 
            sb.append(e.getKey()).append(": ").append(formatCurrency(e.getValue())).append("<br>")
        );
        sb.append("</html>");
        return sb.toString();
    }

    private String formatCurrency(BigDecimal amount) {
        return priceFormat.format(amount) + " VND";
    }
}
