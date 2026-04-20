package com.oop.project.ui.panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.oop.project.model.OrderDraft;
import com.oop.project.model.OrderItem;
import com.oop.project.repository.OrderRepository;
import com.oop.project.service.OrderService;
import com.oop.project.ui.theme.AppTheme;
import com.oop.project.ui.theme.ThemeFonts;
import com.oop.project.ui.util.ReceiptPrinter;

public class OrderConfirmationDialog extends JDialog {

    // ── Colour palette ───────────────────────────────────────────────────────
    private static final Color GREEN_HEADER   = new Color(0x2E7D32);
    private static final Color GREEN_BTN      = new Color(0x1B8A3C);
    private static final Color GREEN_BTN_HOV  = new Color(0x166B31);
    private static final Color BLUE_STAFF     = new Color(0x1565C0);
    private static final Color BLUE_TOTAL     = new Color(0x1565C0);
    private static final Color DIVIDER        = new Color(0xDDDEE0);
    private static final Color TABLE_HEADER_BG= new Color(0xF5F6F7);
    private static final Color TABLE_HEADER_FG= new Color(0x666B72);
    private static final Color STATUS_BG      = new Color(0xF0F0F0);
    private static final Color OUTLINE_BTN_BG = new Color(0xF5F5F5);

    // ── State ────────────────────────────────────────────────────────────────
    private final OrderDraft orderDraft;
    private final int staffId;
    private final String staffName;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final Runnable onSubmitted;
    private final ReceiptPrinter receiptPrinter = new ReceiptPrinter();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0");

    private int submittedOrderId = -1;
    private boolean submitted = false;

    private JButton submitBtn;
    private JButton printBtn;
    private JButton closeBtn;
    private JLabel  orderIdLabel; // shown under the QR placeholder

    // Random order-id string shown before actual submission
    private final String tempOrderRef = generateOrderRef();

    public OrderConfirmationDialog(
        JFrame parent,
        OrderDraft draft,
        int staffId,
        String staffName,
        OrderService orderService,
        OrderRepository orderRepository,
        Runnable onSubmitted
    ) {
        super(parent, "Order Confirmation", true);
        this.orderDraft      = draft;
        this.staffId         = staffId;
        this.staffName       = staffName;
        this.orderService    = orderService;
        this.orderRepository = orderRepository;
        this.onSubmitted     = onSubmitted;

        setUndecorated(true);           // we draw our own title bar
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(660, 540);
        setLocationRelativeTo(parent);
        buildDialog();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Main layout
    // ════════════════════════════════════════════════════════════════════════
    private void buildDialog() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(DIVIDER, 1));

