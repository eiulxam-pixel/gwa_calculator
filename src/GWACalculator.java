import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GWACalculator extends JFrame {
    // --- Palette Constants ---
    private final Color BG_DARK = new Color(10, 10, 10);
    private final Color CARD_DARK = new Color(26, 26, 26);
    private final Color COFFEE_ACCENT = new Color(111, 78, 55);
    private final Color TEXT_MAIN_DARK = new Color(245, 245, 245);
    private final Color TEXT_CREAM = new Color(166, 144, 128);

    private final Color BG_LIGHT = new Color(245, 240, 235);
    private final Color CARD_LIGHT = Color.WHITE;
    private final Color CARAMEL_ACCENT = new Color(160, 110, 70);

    private boolean isDarkMode = true;
    private List<ScaleItemPanel> scaleItems = new ArrayList<>();

    private JTextField nameField, unitsField, gradeField;
    private JTable courseTable;
    private JScrollPane tableScroll;
    private DefaultTableModel tableModel;
    private JLabel gwaDisplay, title, subtitle, priceLabel, scaleTitle;
    private JPanel mainPanel, centerContainer, scaleGrid, footer;
    private RoundedPanel inputCard, tableCard, scaleCard;
    private JButton addBtn, computeBtn, themeToggle, resetBtn;

    private final String DATA_FILE = "gwa_data.txt";

    public GWACalculator() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        setTitle("ACADEMIC PULSE");
        setSize(550, 850);
        setMinimumSize(new Dimension(500, 800));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));
        setContentPane(mainPanel);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        resetBtn = createRoundedButton("🗑", new Color(40, 40, 40), Color.WHITE);
        resetBtn.setPreferredSize(new Dimension(50, 40));
        resetBtn.addActionListener(e -> resetAll());

        JPanel titleGroup = new JPanel(new GridLayout(2, 1));
        titleGroup.setOpaque(false);
        title = new JLabel("ACADEMIC PULSE", SwingConstants.CENTER);
        title.setFont(new Font("Inter", Font.BOLD, 28));
        title.setForeground(TEXT_MAIN_DARK);

        subtitle = new JLabel("GENERAL WEIGHTED AVERAGE TRACKER", SwingConstants.CENTER);
        subtitle.setFont(new Font("Inter", Font.PLAIN, 10));
        subtitle.setForeground(TEXT_CREAM);
        titleGroup.add(title);
        titleGroup.add(subtitle);

        themeToggle = createRoundedButton("☕", COFFEE_ACCENT, Color.WHITE);
        themeToggle.setPreferredSize(new Dimension(50, 40));
        themeToggle.addActionListener(e -> toggleTheme());

        header.add(resetBtn, BorderLayout.WEST);
        header.add(titleGroup, BorderLayout.CENTER);
        header.add(themeToggle, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

        // --- CENTER ---
        centerContainer = new JPanel();
        centerContainer.setOpaque(false);
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        inputCard = new RoundedPanel(25, CARD_DARK);
        inputCard.setLayout(new GridLayout(2, 3, 10, 5));
        inputCard.setMaximumSize(new Dimension(1400, 100));
        inputCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        nameField = createModernField();
        unitsField = createModernField();
        gradeField = createModernField();

        inputCard.add(createMiniLabel("COURSE"));
        inputCard.add(createMiniLabel("UNITS"));
        inputCard.add(createMiniLabel("GRADE"));
        inputCard.add(nameField);
        inputCard.add(unitsField);
        inputCard.add(gradeField);

        centerContainer.add(inputCard);
        centerContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        addBtn = createRoundedButton("ADD SUBJECT", COFFEE_ACCENT, Color.WHITE);
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(200, 45));
        centerContainer.add(addBtn);
        centerContainer.add(Box.createRigidArea(new Dimension(0, 25)));

        scaleTitle = new JLabel("GRADE REFERENCE");
        scaleTitle.setFont(new Font("Inter", Font.BOLD, 12));
        scaleTitle.setForeground(TEXT_CREAM);
        scaleTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerContainer.add(scaleTitle);
        centerContainer.add(Box.createRigidArea(new Dimension(0, 10)));

        scaleCard = new RoundedPanel(25, CARD_DARK);
        scaleCard.setLayout(new BorderLayout());
        scaleCard.setMaximumSize(new Dimension(1400, 80));
        scaleCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        scaleGrid = new JPanel(new GridLayout(1, 4, 10, 0));
        scaleGrid.setOpaque(false);

        String[][] refData = {{"1.0-1.25", "EXC"}, {"1.5-1.75", "VG"}, {"2.0-3.0", "PASS"}, {"5.0", "FAIL"}};
        for (String[] ref : refData) {
            ScaleItemPanel item = new ScaleItemPanel(ref[0], ref[1]);
            scaleItems.add(item);
            scaleGrid.add(item);
        }
        scaleCard.add(scaleGrid);
        centerContainer.add(scaleCard);
        centerContainer.add(Box.createRigidArea(new Dimension(0, 25)));

        tableCard = new RoundedPanel(25, CARD_DARK);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(new String[]{"NAME", "UNITS", "GRADE"}, 0);

        courseTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    Color altColor = isDarkMode ? new Color(32, 32, 32) : new Color(250, 248, 245);
                    Color mainColor = isDarkMode ? CARD_DARK : CARD_LIGHT;
                    c.setBackground(row % 2 == 1 ? altColor : mainColor);
                }
                return c;
            }
        };

        tableScroll = new JScrollPane(courseTable);
        styleTable();

        tableCard.add(tableScroll);
        centerContainer.add(tableCard);
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // --- FOOTER ---
        footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel gwaTextGroup = new JPanel(new GridLayout(2, 1));
        gwaTextGroup.setOpaque(false);
        priceLabel = new JLabel("TOTAL GWA", SwingConstants.CENTER);
        priceLabel.setFont(new Font("Inter", Font.BOLD, 12));
        priceLabel.setForeground(COFFEE_ACCENT);
        gwaDisplay = new JLabel("0.00", SwingConstants.CENTER);
        gwaDisplay.setFont(new Font("Inter", Font.BOLD, 54));
        gwaDisplay.setForeground(TEXT_MAIN_DARK);
        gwaTextGroup.add(priceLabel);
        gwaTextGroup.add(gwaDisplay);

        computeBtn = createRoundedButton("COMPUTE", COFFEE_ACCENT, Color.WHITE);
        computeBtn.setPreferredSize(new Dimension(140, 50));
        computeBtn.setMaximumSize(new Dimension(140, 50));

        footer.add(Box.createHorizontalGlue());
        footer.add(gwaTextGroup);
        footer.add(Box.createHorizontalGlue());
        footer.add(computeBtn);

        mainPanel.add(footer, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addEntry());
        gradeField.addActionListener(e -> addEntry());
        computeBtn.addActionListener(e -> calculateGWA());

        // LOAD DATA ON STARTUP
        loadData();
    }

    private void styleTable() {
        Color cardColor = isDarkMode ? CARD_DARK : CARD_LIGHT;
        Color textColor = isDarkMode ? TEXT_MAIN_DARK : Color.BLACK;
        Color accentColor = isDarkMode ? COFFEE_ACCENT : CARAMEL_ACCENT;
        Color headerTextColor = isDarkMode ? TEXT_CREAM : Color.GRAY;

        courseTable.setBackground(cardColor);
        courseTable.setForeground(textColor);
        courseTable.setRowHeight(35);
        courseTable.setShowGrid(false);
        courseTable.setSelectionBackground(isDarkMode ? new Color(61, 43, 31) : new Color(230, 220, 210));
        courseTable.setSelectionForeground(textColor);
        courseTable.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = courseTable.getTableHeader();
        header.setOpaque(false);
        header.setBackground(cardColor);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = new JLabel(value.toString().toUpperCase(), SwingConstants.CENTER);
                l.setOpaque(true);
                l.setBackground(cardColor);
                l.setForeground(headerTextColor);
                l.setFont(new Font("Inter", Font.BOLD, 11));
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, isDarkMode ? new Color(45, 45, 45) : new Color(230, 230, 230)));
                return l;
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        };

        for (int i = 0; i < 3; i++) courseTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        tableScroll.getViewport().setBackground(cardColor);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI(accentColor, cardColor));
        tableScroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
    }

    // --- DATA PERSISTENCE METHODS ---
    private void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.println(tableModel.getValueAt(i, 0) + "," +
                        tableModel.getValueAt(i, 1) + "," +
                        tableModel.getValueAt(i, 2));
            }
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    tableModel.addRow(new Object[]{parts[0], parts[1], parts[2]});
                }
            }
            calculateGWA();
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        Color bg = isDarkMode ? BG_DARK : BG_LIGHT;
        Color card = isDarkMode ? CARD_DARK : CARD_LIGHT;
        Color text = isDarkMode ? TEXT_MAIN_DARK : Color.DARK_GRAY;
        Color accent = isDarkMode ? COFFEE_ACCENT : CARAMEL_ACCENT;

        mainPanel.setBackground(bg);
        title.setForeground(text);
        scaleTitle.setForeground(isDarkMode ? TEXT_CREAM : Color.GRAY);
        gwaDisplay.setForeground(text);
        priceLabel.setForeground(accent);

        inputCard.setBgColor(card);
        tableCard.setBgColor(card);
        scaleCard.setBgColor(card);

        themeToggle.setText(isDarkMode ? "☕" : "☀️");
        themeToggle.setBackground(accent);
        addBtn.setBackground(accent);
        computeBtn.setBackground(accent);

        Color fieldBg = isDarkMode ? new Color(15, 15, 15) : new Color(240, 240, 240);
        nameField.setBackground(fieldBg);
        unitsField.setBackground(fieldBg);
        gradeField.setBackground(fieldBg);
        nameField.setForeground(text);
        unitsField.setForeground(text);
        gradeField.setForeground(text);

        for (ScaleItemPanel item : scaleItems) item.updateTheme(accent, isDarkMode);

        SwingUtilities.updateComponentTreeUI(this);
        styleTable();
    }

    private void resetAll() {
        int confirm = JOptionPane.showConfirmDialog(this, "CLEAR ALL DATA?", "RESET", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.setRowCount(0);
            gwaDisplay.setText("0.00");
            nameField.setText(""); unitsField.setText(""); gradeField.setText("");
            saveData(); // Save empty state
        }
    }

    private void addEntry() {
        try {
            String name = nameField.getText().trim().toUpperCase();
            int u = Integer.parseInt(unitsField.getText().trim());
            double g = Double.parseDouble(gradeField.getText().trim());
            tableModel.addRow(new Object[]{name.isEmpty() ? "SUBJ" : name, u, g});
            nameField.setText(""); unitsField.setText(""); gradeField.setText("");
            nameField.requestFocus();
            saveData(); // Auto-save after adding
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "INVALID INPUT");
        }
    }

    private void calculateGWA() {
        double tw = 0, tu = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            double u = Double.parseDouble(tableModel.getValueAt(i, 1).toString());
            double g = Double.parseDouble(tableModel.getValueAt(i, 2).toString());
            tw += (g * u); tu += u;
        }
        gwaDisplay.setText(tu > 0 ? String.format("%.2f", tw / tu) : "0.00");
    }

    private JTextField createModernField() {
        JTextField f = new JTextField();
        f.setBackground(new Color(15, 15, 15));
        f.setForeground(TEXT_MAIN_DARK);
        f.setCaretColor(COFFEE_ACCENT);
        f.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return f;
    }

    private JButton createRoundedButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent())/2-2);
                g2.dispose();
            }
        };
        b.setBackground(bg); b.setForeground(fg);
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel createMiniLabel(String s) {
        JLabel l = new JLabel(s);
        l.setFont(new Font("Inter", Font.BOLD, 10));
        l.setForeground(TEXT_CREAM);
        return l;
    }

    class ModernScrollBarUI extends BasicScrollBarUI {
        private final Color thumb;
        private final Color track;
        public ModernScrollBarUI(Color thumb, Color track) { this.thumb = thumb; this.track = track; }
        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(track); g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumb);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 5, thumbBounds.width - 4, thumbBounds.height - 10, 10, 10);
            g2.dispose();
        }
    }

    class ScaleItemPanel extends JPanel {
        private JLabel l1, l2;
        public ScaleItemPanel(String val, String desc) {
            setOpaque(false); setLayout(new GridLayout(2, 1));
            l1 = new JLabel(val, SwingConstants.CENTER);
            l1.setFont(new Font("Inter", Font.BOLD, 11));
            l1.setForeground(COFFEE_ACCENT);
            l2 = new JLabel(desc.toUpperCase(), SwingConstants.CENTER);
            l2.setFont(new Font("Inter", Font.BOLD, 9));
            l2.setForeground(TEXT_CREAM);
            add(l1); add(l2);
        }
        public void updateTheme(Color accent, boolean dark) {
            l1.setForeground(accent); l2.setForeground(dark ? TEXT_CREAM : Color.GRAY);
        }
    }

    class RoundedPanel extends JPanel {
        private int radius; private Color bgColor;
        public RoundedPanel(int r, Color bg) { this.radius = r; this.bgColor = bg; setOpaque(false); }
        public void setBgColor(Color bg) { this.bgColor = bg; repaint(); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor); g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GWACalculator().setVisible(true));
    }
}