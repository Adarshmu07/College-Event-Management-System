import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.io.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Theme colors, custom-painted components, charts and helper methods shared by every screen. */
public class UI {

    // ---------- Theme ----------
    public static final Color BG = new Color(243, 245, 250);
    public static final Color SIDEBAR = new Color(17, 24, 39);
    public static final Color SIDEBAR_HOVER = new Color(31, 41, 55);
    public static final Color ACCENT = new Color(79, 70, 229);
    public static final Color PURPLE = new Color(139, 92, 246);
    public static final Color TEXT = new Color(31, 41, 55);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color BORDER = new Color(229, 231, 235);
    public static final Color GREEN = new Color(16, 185, 129);
    public static final Color ORANGE = new Color(245, 158, 11);
    public static final Color RED = new Color(239, 68, 68);
    public static final Color BLUE = new Color(59, 130, 246);
    public static final Color STRIPE = new Color(249, 250, 252);
    public static final Color SELECT = new Color(224, 231, 255);
    static final Color[] PALETTE = {
        new Color(79, 70, 229), new Color(16, 185, 129), new Color(245, 158, 11),
        new Color(239, 68, 68), new Color(59, 130, 246), new Color(139, 92, 246),
        new Color(236, 72, 153), new Color(20, 184, 166)
    };

    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    public static Graphics2D g2(Graphics g) {
        Graphics2D x = (Graphics2D) g.create();
        x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        x.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return x;
    }

