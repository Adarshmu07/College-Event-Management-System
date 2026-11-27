import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * PART 1 of 2 - Partner 1: Core, Login, Dashboard and Events
 * Contains: theme, database layer (DB), UI helpers, Login window, Main window,
 *           Dashboard tab and Events tab.
 * Partner 2's file: StudentRegistrationModule.java (Students and Registrations tabs).
 *
 * Both files must be in the same folder, together with sqlite-jdbc.jar.
 *
 * Compile (Windows) : javac -cp ".;sqlite-jdbc.jar" CollegeEventManager.java StudentRegistrationModule.java
 * Run     (Windows) : java  -cp ".;sqlite-jdbc.jar" CollegeEventManager
 * Mac/Linux: use ":" instead of ";" in the classpath.
 *
 * Default login:  admin / admin123
 */
public class CollegeEventManager {

    // ===================== THEME =====================
    static final Color PRIMARY = new Color(41, 98, 255);
    static final Color SUCCESS = new Color(34, 139, 87);
    static final Color DANGER = new Color(200, 55, 55);
    static final Color NEUTRAL = new Color(100, 110, 125);
    static final Color BG = new Color(245, 247, 251);
    static final Font BASE = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font BOLD = new Font("Segoe UI", Font.BOLD, 14);

    // ===================== DATABASE =====================
    static class DB {
        static Connection conn;

        static void init() throws SQLException {
            conn = DriverManager.getConnection("jdbc:sqlite:college_events.db");
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
                st.execute("CREATE TABLE IF NOT EXISTS events ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, category TEXT,"
                        + "event_date TEXT NOT NULL, event_time TEXT, venue TEXT, organizer TEXT,"
                        + "capacity INTEGER DEFAULT 0, description TEXT)");
                st.execute("CREATE TABLE IF NOT EXISTS students ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, roll_no TEXT UNIQUE NOT NULL, name TEXT NOT NULL,"
                        + "department TEXT, year INTEGER, email TEXT, phone TEXT)");
                st.execute("CREATE TABLE IF NOT EXISTS registrations ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,"
                        + "student_id INTEGER NOT NULL REFERENCES students(id) ON DELETE CASCADE,"
                        + "reg_date TEXT, UNIQUE(event_id, student_id))");
            }
        }

        static void bind(PreparedStatement ps, Object[] p) throws SQLException {
            for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
        }

