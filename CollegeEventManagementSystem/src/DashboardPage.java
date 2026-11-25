import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DashboardPage extends Page {

    private final JLabel vEvents = bigLabel();
    private final JLabel vUpcoming = bigLabel();
    private final JLabel vStudents = bigLabel();
    private final JLabel vRegs = bigLabel();
    private final JLabel vRate = bigLabel();
    private final UI.BarChart bar = new UI.BarChart();
    private final UI.DonutChart donut = new UI.DonutChart();
    private final DefaultTableModel upModel;
    private final DefaultTableModel actModel;

    public DashboardPage(Consumer<String> navigate) {
        upModel = new DefaultTableModel(new String[]{"Event", "Date", "Venue", "Seats"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        actModel = new DefaultTableModel(new String[]{"Time", "User", "Activity"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 0, 16, 0);

        // Row 0: welcome + quick actions
        JPanel welcome = new JPanel(new BorderLayout());
        welcome.setOpaque(false);
        JPanel hello = new JPanel(new GridLayout(2, 1, 0, 2));
        hello.setOpaque(false);
        hello.add(UI.label("Welcome back, " + Db.currentName, Font.BOLD, 22, UI.TEXT));
        hello.add(UI.label("Here is what is happening across your college events.", Font.PLAIN, 13, UI.MUTED));
        JPanel quick = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        quick.setOpaque(false);
        UI.RoundButton q1 = UI.primary("+ New Event");
        UI.RoundButton q2 = UI.outline("+ Add Student");
        UI.RoundButton q3 = UI.outline("Register Student");
        q1.addActionListener(e -> navigate.accept("Events"));
        q2.addActionListener(e -> navigate.accept("Students"));
        q3.addActionListener(e -> navigate.accept("Registrations"));
        quick.add(q1);
        quick.add(q2);
        quick.add(q3);
        welcome.add(hello, BorderLayout.WEST);
        welcome.add(quick, BorderLayout.EAST);
        g.gridy = 0;
        g.weighty = 0;
        body.add(welcome, g);

        // Row 1: stat cards
        JPanel stats = new JPanel(new GridLayout(1, 5, 16, 0));
        stats.setOpaque(false);
        stats.add(stat("Total Events", vEvents, UI.ACCENT));
        stats.add(stat("Upcoming Events", vUpcoming, UI.BLUE));
        stats.add(stat("Students", vStudents, UI.GREEN));
        stats.add(stat("Registrations", vRegs, UI.ORANGE));
        stats.add(stat("Attendance Rate", vRate, UI.PURPLE));
        g.gridy = 1;
        body.add(stats, g);

        // Row 2: charts
        JPanel charts = new JPanel(new GridLayout(1, 2, 16, 0));
        charts.setOpaque(false);
        charts.setPreferredSize(new Dimension(100, 290));
        charts.add(chartCard("Registrations per event (top 8)", bar));
        charts.add(chartCard("Events by category", donut));
        g.gridy = 2;
        g.weighty = 1;
        body.add(charts, g);

        // Row 3: tables
        JPanel tables = new JPanel(new GridLayout(1, 2, 16, 0));
        tables.setOpaque(false);
        tables.setPreferredSize(new Dimension(100, 270));
        JTable upTable = new JTable(upModel);
        UI.styleTable(upTable);
        upTable.setRowHeight(32);
        upTable.getColumnModel().getColumn(3).setCellRenderer(new UI.SeatsRenderer());
        JTable actTable = new JTable(actModel);
        UI.styleTable(actTable);
        actTable.setRowHeight(32);
        actTable.getColumnModel().getColumn(0).setMaxWidth(130);
        actTable.getColumnModel().getColumn(1).setMaxWidth(90);
        tables.add(tableCard("Upcoming events", upTable));
        tables.add(tableCard("Recent activity", actTable));
        g.gridy = 3;
        g.insets = new Insets(0, 0, 0, 0);
        body.add(tables, g);

        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);
    }

    private static JLabel bigLabel() {
        return UI.label("0", Font.BOLD, 30, UI.TEXT);
    }

    private JPanel stat(String title, JLabel value, Color accent) {
        UI.RoundPanel p = new UI.RoundPanel(new GridLayout(2, 1, 0, 4), Color.WHITE, 16, true) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = UI.g2(g);
                int s = 46;
                int x = getWidth() - s - 18;
                int y = (getHeight() - s) / 2;
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35));
                g2.fillOval(x, y, s, s);
                g2.setColor(accent);
                g2.fillOval(x + 15, y + 15, 16, 16);
                g2.dispose();
            }
        };
        p.setBorder(new EmptyBorder(18, 20, 18, 20));
        p.add(UI.label(title, Font.PLAIN, 13, UI.MUTED));
        p.add(value);
        return p;
    }

    private JPanel chartCard(String title, JComponent chart) {
        UI.RoundPanel p = UI.card(new BorderLayout(0, 8));
        p.add(UI.label(title, Font.BOLD, 15, UI.TEXT), BorderLayout.NORTH);
        p.add(chart, BorderLayout.CENTER);
        return p;
    }

    private JPanel tableCard(String title, JTable t) {
        UI.RoundPanel p = new UI.RoundPanel(new BorderLayout(0, 6), Color.WHITE, 16, true);
        p.setBorder(new EmptyBorder(14, 14, 10, 14));
        p.add(UI.label(title, Font.BOLD, 15, UI.TEXT), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    @Override
    void refresh() {
        try {
            String today = LocalDate.now().toString();
            vEvents.setText(String.valueOf(Db.count("SELECT COUNT(*) FROM events")));
            vUpcoming.setText(String.valueOf(Db.count("SELECT COUNT(*) FROM events WHERE event_date >= ?", today)));
            vStudents.setText(String.valueOf(Db.count("SELECT COUNT(*) FROM students")));
            vRegs.setText(String.valueOf(Db.count("SELECT COUNT(*) FROM registrations")));
            long present = Db.count("SELECT COUNT(*) FROM registrations WHERE attendance='Present'");
            long absent = Db.count("SELECT COUNT(*) FROM registrations WHERE attendance='Absent'");
            vRate.setText(present + absent == 0 ? "-" : (present * 100 / (present + absent)) + "%");

            List<String> bl = new ArrayList<>();
            List<Integer> bv = new ArrayList<>();
            for (Object[] r : Db.query("SELECT e.name, COUNT(r.id) AS c FROM events e LEFT JOIN registrations r ON r.event_id = e.id "
                    + "GROUP BY e.id ORDER BY c DESC, e.name LIMIT 8")) {
                bl.add(Db.str(r[0]));
                bv.add(Db.num(r[1]));
            }
            bar.setData(bl, bv);

            List<String> cl = new ArrayList<>();
            List<Integer> cv = new ArrayList<>();
            for (Object[] r : Db.query("SELECT category, COUNT(*) AS c FROM events GROUP BY category ORDER BY c DESC")) {
                cl.add(Db.str(r[0]));
                cv.add(Db.num(r[1]));
            }
            donut.setData(cl, cv);

            upModel.setRowCount(0);
            for (Object[] r : Db.query("SELECT e.name, e.event_date, v.name, "
                    + "(SELECT COUNT(*) FROM registrations x WHERE x.event_id = e.id), e.max_participants "
                    + "FROM events e JOIN venues v ON v.id = e.venue_id WHERE e.event_date >= ? "
                    + "ORDER BY e.event_date, e.start_time LIMIT 6", today)) {
                upModel.addRow(new Object[]{r[0], r[1], r[2], new UI.Seats(Db.num(r[3]), Db.num(r[4]))});
            }

            actModel.setRowCount(0);
            for (Object[] r : Db.query("SELECT ts, username, action FROM activity ORDER BY id DESC LIMIT 6")) {
                actModel.addRow(new Object[]{r[0], r[1], r[2]});
            }
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }
}
