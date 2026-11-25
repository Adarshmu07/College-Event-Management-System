import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportsPage extends Page {

    private static final String[] NAMES = {"Event Summary", "Department Analysis", "Top Students", "Venue Usage"};

    private final CardLayout cards = new CardLayout();
    private final JPanel holder = new JPanel(cards);
    private final DefaultTableModel[] models = new DefaultTableModel[NAMES.length];
    private final JTable[] tables = new JTable[NAMES.length];
    private final UI.RoundButton[] tabButtons = new UI.RoundButton[NAMES.length];
    private int current = 0;

    public ReportsPage() {
        String[][] cols = {
            {"Event", "Category", "Date", "Venue", "Registered", "Capacity", "Fill %", "Present", "Absent", "Attendance %", "Avg Rating"},
            {"Department", "Students", "Registrations", "Avg Events / Student", "Attended"},
            {"Rank", "Student", "Roll No", "Department", "Events Joined", "Attended", "Avg Rating Given"},
            {"Venue", "Capacity", "Events Hosted", "Total Registrations", "Avg Registrations / Event"}
        };

        JPanel bar = UI.flow();
        for (int i = 0; i < NAMES.length; i++) {
            final int idx = i;
            models[i] = new DefaultTableModel(cols[i], 0) {
                @Override
                public boolean isCellEditable(int r, int c) { return false; }
            };
            tables[i] = new JTable(models[i]);
            UI.styleTable(tables[i]);
            tables[i].setRowSorter(new TableRowSorter<DefaultTableModel>(models[i]));
            holder.add(UI.tableCard(tables[i]), NAMES[i]);

            tabButtons[i] = i == 0 ? UI.primary(NAMES[i]) : UI.outline(NAMES[i]);
            tabButtons[i].addActionListener(e -> showTab(idx));
            bar.add(tabButtons[i]);
        }
        UI.RoundButton export = UI.outline("Export This Report");
        export.addActionListener(e -> UI.exportCsv(this, tables[current], NAMES[current].toLowerCase().replace(' ', '_')));
        JPanel exportRow = UI.flow();
        exportRow.add(export);

        add(UI.toolbar(bar, exportRow), BorderLayout.NORTH);
        add(holder, BorderLayout.CENTER);
    }

    private void showTab(int idx) {
        current = idx;
        cards.show(holder, NAMES[idx]);
        for (int i = 0; i < tabButtons.length; i++) tabButtons[i].setOutline(i != idx);
    }

    private static String pct(long part, long whole) {
        return whole <= 0 ? "-" : (part * 100 / whole) + "%";
    }

    @Override
    void refresh() {
        try {
            // 1. Event summary
            models[0].setRowCount(0);
            for (Object[] r : Db.query("SELECT e.name, e.category, e.event_date, v.name, "
                    + "(SELECT COUNT(*) FROM registrations x WHERE x.event_id = e.id), e.max_participants, "
                    + "(SELECT COUNT(*) FROM registrations x WHERE x.event_id = e.id AND x.attendance = 'Present'), "
                    + "(SELECT COUNT(*) FROM registrations x WHERE x.event_id = e.id AND x.attendance = 'Absent'), "
                    + "(SELECT AVG(x.rating) FROM registrations x WHERE x.event_id = e.id AND x.rating > 0) "
                    + "FROM events e JOIN venues v ON v.id = e.venue_id ORDER BY e.event_date DESC")) {
                int reg = Db.num(r[4]), max = Db.num(r[5]), pres = Db.num(r[6]), abs = Db.num(r[7]);
                String rating = r[8] == null ? "-" : String.format("%.1f / 5", ((Number) r[8]).doubleValue());
                models[0].addRow(new Object[]{r[0], r[1], r[2], r[3], reg, max, pct(reg, max), pres, abs, pct(pres, pres + abs), rating});
            }

            // 2. Department analysis
            models[1].setRowCount(0);
            for (Object[] r : Db.query("SELECT s.department, COUNT(DISTINCT s.id), COUNT(r.id), "
                    + "SUM(CASE WHEN r.attendance = 'Present' THEN 1 ELSE 0 END) "
                    + "FROM students s LEFT JOIN registrations r ON r.student_id = s.id "
                    + "GROUP BY s.department ORDER BY COUNT(r.id) DESC")) {
                int students = Db.num(r[1]), regs = Db.num(r[2]);
                String avg = students == 0 ? "-" : String.format("%.1f", (double) regs / students);
                models[1].addRow(new Object[]{r[0], students, regs, avg, Db.num(r[3])});
            }

            // 3. Top students
            models[2].setRowCount(0);
            int rank = 1;
            for (Object[] r : Db.query("SELECT s.name, s.roll_no, s.department, COUNT(r.id), "
                    + "SUM(CASE WHEN r.attendance = 'Present' THEN 1 ELSE 0 END), "
                    + "AVG(CASE WHEN r.rating > 0 THEN r.rating END) "
                    + "FROM students s JOIN registrations r ON r.student_id = s.id "
                    + "GROUP BY s.id ORDER BY 5 DESC, 4 DESC, s.name LIMIT 15")) {
                String rating = r[5] == null ? "-" : String.format("%.1f / 5", ((Number) r[5]).doubleValue());
                models[2].addRow(new Object[]{rank++, r[0], r[1], r[2], Db.num(r[3]), Db.num(r[4]), rating});
            }

            // 4. Venue usage
            models[3].setRowCount(0);
            for (Object[] r : Db.query("SELECT v.name, v.capacity, COUNT(DISTINCT e.id), COUNT(r.id) "
                    + "FROM venues v LEFT JOIN events e ON e.venue_id = v.id "
                    + "LEFT JOIN registrations r ON r.event_id = e.id "
                    + "GROUP BY v.id ORDER BY COUNT(DISTINCT e.id) DESC, v.name")) {
                int hosted = Db.num(r[2]), regs = Db.num(r[3]);
                String avg = hosted == 0 ? "-" : String.format("%.1f", (double) regs / hosted);
                models[3].addRow(new Object[]{r[0], r[1], hosted, regs, avg});
            }
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }
}