        static List<Object[]> query(String sql, Object... p) {
            List<Object[]> rows = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bind(ps, p);
                try (ResultSet rs = ps.executeQuery()) {
                    int n = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        Object[] r = new Object[n];
                        for (int i = 0; i < n; i++) r[i] = rs.getObject(i + 1);
                        rows.add(r);
                    }
                }
            } catch (SQLException e) {
                error(e.getMessage());
            }
            return rows;
        }

        static boolean update(String sql, Object... p) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                bind(ps, p);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                String m = e.getMessage();
                if (m != null && m.contains("UNIQUE")) m = "This record already exists (duplicate).";
                error(m);
                return false;
            }
        }

        static int scalar(String sql, Object... p) {
            List<Object[]> r = query(sql, p);
            return r.isEmpty() || r.get(0)[0] == null ? 0 : ((Number) r.get(0)[0]).intValue();
        }
    }

    // ===================== UI HELPERS =====================
    interface Refreshable { void refresh(); }

    static void error(String m) { JOptionPane.showMessageDialog(null, m, "Error", JOptionPane.ERROR_MESSAGE); }
    static void info(String m) { JOptionPane.showMessageDialog(null, m, "Done", JOptionPane.INFORMATION_MESSAGE); }
    static boolean confirm(String m) {
        return JOptionPane.showConfirmDialog(null, m, "Please confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    static JButton button(String text, Color c) {
        JButton b = new JButton(text);
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFont(BOLD);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    static JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(26);
        t.setFont(BASE);
        t.getTableHeader().setFont(BOLD);
        t.setSelectionBackground(new Color(200, 218, 255));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        return t;
    }

    static void fill(DefaultTableModel m, List<Object[]> rows) {
        m.setRowCount(0);
        for (Object[] r : rows) m.addRow(r);
    }

    static void addRow(JPanel p, int row, String label, JComponent c) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.gridx = 0; g.gridy = row; g.anchor = GridBagConstraints.WEST;
        JLabel l = new JLabel(label);
        l.setFont(BASE);
        p.add(l, g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        c.setFont(BASE);
        p.add(c, g);
    }

    static JPanel card(String title, String value, Color c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(c);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 34));
        v.setForeground(Color.WHITE);
        JLabel t = new JLabel(title);
        t.setFont(BASE);
        t.setForeground(Color.WHITE);
        p.add(v, BorderLayout.CENTER);
        p.add(t, BorderLayout.SOUTH);
        return p;
    }

    static class Item {
        final int id; final String label;
        Item(int id, String label) { this.id = id; this.label = label; }
        public String toString() { return label; }
    }

    // ===================== DASHBOARD =====================
    static class DashboardPanel extends JPanel implements Refreshable {
        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 0));
        DefaultTableModel m = model("Event", "Category", "Date", "Time", "Venue", "Registered / Capacity");

        DashboardPanel() {
            setLayout(new BorderLayout(10, 15));
            setBorder(new EmptyBorder(15, 15, 15, 15));
            setBackground(BG);
            cards.setOpaque(false);
            add(cards, BorderLayout.NORTH);
            JPanel mid = new JPanel(new BorderLayout(0, 6));
            mid.setOpaque(false);
            JLabel h = new JLabel("Upcoming Events");
            h.setFont(new Font("Segoe UI", Font.BOLD, 18));
            mid.add(h, BorderLayout.NORTH);
            mid.add(new JScrollPane(table(m)), BorderLayout.CENTER);
            add(mid, BorderLayout.CENTER);
            refresh();
        }

        public void refresh() {
            String today = LocalDate.now().toString();
            cards.removeAll();
            cards.add(card("Total Events", "" + DB.scalar("SELECT COUNT(*) FROM events"), PRIMARY));
            cards.add(card("Upcoming Events", "" + DB.scalar("SELECT COUNT(*) FROM events WHERE event_date >= ?", today), SUCCESS));
            cards.add(card("Students", "" + DB.scalar("SELECT COUNT(*) FROM students"), new Color(230, 126, 34)));
            cards.add(card("Registrations", "" + DB.scalar("SELECT COUNT(*) FROM registrations"), new Color(142, 68, 173)));
            cards.revalidate(); cards.repaint();
            List<Object[]> rows = DB.query(
                    "SELECT e.name, e.category, e.event_date, e.event_time, e.venue,"
                    + " (SELECT COUNT(*) FROM registrations r WHERE r.event_id=e.id) || ' / ' || e.capacity"
                    + " FROM events e WHERE e.event_date >= ? ORDER BY e.event_date, e.event_time", today);
            fill(m, rows);
        }
    }

    // ===================== EVENTS =====================
    static class EventsPanel extends JPanel implements Refreshable {
        DefaultTableModel m = model("ID", "Event", "Category", "Date", "Time", "Venue", "Organizer", "Capacity", "Registered");
        JTable t = table(m);
        JTextField name = new JTextField(), date = new JTextField(LocalDate.now().toString()),
                time = new JTextField("10:00"), venue = new JTextField(), organizer = new JTextField(),
                search = new JTextField(20);
        JComboBox<String> category = new JComboBox<>(new String[]{"Technical", "Cultural", "Sports", "Workshop", "Seminar", "Other"});
        JSpinner capacity = new JSpinner(new SpinnerNumberModel(100, 1, 100000, 1));
        JTextArea desc = new JTextArea(4, 20);
        int selectedId = -1;

        EventsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(12, 12, 12, 12));
            setBackground(BG);

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            top.setOpaque(false);
            JLabel sl = new JLabel("Search:"); sl.setFont(BOLD);
            top.add(sl); search.setFont(BASE); top.add(search);
            search.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { refresh(); } });
            add(top, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createTitledBorder("Event Details"));
            form.setBackground(Color.WHITE);
            desc.setLineWrap(true); desc.setWrapStyleWord(true);
            addRow(form, 0, "Event Name *", name);
            addRow(form, 1, "Category", category);
            addRow(form, 2, "Date (YYYY-MM-DD) *", date);
            addRow(form, 3, "Time (HH:MM)", time);
            addRow(form, 4, "Venue", venue);
            addRow(form, 5, "Organizer", organizer);
            addRow(form, 6, "Capacity", capacity);
            addRow(form, 7, "Description", new JScrollPane(desc));

            JPanel btns = new JPanel(new GridLayout(2, 2, 8, 8));
            btns.setOpaque(false);
            JButton add = button("Add", SUCCESS), upd = button("Update", PRIMARY),
                    del = button("Delete", DANGER), clr = button("Clear", NEUTRAL);
            btns.add(add); btns.add(upd); btns.add(del); btns.add(clr);
            JPanel left = new JPanel(new BorderLayout(0, 10));
            left.setOpaque(false);
            left.add(form, BorderLayout.CENTER);
            left.add(btns, BorderLayout.SOUTH);
            left.setPreferredSize(new Dimension(360, 0));
            add(left, BorderLayout.WEST);
            add(new JScrollPane(t), BorderLayout.CENTER);

            add.addActionListener(e -> save(true));
            upd.addActionListener(e -> save(false));
            del.addActionListener(e -> delete());
            clr.addActionListener(e -> clear());
            t.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) load(); });
            refresh();
        }

        boolean validateForm() {
            if (name.getText().trim().isEmpty()) { error("Please enter the event name."); return false; }
            try { LocalDate.parse(date.getText().trim()); }
            catch (DateTimeParseException ex) { error("Date must be in YYYY-MM-DD format (e.g. 2026-11-25)."); return false; }
            if (!time.getText().trim().isEmpty()) {
                try { LocalTime.parse(time.getText().trim()); }
                catch (DateTimeParseException ex) { error("Time must be in HH:MM format (e.g. 14:30)."); return false; }
            }
            return true;
        }

        void save(boolean isNew) {
            if (!isNew && selectedId < 0) { error("Select an event from the table first."); return; }
            if (!validateForm()) return;
            Object[] p = {name.getText().trim(), category.getSelectedItem(), date.getText().trim(), time.getText().trim(),
                    venue.getText().trim(), organizer.getText().trim(), capacity.getValue(), desc.getText().trim()};
            boolean ok;
            if (isNew) {
                ok = DB.update("INSERT INTO events(name,category,event_date,event_time,venue,organizer,capacity,description) VALUES(?,?,?,?,?,?,?,?)", p);
            } else {
                Object[] q = new Object[p.length + 1];
                System.arraycopy(p, 0, q, 0, p.length);
                q[p.length] = selectedId;
                ok = DB.update("UPDATE events SET name=?,category=?,event_date=?,event_time=?,venue=?,organizer=?,capacity=?,description=? WHERE id=?", q);
            }
            if (ok) { refresh(); clear(); info(isNew ? "Event added." : "Event updated."); }
        }

        void delete() {
            if (selectedId < 0) { error("Select an event from the table first."); return; }
            if (confirm("Delete this event and all its registrations?")
                    && DB.update("DELETE FROM events WHERE id=?", selectedId)) { refresh(); clear(); }
        }

        void clear() {
            selectedId = -1;
            t.clearSelection();
            name.setText(""); venue.setText(""); organizer.setText(""); desc.setText("");
            date.setText(LocalDate.now().toString()); time.setText("10:00");
            category.setSelectedIndex(0); capacity.setValue(100);
        }

        void load() {
            int r = t.getSelectedRow();
            if (r < 0) return;
            r = t.convertRowIndexToModel(r);
            selectedId = ((Number) m.getValueAt(r, 0)).intValue();
            name.setText(str(m.getValueAt(r, 1)));
            category.setSelectedItem(str(m.getValueAt(r, 2)));
            date.setText(str(m.getValueAt(r, 3)));
            time.setText(str(m.getValueAt(r, 4)));
            venue.setText(str(m.getValueAt(r, 5)));
            organizer.setText(str(m.getValueAt(r, 6)));
            capacity.setValue(Math.max(1, ((Number) m.getValueAt(r, 7)).intValue()));
            List<Object[]> d = DB.query("SELECT description FROM events WHERE id=?", selectedId);
            desc.setText(d.isEmpty() ? "" : str(d.get(0)[0]));
        }

        public void refresh() {
            String s = "%" + search.getText().trim() + "%";
            fill(m, DB.query("SELECT e.id,e.name,e.category,e.event_date,e.event_time,e.venue,e.organizer,e.capacity,"
                    + "(SELECT COUNT(*) FROM registrations r WHERE r.event_id=e.id) FROM events e "
                    + "WHERE e.name LIKE ? OR e.category LIKE ? OR e.venue LIKE ? OR e.organizer LIKE ? "
                    + "ORDER BY e.event_date, e.event_time", s, s, s, s));
        }
    }

    static String str(Object o) { return o == null ? "" : o.toString(); }

    // ===================== MAIN WINDOW =====================
    static void showMain() {
        JFrame f = new JFrame("College Event Management System");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1150, 700);
        f.setLocationRelativeTo(null);

        JLabel header = new JLabel("  College Event Management System");
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(12, 8, 12, 8));

        DashboardPanel dash = new DashboardPanel();
        EventsPanel ev = new EventsPanel();
        StudentRegistrationModule.StudentsPanel st = new StudentRegistrationModule.StudentsPanel();
        StudentRegistrationModule.RegistrationsPanel rg = new StudentRegistrationModule.RegistrationsPanel();
        Refreshable[] panels = {dash, ev, st, rg};

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(BOLD);
        tabs.addTab("Dashboard", dash);
        tabs.addTab("Events", ev);
        tabs.addTab("Students", st);
        tabs.addTab("Registrations", rg);
        tabs.addChangeListener(e -> panels[tabs.getSelectedIndex()].refresh());

        f.add(header, BorderLayout.NORTH);
        f.add(tabs, BorderLayout.CENTER);
        f.setVisible(true);
    }

    static void showLogin() {
        JFrame f = new JFrame("Login - College Event Management");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(25, 30, 25, 30));
        p.setBackground(Color.WHITE);
        JLabel title = new JLabel("Admin Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(PRIMARY);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(0, 0, 15, 0);
        p.add(title, g);
        JTextField user = new JTextField(15);
        JPasswordField pass = new JPasswordField(15);
        g.gridwidth = 1; g.insets = new Insets(5, 5, 5, 5);
        g.gridy = 1; g.gridx = 0; p.add(new JLabel("Username"), g);
        g.gridx = 1; p.add(user, g);
        g.gridy = 2; g.gridx = 0; p.add(new JLabel("Password"), g);
        g.gridx = 1; p.add(pass, g);
        JButton go = button("Login", PRIMARY);
        g.gridy = 3; g.gridx = 0; g.gridwidth = 2; g.insets = new Insets(15, 5, 5, 5);
        p.add(go, g);
        JLabel hint = new JLabel("Default: admin / admin123", SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);
        g.gridy = 4; g.insets = new Insets(5, 5, 0, 5);
        p.add(hint, g);

        Runnable login = () -> {
            if (user.getText().trim().equals("admin") && new String(pass.getPassword()).equals("admin123")) {
                f.dispose();
                showMain();
            } else error("Wrong username or password.");
        };
        go.addActionListener(e -> login.run());
        pass.addActionListener(e -> login.run());
        user.addActionListener(e -> login.run());
        f.add(p);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setResizable(false);
        f.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(i.getName())) { UIManager.setLookAndFeel(i.getClassName()); break; }
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> {
            try {
                DB.init();
            } catch (SQLException e) {
                error("Database error: " + e.getMessage()
                        + "\n\nMake sure sqlite-jdbc.jar is in the same folder and included in the classpath.");
                System.exit(1);
            }
            showLogin();
        });
    }
}