        root.add(buildTitleBar(),   BorderLayout.NORTH);
        root.add(buildBody(),       BorderLayout.CENTER);
        root.add(buildStatusBar(),  BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ── Green title bar ──────────────────────────────────────────────────────
    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(GREEN_HEADER);
        bar.setPreferredSize(new Dimension(0, 40));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        // Left: icon + title
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));

        JLabel iconLbl = new JLabel("🧾");
        iconLbl.setFont(ThemeFonts.bodyMd().deriveFont(14f));
        iconLbl.setForeground(Color.WHITE);

        JLabel titleLbl = new JLabel("  ORDER CONFIRMATION");
        titleLbl.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD, 13f));
        titleLbl.setForeground(Color.WHITE);

        left.add(iconLbl);
        left.add(titleLbl);

        // Right: X close button
        JButton closeX = new JButton("✕");
        closeX.setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD, 14f));
        closeX.setForeground(Color.WHITE);
        closeX.setBackground(null);
        closeX.setOpaque(false);
        closeX.setBorderPainted(false);
        closeX.setFocusPainted(false);
        closeX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeX.addActionListener(e -> closeDialog());

        bar.add(left,   BorderLayout.WEST);
        bar.add(closeX, BorderLayout.EAST);
        return bar;
    }

    // ── Body: staff header + table + summary + buttons ───────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        body.add(buildStaffHeader(), BorderLayout.NORTH);
        body.add(buildCenterSection(), BorderLayout.CENTER);
        body.add(buildButtonRow(),   BorderLayout.SOUTH);

        return body;
    }

    // ── Staff info row ───────────────────────────────────────────────────────
    private JPanel buildStaffHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, DIVIDER),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        // Left: STAFF NAME
        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

        JLabel staffLblKey = new JLabel("STAFF NAME");
        staffLblKey.setFont(ThemeFonts.labelSm().deriveFont(Font.BOLD));
        staffLblKey.setForeground(TABLE_HEADER_FG);

        JLabel staffLblVal = new JLabel(staffName);
        staffLblVal.setFont(ThemeFonts.bodyLg().deriveFont(Font.BOLD));
        staffLblVal.setForeground(BLUE_STAFF);

        leftCol.add(staffLblKey);
        leftCol.add(Box.createVerticalStrut(2));
        leftCol.add(staffLblVal);

        // Right: EMPLOYEE ID
        JPanel rightCol = new JPanel();
        rightCol.setOpaque(false);
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));

        JLabel idKey = new JLabel("EMPLOYEE ID");
        idKey.setFont(ThemeFonts.labelSm().deriveFont(Font.BOLD));
        idKey.setForeground(TABLE_HEADER_FG);
        idKey.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel idVal = new JLabel("ID: " + String.format("%04d", staffId));
        idVal.setFont(ThemeFonts.bodyMd().deriveFont(Font.BOLD));
        idVal.setForeground(AppTheme.TEXT_PRIMARY);
        idVal.setAlignmentX(Component.RIGHT_ALIGNMENT);
        idVal.setHorizontalAlignment(SwingConstants.RIGHT);

        rightCol.add(idKey);
        rightCol.add(Box.createVerticalStrut(2));
        rightCol.add(idVal);

        panel.add(leftCol,  BorderLayout.WEST);
        panel.add(rightCol, BorderLayout.EAST);
        return panel;
    }

    // ── Center: table + (QR | summary) ──────────────────────────────────────
    private JPanel buildCenterSection() {
        JPanel section = new JPanel(new BorderLayout(0, 0));
        section.setBackground(Color.WHITE);

        section.add(buildItemsTable(),   BorderLayout.CENTER);
        section.add(buildBottomSection(), BorderLayout.SOUTH);

        return section;
    }

    // ── Items table ──────────────────────────────────────────────────────────
    private JScrollPane buildItemsTable() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ITEM DESCRIPTION", "QTY", "UNIT PRICE", "TOTAL"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (OrderItem item : orderDraft.getItems()) {
            String name = item.getMenuItem().getName();
            String custom = item.getCustomizationSummary();
            String desc = (custom == null || custom.isBlank()) ? name : name + " – " + custom;
            model.addRow(new Object[]{
                desc,
                item.getQuantity(),
                priceFormat.format(item.getUnitPrice()),
                priceFormat.format(item.getLineTotal())
            });
        }

        JTable table = new JTable(model);
        table.setEnabled(false);
        table.setFont(ThemeFonts.bodyMd());
        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(DIVIDER);
        table.setBackground(Color.WHITE);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setFont(ThemeFonts.labelSm().deriveFont(Font.BOLD));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_FG);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER));
        header.setReorderingAllowed(false);

        // Column widths & alignment
        int[] widths = {280, 50, 120, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        // Right-align numeric columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : new int[]{1, 2, 3}) {
            table.getColumnModel().getColumn(c).setCellRenderer(rightRenderer);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    // ── Bottom section: QR code area + pricing summary ───────────────────────
    private JPanel buildBottomSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(new Color(0xF8F9FA));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, DIVIDER));

        panel.add(buildQRArea(),      BorderLayout.WEST);
        panel.add(buildPriceSummary(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildQRArea() {
        JPanel qrPanel = new JPanel();
        qrPanel.setOpaque(false);
        qrPanel.setLayout(new BoxLayout(qrPanel, BoxLayout.Y_AXIS));
        qrPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        qrPanel.setPreferredSize(new Dimension(130, 0));

        // QR placeholder drawn with Graphics2D
        JPanel qrBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(0x333333));
                g2.setStroke(new BasicStroke(2f));
                // Outer frame
                int m = 4;
                g2.drawRect(m, m, w - 2 * m - 1, h - 2 * m - 1);
                // Corner squares
                int cs = 14;
                g2.fillRect(m + 4, m + 4, cs, cs);
                g2.fillRect(w - m - 4 - cs, m + 4, cs, cs);
                g2.fillRect(m + 4, h - m - 4 - cs, cs, cs);
                // Inner dots grid
                g2.setColor(new Color(0x444444));
                int dotSize = 4, step = 7;
                for (int row = 0; row < 5; row++) {
                    for (int col = 0; col < 5; col++) {
                        int dx = m + 26 + col * step;
                        int dy = m + 26 + row * step;
                        if ((row + col) % 2 == 0) {
                            g2.fillRect(dx, dy, dotSize, dotSize);
                        }
                    }
                }
                g2.dispose();
            }
        };
        qrBox.setPreferredSize(new Dimension(72, 72));
        qrBox.setMaximumSize(new Dimension(72, 72));
        qrBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        orderIdLabel = new JLabel("ORDER-ID: " + tempOrderRef);
        orderIdLabel.setFont(ThemeFonts.labelSm());
        orderIdLabel.setForeground(AppTheme.TEXT_SECONDARY);
        orderIdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        qrPanel.add(Box.createVerticalGlue());
        qrPanel.add(qrBox);
        qrPanel.add(Box.createVerticalStrut(4));
        qrPanel.add(orderIdLabel);
        qrPanel.add(Box.createVerticalGlue());
        return qrPanel;
    }

    private JPanel buildPriceSummary() {
        BigDecimal subtotal = orderDraft.getSubtotal();
        BigDecimal tax      = orderService.calculateTax(subtotal);
        BigDecimal fee      = orderService.calculateServiceFee(subtotal);
        BigDecimal total    = orderService.calculateTotal(subtotal);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 16));

        panel.add(buildSummaryRow("SUBTOTAL",       priceFormat.format(subtotal) + " VND", false));
        panel.add(Box.createVerticalStrut(6));
        panel.add(buildSummaryRow("TAX (10%)",      priceFormat.format(tax) + " VND", false));
        panel.add(Box.createVerticalStrut(6));
        panel.add(buildSummaryRow("SERVICE (5%)",   priceFormat.format(fee) + " VND", false));
        panel.add(Box.createVerticalStrut(10));

        // TOTAL row
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel totalKey = new JLabel("TOTAL");
        totalKey.setFont(ThemeFonts.bodyLg().deriveFont(Font.BOLD));
        totalKey.setForeground(BLUE_TOTAL);

        JLabel totalVal = new JLabel(priceFormat.format(total) + " VND");
        totalVal.setFont(ThemeFonts.titleLg().deriveFont(Font.BOLD, 20f));
        totalVal.setForeground(BLUE_TOTAL);

        totalRow.add(totalKey, BorderLayout.WEST);
        totalRow.add(totalVal, BorderLayout.EAST);
        panel.add(totalRow);

        return panel;
    }

    private JPanel buildSummaryRow(String key, String value, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel k = new JLabel(key);
        k.setFont(ThemeFonts.labelMd());
        k.setForeground(TABLE_HEADER_FG);

        JLabel v = new JLabel(value);
        v.setFont(bold ? ThemeFonts.bodyMd().deriveFont(Font.BOLD) : ThemeFonts.bodyMd());
        v.setForeground(AppTheme.TEXT_PRIMARY);
        v.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    // ── Button row: CLOSE | PRINT RECEIPT | SUBMIT ORDER ────────────────────
    private JPanel buildButtonRow() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 8, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        closeBtn  = buildOutlineButton("CLOSE");
        printBtn  = buildOutlineButton("🖨  PRINT RECEIPT");
        submitBtn = buildGreenButton("✔  SUBMIT ORDER");

        printBtn.setEnabled(false);

        closeBtn.addActionListener( e -> closeDialog());
        printBtn.addActionListener( e -> printReceipt());
        submitBtn.addActionListener(e -> submitOrder());

        panel.add(closeBtn);
        panel.add(printBtn);
        panel.add(submitBtn);
        return panel;
    }

    private JButton buildOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        btn.setBackground(OUTLINE_BTN_BG);
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DIVIDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return btn;
    }

    private JButton buildGreenButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeFonts.labelMd().deriveFont(Font.BOLD));
        btn.setBackground(GREEN_BTN);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(GREEN_BTN_HOV);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(GREEN_BTN);
            }
        });
        return btn;
    }

    // ── Status bar ───────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(STATUS_BG);
        bar.setPreferredSize(new Dimension(0, 28));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, DIVIDER),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));

        JPanel leftStatus = new JPanel();
        leftStatus.setOpaque(false);
        leftStatus.setLayout(new BoxLayout(leftStatus, BoxLayout.X_AXIS));

        // Green dot
        JLabel dot = new JLabel("●");
        dot.setFont(ThemeFonts.labelSm().deriveFont(10f));
        dot.setForeground(new Color(0x2E7D32));

        JLabel dbLabel = new JLabel("  DATABASE CONNECTED: 127.0.0.1");
        dbLabel.setFont(ThemeFonts.labelSm());
        dbLabel.setForeground(AppTheme.TEXT_SECONDARY);

        leftStatus.add(dot);
        leftStatus.add(dbLabel);

        JLabel rightStatus = new JLabel("READY_STATE: VALIDATED");
        rightStatus.setFont(ThemeFonts.labelSm());
        rightStatus.setForeground(AppTheme.TEXT_SECONDARY);

        bar.add(leftStatus,  BorderLayout.WEST);
        bar.add(rightStatus, BorderLayout.EAST);
        return bar;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Business logic (unchanged)
    // ════════════════════════════════════════════════════════════════════════
    private void submitOrder() {
        try {
            BigDecimal subtotal = orderDraft.getSubtotal();
            BigDecimal tax      = orderService.calculateTax(subtotal);
            BigDecimal fee      = orderService.calculateServiceFee(subtotal);
            BigDecimal total    = orderService.calculateTotal(subtotal);

            submittedOrderId = orderRepository.submitOrder(
                orderDraft, staffId, subtotal, tax, fee, total);

            submitted = true;
            orderIdLabel.setText("ORDER-ID: " + submittedOrderId);

            JOptionPane.showMessageDialog(
                this,
                "Order submitted successfully! Order ID: " + submittedOrderId,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );

            submitBtn.setEnabled(false);
            submitBtn.setBackground(new Color(0xA5D6B0));
            printBtn.setEnabled(true);
            closeBtn.setText("DONE");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to submit order: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void closeDialog() {
        if (submitted && onSubmitted != null) {
            onSubmitted.run();
        }
        dispose();
    }

    private void printReceipt() {
        if (submittedOrderId <= 0) {
            JOptionPane.showMessageDialog(
                this,
                "Please submit the order first before printing.",
                "No Order ID",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        receiptPrinter.printReceipt(orderDraft, submittedOrderId, staffId, staffName);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private static String generateOrderRef() {
        int n = new Random().nextInt(9000) + 1000;
        String suffix = "" + (char)('A' + new Random().nextInt(26))
                          + (char)('A' + new Random().nextInt(26));
        return n + "-" + suffix;
    }

    public boolean isSubmitted()    { return submitted; }
    public int getSubmittedOrderId(){ return submittedOrderId; }
}