    public static JLabel label(String text, int style, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    // ---------- Buttons ----------
    public static class RoundButton extends JButton {
        private final Color base;
        private boolean outline;

        public RoundButton(String text, Color base, boolean outline) {
            super(text);
            this.base = base;
            this.outline = outline;
            setFont(font(Font.BOLD, 13));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void setOutline(boolean o) {
            outline = o;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 36, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = UI.g2(g);
            int w = getWidth(), h = getHeight();
            Color textColor;
            if (!isEnabled()) {
                g2.setColor(new Color(229, 231, 235));
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                textColor = new Color(156, 163, 175);
            } else if (outline) {
                boolean hover = getModel().isRollover() || getModel().isPressed();
                g2.setColor(hover ? new Color(base.getRed(), base.getGreen(), base.getBlue(), 28) : Color.WHITE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
                g2.setColor(base);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
                textColor = base;
            } else {
                Color c = base;
                if (getModel().isPressed()) c = base.darker().darker();
                else if (getModel().isRollover()) c = base.darker();
                g2.setColor(c);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                textColor = Color.WHITE;
            }
            g2.setFont(getFont());
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(getText())) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    public static RoundButton primary(String t) { return new RoundButton(t, ACCENT, false); }
    public static RoundButton success(String t) { return new RoundButton(t, GREEN, false); }
    public static RoundButton danger(String t) { return new RoundButton(t, RED, false); }
    public static RoundButton outline(String t) { return new RoundButton(t, ACCENT, true); }

    // ---------- Panels ----------
    public static class RoundPanel extends JPanel {
        private final Color fill;
        private final int arc;
        private final boolean border;

        public RoundPanel(LayoutManager lm, Color fill, int arc, boolean border) {
            super(lm);
            this.fill = fill;
            this.arc = arc;
            this.border = border;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = UI.g2(g);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (border) {
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static RoundPanel card(LayoutManager lm) {
        RoundPanel p = new RoundPanel(lm, Color.WHITE, 16, true);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        return p;
    }

    public static JPanel flow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        return p;
    }

    /** Card with a filter row on top and an action-button row underneath. */
    public static JPanel toolbar(JPanel filters, JPanel actions) {
        RoundPanel card = new RoundPanel(new BorderLayout(0, 10), Color.WHITE, 16, true);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        filters.setOpaque(false);
        actions.setOpaque(false);
        card.add(filters, BorderLayout.NORTH);
        card.add(actions, BorderLayout.SOUTH);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 14, 0));
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    public static RoundPanel tableCard(JTable t) {
        RoundPanel p = new RoundPanel(new BorderLayout(), Color.WHITE, 16, true);
        p.setBorder(new EmptyBorder(6, 6, 6, 6));
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ---------- Inputs ----------
    public static JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        styleField(f);
        return f;
    }

    public static void styleField(JComponent f) {
        f.setFont(font(Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 10, 7, 10)));
    }

    public static void styleCombo(JComboBox<?> c, int width) {
        c.setFont(font(Font.PLAIN, 13));
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(width, 34));
    }

    public static void onChange(JTextField f, Runnable r) {
        f.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        });
    }

    public static JSpinner dateSpinner(String iso) {
        java.util.Date d = java.util.Date.from(LocalDate.parse(iso).atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner s = new JSpinner(new SpinnerDateModel(d, null, null, Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, "yyyy-MM-dd"));
        s.setFont(font(Font.PLAIN, 13));
        return s;
    }

    public static String dateValue(JSpinner s) {
        return ((java.util.Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }

    public static JSpinner timeSpinner(String hhmm) {
        LocalTime t = LocalTime.parse(hhmm);
        java.util.Date d = java.util.Date.from(LocalDate.now().atTime(t).atZone(ZoneId.systemDefault()).toInstant());
        JSpinner s = new JSpinner(new SpinnerDateModel(d, null, null, Calendar.MINUTE));
        s.setEditor(new JSpinner.DateEditor(s, "HH:mm"));
        s.setFont(font(Font.PLAIN, 13));
        return s;
    }

    public static String timeValue(JSpinner s) {
        return ((java.util.Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault())
                .toLocalTime().withSecond(0).withNano(0).toString();
    }

    /** Item for combo boxes: shows a label, remembers a database id. */
    public static class Item {
        public final int id;
        public final String label;

        public Item(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() { return label; }
    }

    public static void select(JComboBox<Item> box, int id) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).id == id) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    public static int selectedId(JComboBox<Item> box) {
        Item it = (Item) box.getSelectedItem();
        return it == null ? -1 : it.id;
    }

    // ---------- Messages ----------
    public static void info(Component p, String msg) { JOptionPane.showMessageDialog(p, msg, "Information", JOptionPane.INFORMATION_MESSAGE); }
    public static void warn(Component p, String msg) { JOptionPane.showMessageDialog(p, msg, "Please check", JOptionPane.WARNING_MESSAGE); }
    public static void error(Component p, String msg) { JOptionPane.showMessageDialog(p, msg, "Error", JOptionPane.ERROR_MESSAGE); }

    public static boolean confirm(Component p, String msg) {
        return JOptionPane.showConfirmDialog(p, msg, "Please confirm", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // ---------- Dialogs ----------
    public static JDialog dialog(Component parent, String title) {
        Window w = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(w, title, Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        return d;
    }

    public static void row(JPanel p, int r, String label, JComponent c) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = r;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(8, 0, 8, 16);
        p.add(label(label, Font.BOLD, 12, MUTED), g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 0, 5, 0);
        p.add(c, g);
    }

    public static JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        return p;
    }

    public static void formDialog(Component parent, String title, String subtitle, JPanel form,
                                  BooleanSupplier onSave, String saveText) {
        JDialog d = dialog(parent, title);
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(22, 26, 20, 26));
        JPanel head = new JPanel(new GridLayout(2, 1, 0, 2));
        head.setOpaque(false);
        head.add(label(title, Font.BOLD, 20, TEXT));
        head.add(label(subtitle, Font.PLAIN, 13, MUTED));
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        foot.setOpaque(false);
        RoundButton cancel = outline("Cancel");
        cancel.addActionListener(e -> d.dispose());
        RoundButton save = primary(saveText);
        save.addActionListener(e -> {
            if (onSave.getAsBoolean()) d.dispose();
        });
        foot.add(cancel);
        foot.add(save);
        root.add(head, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(foot, BorderLayout.SOUTH);
        d.setContentPane(root);
        d.getRootPane().setDefaultButton(save);
        d.pack();
        d.setSize(Math.max(d.getWidth(), 540), d.getHeight());
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }

    /** Read-only table in a popup window, with CSV export. badgeCols are rendered as colored pills. */
    public static void showTable(Component parent, String title, String summary, String[] cols,
                                 List<Object[]> rows, int... badgeCols) {
        JDialog d = dialog(parent, title);
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));
        JPanel head = new JPanel(new GridLayout(2, 1, 0, 2));
        head.setOpaque(false);
        head.add(label(title, Font.BOLD, 18, TEXT));
        head.add(label(summary, Font.PLAIN, 13, MUTED));
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] r : rows) m.addRow(r);
        JTable t = new JTable(m);
        styleTable(t);
        t.setRowSorter(new TableRowSorter<DefaultTableModel>(m));
        for (int b : badgeCols) t.getColumnModel().getColumn(b).setCellRenderer(new Badge());
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        foot.setOpaque(false);
        RoundButton exp = outline("Export CSV");
        exp.addActionListener(e -> exportCsv(d, t, "export"));
        RoundButton close = primary("Close");
        close.addActionListener(e -> d.dispose());
        foot.add(exp);
        foot.add(close);
        root.add(head, BorderLayout.NORTH);
        root.add(tableCard(t), BorderLayout.CENTER);
        root.add(foot, BorderLayout.SOUTH);
        d.setContentPane(root);
        d.setSize(860, 500);
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }

    // ---------- Tables ----------
    public static void styleTable(JTable t) {
        t.setRowHeight(36);
        t.setFont(font(Font.PLAIN, 13));
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(SELECT);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setFocusable(false);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleHeader(t);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, sel, false, r, c);
                l.setBorder(new EmptyBorder(0, 12, 0, 12));
                l.setForeground(TEXT);
                l.setBackground(sel ? SELECT : (r % 2 == 0 ? Color.WHITE : STRIPE));
                l.setOpaque(true);
                return l;
            }
        });
    }

    private static void styleHeader(JTable t) {
        javax.swing.table.JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 40));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, c);
                l.setBackground(new Color(238, 241, 248));
                l.setForeground(MUTED);
                l.setFont(font(Font.BOLD, 12));
                l.setBorder(new EmptyBorder(0, 12, 0, 12));
                l.setOpaque(true);
                return l;
            }
        });
    }

    public static int selectedModelRow(JTable t) {
        int v = t.getSelectedRow();
        return v < 0 ? -1 : t.convertRowIndexToModel(v);
    }

    /** Colored pill for status-like text (Upcoming, Present, Absent...). */
    public static class Badge extends JLabel implements TableCellRenderer {
        private Color pill = MUTED;
        private Color rowBg = Color.WHITE;

        public Badge() {
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(font(Font.BOLD, 11));
            setForeground(Color.WHITE);
        }

        static Color colorFor(String s) {
            switch (s) {
                case "Upcoming": return BLUE;
                case "Today": return ORANGE;
                case "Completed": return MUTED;
                case "Present": return GREEN;
                case "Absent": return RED;
                case "Pending": return ORANGE;
                case "Administrator": return PURPLE;
                default: return ACCENT;
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String s = v == null ? "" : v.toString();
            setText(s);
            pill = colorFor(s);
            rowBg = sel ? SELECT : (r % 2 == 0 ? Color.WHITE : STRIPE);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = UI.g2(g);
            g2.setColor(rowBg);
            g2.fillRect(0, 0, getWidth(), getHeight());
            FontMetrics fm = getFontMetrics(getFont());
            int w = Math.min(getWidth() - 12, fm.stringWidth(getText()) + 26);
            int h = 22;
            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;
            g2.setColor(pill);
            g2.fillRoundRect(x, y, w, h, h, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Value for the "Seats" column: used seats out of capacity. */
    public static class Seats {
        public final int used, max;

        public Seats(int used, int max) {
            this.used = used;
            this.max = max;
        }

        public int pct() { return max <= 0 ? 0 : Math.min(100, used * 100 / max); }

        @Override
        public String toString() { return used + " / " + max; }
    }

    /** Draws a small progress bar plus "used / max" text for a Seats value. */
    public static class SeatsRenderer extends JPanel implements TableCellRenderer {
        private Seats seats;
        private Color rowBg = Color.WHITE;

        public SeatsRenderer() { setOpaque(false); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            seats = (v instanceof Seats) ? (Seats) v : null;
            rowBg = sel ? SELECT : (r % 2 == 0 ? Color.WHITE : STRIPE);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = UI.g2(g);
            g2.setColor(rowBg);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (seats != null) {
                int pad = 12, textW = 62;
                int barW = Math.max(20, getWidth() - pad * 2 - textW);
                int barH = 8;
                int y = (getHeight() - barH) / 2;
                g2.setColor(BORDER);
                g2.fillRoundRect(pad, y, barW, barH, barH, barH);
                int pct = seats.pct();
                Color c = pct >= 90 ? RED : (pct >= 60 ? ORANGE : GREEN);
                g2.setColor(c);
                g2.fillRoundRect(pad, y, Math.max(barH, barW * pct / 100), barH, barH, barH);
                g2.setColor(TEXT);
                g2.setFont(font(Font.PLAIN, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(seats.toString(), pad + barW + 8, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    // ---------- CSV export ----------
    private static String csv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    public static void exportCsv(Component parent, JTable t, String name) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(name + ".csv"));
        if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        try (PrintWriter w = new PrintWriter(f, "UTF-8")) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < t.getColumnCount(); c++) {
                if (c > 0) sb.append(',');
                sb.append(csv(t.getColumnName(c)));
            }
            w.println(sb);
            for (int r = 0; r < t.getRowCount(); r++) {
                sb.setLength(0);
                for (int c = 0; c < t.getColumnCount(); c++) {
                    if (c > 0) sb.append(',');
                    Object v = t.getValueAt(r, c);
                    sb.append(csv(v == null ? "" : v.toString()));
                }
                w.println(sb);
            }
            info(parent, "Exported " + t.getRowCount() + " rows to:\n" + f.getAbsolutePath());
        } catch (IOException ex) {
            error(parent, "Could not save the file: " + ex.getMessage());
        }
    }

    // ---------- Charts ----------
    public static class BarChart extends JPanel {
        private List<String> labels = new ArrayList<>();
        private List<Integer> values = new ArrayList<>();

        public BarChart() { setOpaque(false); }

        public void setData(List<String> l, List<Integer> v) {
            labels = l;
            values = v;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = UI.g2(g);
            int w = getWidth(), h = getHeight();
            g2.setFont(font(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            if (values.isEmpty()) {
                g2.setColor(MUTED);
                g2.drawString("No data yet", 20, h / 2);
                g2.dispose();
                return;
            }
            int left = 34, bottom = 30, top = 18, right = 10;
            int max = 1;
            for (int v : values) max = Math.max(max, v);
            int chartH = h - top - bottom;
            int chartW = w - left - right;
            for (int i = 0; i <= 4; i++) {
                int y = top + chartH - chartH * i / 4;
                g2.setColor(new Color(235, 237, 243));
                g2.drawLine(left, y, w - right, y);
                g2.setColor(MUTED);
                String s = String.valueOf(max * i / 4);
                g2.drawString(s, left - 8 - fm.stringWidth(s), y + 4);
            }
            int n = values.size();
            int slot = chartW / n;
            int barW = Math.min(44, slot * 6 / 10);
            for (int i = 0; i < n; i++) {
                int bh = (int) ((long) chartH * values.get(i) / max);
                int x = left + i * slot + (slot - barW) / 2;
                int y = top + chartH - bh;
                g2.setPaint(new GradientPaint(x, y, ACCENT, x, y + Math.max(bh, 2), PURPLE));
                g2.fillRoundRect(x, y, barW, Math.max(bh, 2), 8, 8);
                g2.setColor(TEXT);
                String vs = String.valueOf(values.get(i));
                g2.drawString(vs, x + (barW - fm.stringWidth(vs)) / 2, y - 4);
                String lab = labels.get(i);
                if (lab.length() > 8) lab = lab.substring(0, 7) + "..";
                g2.setColor(MUTED);
                g2.drawString(lab, x + (barW - fm.stringWidth(lab)) / 2, h - bottom + 16);
            }
            g2.dispose();
        }
    }

    public static class DonutChart extends JPanel {
        private List<String> labels = new ArrayList<>();
        private List<Integer> values = new ArrayList<>();

        public DonutChart() { setOpaque(false); }

        public void setData(List<String> l, List<Integer> v) {
            labels = l;
            values = v;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = UI.g2(g);
            int w = getWidth(), h = getHeight();
            int total = 0;
            for (int v : values) total += v;
            if (total == 0) {
                g2.setColor(MUTED);
                g2.setFont(font(Font.PLAIN, 11));
                g2.drawString("No data yet", 20, h / 2);
                g2.dispose();
                return;
            }
            int d = Math.max(60, Math.min(h - 10, w / 2));
            int x = 10, y = (h - d) / 2;
            double start = 90;
            for (int i = 0; i < values.size(); i++) {
                double ext = -360.0 * values.get(i) / total;
                g2.setColor(PALETTE[i % PALETTE.length]);
                g2.fill(new Arc2D.Double(x, y, d, d, start, ext, Arc2D.PIE));
                start += ext;
            }
            int hole = d * 60 / 100;
            g2.setColor(Color.WHITE);
            g2.fillOval(x + (d - hole) / 2, y + (d - hole) / 2, hole, hole);
            g2.setColor(TEXT);
            g2.setFont(font(Font.BOLD, 22));
            FontMetrics fm = g2.getFontMetrics();
            String t = String.valueOf(total);
            g2.drawString(t, x + (d - fm.stringWidth(t)) / 2, y + d / 2 + 2);
            g2.setColor(MUTED);
            g2.setFont(font(Font.PLAIN, 11));
            fm = g2.getFontMetrics();
            String sub = "events";
            g2.drawString(sub, x + (d - fm.stringWidth(sub)) / 2, y + d / 2 + 18);
            int lx = x + d + 24;
            int ly = Math.max(14, (h - values.size() * 24) / 2);
            for (int i = 0; i < values.size(); i++) {
                g2.setColor(PALETTE[i % PALETTE.length]);
                g2.fillRoundRect(lx, ly + i * 24, 12, 12, 4, 4);
                g2.setColor(TEXT);
                g2.setFont(font(Font.PLAIN, 12));
                g2.drawString(labels.get(i) + "  (" + values.get(i) + ")", lx + 20, ly + i * 24 + 11);
            }
            g2.dispose();
        }
    }
}
