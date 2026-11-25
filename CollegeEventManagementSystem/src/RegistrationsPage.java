import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegistrationsPage extends Page {

    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JComboBox<UI.Item> eventBox = new JComboBox<>();
    private final JComboBox<UI.Item> studentBox = new JComboBox<>();
    private final JLabel info = UI.label(" ", Font.BOLD, 12, UI.ACCENT);
    private final JTextField search = UI.field(16);
    private final JComboBox<String> filterEvent = new JComboBox<>();
    private boolean loading = false;

    public RegistrationsPage() {
        model = new DefaultTableModel(new String[]{"ID", "Event", "Event Date", "Student", "Roll No", "Department", "Registered On", "Attendance", "Rating"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UI.styleTable(table);
        sorter = new TableRowSorter<DefaultTableModel>(model);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(7).setCellRenderer(new UI.Badge());

        // ----- registration form card -----
        UI.styleCombo(eventBox, 300);
        UI.styleCombo(studentBox, 300);
        UI.styleCombo(filterEvent, 220);
        filterEvent.addItem("All Events");
        UI.RoundButton register = UI.success("Register Student");
        register.addActionListener(e -> registerStudent());
        eventBox.addActionListener(e -> {
            if (!loading) updateInfo();
        });

        UI.RoundPanel formCard = new UI.RoundPanel(new BorderLayout(0, 10), Color.WHITE, 16, true);
        formCard.setBorder(new javax.swing.border.EmptyBorder(14, 16, 14, 16));
        formCard.add(UI.label("New Registration", Font.BOLD, 15, UI.TEXT), BorderLayout.NORTH);
        JPanel formRow = UI.flow();
        formRow.add(UI.label("Event", Font.BOLD, 12, UI.MUTED));
        formRow.add(eventBox);
        formRow.add(UI.label("Student", Font.BOLD, 12, UI.MUTED));
        formRow.add(studentBox);
        formRow.add(register);
        formCard.add(formRow, BorderLayout.CENTER);
        formCard.add(info, BorderLayout.SOUTH);
        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.setOpaque(false);
        formWrap.setBorder(new javax.swing.border.EmptyBorder(0, 0, 14, 0));
        formWrap.add(formCard, BorderLayout.CENTER);

        // ----- filters and actions -----
        JPanel filters = UI.flow();
        filters.add(UI.label("Search", Font.BOLD, 12, UI.MUTED));
        filters.add(search);
        filters.add(filterEvent);

        JPanel actions = UI.flow();
        UI.RoundButton present = UI.success("Mark Present");
        UI.RoundButton absent = UI.outline("Mark Absent");
        UI.RoundButton rate = UI.outline("Rate / Feedback");
        UI.RoundButton cancel = UI.danger("Cancel Registration");
        UI.RoundButton export = UI.outline("Export CSV");
        present.addActionListener(e -> markAttendance("Present"));
        absent.addActionListener(e -> markAttendance("Absent"));
        rate.addActionListener(e -> showRating());
        cancel.addActionListener(e -> cancelRegistration());
        export.addActionListener(e -> UI.exportCsv(this, table, "registrations"));
        actions.add(present);
        actions.add(absent);
        actions.add(rate);
        actions.add(cancel);
        actions.add(export);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(formWrap, BorderLayout.NORTH);
        north.add(UI.toolbar(filters, actions), BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(UI.tableCard(table), BorderLayout.CENTER);

        UI.onChange(search, this::applyFilter);
        filterEvent.addActionListener(e -> {
            if (!loading) applyFilter();
        });
    }

    private int selectedId() {
        int r = UI.selectedModelRow(table);
        return r < 0 ? -1 : Db.num(model.getValueAt(r, 0));
    }

    private void applyFilter() {
        List<RowFilter<Object, Object>> fs = new ArrayList<>();
        String q = search.getText().trim();
        if (!q.isEmpty()) fs.add(RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
        String ev = (String) filterEvent.getSelectedItem();
        if (ev != null && !ev.equals("All Events")) fs.add(RowFilter.regexFilter("^" + Pattern.quote(ev) + "$", 1));
        sorter.setRowFilter(fs.isEmpty() ? null : RowFilter.<DefaultTableModel, Integer>andFilter(fs));
    }

    @Override
    void refresh() {
        loading = true;
        try {
            String today = LocalDate.now().toString();

            int keepEvent = UI.selectedId(eventBox);
            int keepStudent = UI.selectedId(studentBox);
            eventBox.removeAllItems();
            for (Object[] r : Db.query("SELECT id, name, event_date FROM events WHERE event_date >= ? ORDER BY event_date, name", today)) {
                eventBox.addItem(new UI.Item(Db.num(r[0]), Db.str(r[1]) + "  (" + Db.str(r[2]) + ")"));
            }
            UI.select(eventBox, keepEvent);
            studentBox.removeAllItems();
            for (Object[] r : Db.query("SELECT id, roll_no, name, department FROM students ORDER BY name")) {
                studentBox.addItem(new UI.Item(Db.num(r[0]), Db.str(r[2]) + "  -  " + Db.str(r[1]) + "  (" + Db.str(r[3]) + ")"));
            }
            UI.select(studentBox, keepStudent);

            String keepFilter = (String) filterEvent.getSelectedItem();
            filterEvent.removeAllItems();
            filterEvent.addItem("All Events");
            for (Object[] r : Db.query("SELECT DISTINCT name FROM events ORDER BY name")) {
                filterEvent.addItem(Db.str(r[0]));
            }
            if (keepFilter != null) filterEvent.setSelectedItem(keepFilter);

            model.setRowCount(0);
            for (Object[] r : Db.query("SELECT r.id, e.name, e.event_date, s.name, s.roll_no, s.department, r.reg_date, r.attendance, r.rating "
                    + "FROM registrations r JOIN events e ON e.id = r.event_id JOIN students s ON s.id = r.student_id "
                    + "ORDER BY r.id DESC")) {
                int rating = Db.num(r[8]);
                model.addRow(new Object[]{r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], rating > 0 ? rating + " / 5" : "-"});
            }
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        } finally {
            loading = false;
        }
        updateInfo();
        applyFilter();
    }

    private void updateInfo() {
        int eid = UI.selectedId(eventBox);
        if (eid < 0) {
            info.setText("No upcoming events available. Add one in the Events screen.");
            return;
        }
        try {
            List<Object[]> r = Db.query("SELECT e.max_participants, (SELECT COUNT(*) FROM registrations x WHERE x.event_id = e.id), "
                    + "e.start_time, e.end_time, v.name FROM events e JOIN venues v ON v.id = e.venue_id WHERE e.id=?", eid);
            if (r.isEmpty()) return;
            int max = Db.num(r.get(0)[0]);
            int used = Db.num(r.get(0)[1]);
            info.setText("Seats filled: " + used + " / " + max + " (" + (max - used) + " left)   |   "
                    + Db.str(r.get(0)[2]) + " - " + Db.str(r.get(0)[3]) + "   |   " + Db.str(r.get(0)[4]));
        } catch (SQLException ex) {
            info.setText(" ");
        }
    }

    // ---------- Register ----------
    private void registerStudent() {
        int eid = UI.selectedId(eventBox);
        int sid = UI.selectedId(studentBox);
        if (eid < 0 || sid < 0) {
            UI.warn(this, "Please choose both an event and a student. Add them first if the lists are empty.");
            return;
        }
        try {
            List<Object[]> ev = Db.query("SELECT name, event_date, start_time, end_time, max_participants FROM events WHERE id=?", eid);
            if (ev.isEmpty()) return;
            String evName = Db.str(ev.get(0)[0]);
            String date = Db.str(ev.get(0)[1]);
            String start = Db.str(ev.get(0)[2]);
            String end = Db.str(ev.get(0)[3]);
            int max = Db.num(ev.get(0)[4]);

            if (date.compareTo(LocalDate.now().toString()) < 0) {
                UI.warn(this, "This event is already over. Registration is closed.");
                return;
            }
            if (Db.count("SELECT COUNT(*) FROM registrations WHERE event_id=? AND student_id=?", eid, sid) > 0) {
                UI.warn(this, "This student is already registered for \"" + evName + "\".");
                return;
            }
            long used = Db.count("SELECT COUNT(*) FROM registrations WHERE event_id=?", eid);
            if (used >= max) {
                UI.warn(this, "\"" + evName + "\" is full (" + used + " / " + max + " seats). Registration is not possible.");
                return;
            }
            long clash = Db.count("SELECT COUNT(*) FROM registrations r JOIN events e ON e.id = r.event_id "
                    + "WHERE r.student_id=? AND e.id<>? AND e.event_date=? AND e.start_time < ? AND e.end_time > ?",
                    sid, eid, date, end, start);
            if (clash > 0) {
                UI.warn(this, "This student is already registered for another event at the same date and time.");
                return;
            }
            Db.update("INSERT INTO registrations(event_id, student_id, reg_date, attendance, rating, feedback) VALUES (?,?,?,'Pending',0,'')",
                    eid, sid, LocalDate.now().toString());
            Db.log("Registered " + studentBox.getSelectedItem().toString().split("  -  ")[0] + " for " + evName);
            refresh();
            UI.info(this, "Registration successful.");
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }

    // ---------- Attendance ----------
    private void markAttendance(String status) {
        int id = selectedId();
        if (id < 0) {
            UI.warn(this, "Select a registration from the table first.");
            return;
        }
        int mr = UI.selectedModelRow(table);
        String date = Db.str(model.getValueAt(mr, 2));
        if (date.compareTo(LocalDate.now().toString()) > 0) {
            UI.warn(this, "Attendance can only be marked on or after the event date (" + date + ").");
            return;
        }
        try {
            if (status.equals("Absent")) {
                Db.update("UPDATE registrations SET attendance='Absent', rating=0, feedback='' WHERE id=?", id);
            } else {
                Db.update("UPDATE registrations SET attendance='Present' WHERE id=?", id);
            }
            Db.log("Marked " + status + ": " + Db.str(model.getValueAt(mr, 3)) + " at " + Db.str(model.getValueAt(mr, 1)));
            refresh();
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }

    // ---------- Rating ----------
    private void showRating() {
        int id = selectedId();
        if (id < 0) {
            UI.warn(this, "Select a registration from the table first.");
            return;
        }
        try {
            List<Object[]> r = Db.query("SELECT attendance, rating, feedback FROM registrations WHERE id=?", id);
            if (r.isEmpty()) return;
            if (!"Present".equals(Db.str(r.get(0)[0]))) {
                UI.warn(this, "Only students marked Present can give a rating. Mark attendance first.");
                return;
            }
            JComboBox<Integer> stars = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
            stars.setFont(UI.font(Font.PLAIN, 13));
            int old = Db.num(r.get(0)[1]);
            stars.setSelectedItem(old > 0 ? old : 5);
            JTextArea fb = new JTextArea(4, 24);
            fb.setLineWrap(true);
            fb.setWrapStyleWord(true);
            fb.setFont(UI.font(Font.PLAIN, 13));
            fb.setBorder(new javax.swing.border.EmptyBorder(6, 8, 6, 8));
            fb.setText(Db.str(r.get(0)[2]));
            JScrollPane fbScroll = new JScrollPane(fb);
            fbScroll.setBorder(new LineBorder(UI.BORDER, 1, true));

            JPanel form = UI.formPanel();
            UI.row(form, 0, "Rating (1 to 5)", stars);
            UI.row(form, 1, "Feedback", fbScroll);
            UI.formDialog(this, "Rate Event", "Collect the student's rating and feedback for this event.", form, () -> {
                try {
                    Db.update("UPDATE registrations SET rating=?, feedback=? WHERE id=?",
                            (Integer) stars.getSelectedItem(), fb.getText().trim(), id);
                    Db.log("Saved feedback for registration #" + id);
                    refresh();
                    return true;
                } catch (SQLException ex) {
                    UI.error(this, "Database error: " + ex.getMessage());
                    return false;
                }
            }, "Save Rating");
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }

    // ---------- Cancel ----------
    private void cancelRegistration() {
        int id = selectedId();
        if (id < 0) {
            UI.warn(this, "Select a registration from the table first.");
            return;
        }
        int mr = UI.selectedModelRow(table);
        String date = Db.str(model.getValueAt(mr, 2));
        if (date.compareTo(LocalDate.now().toString()) < 0) {
            UI.warn(this, "This event is already over, so its registration cannot be cancelled.");
            return;
        }
        if (!UI.confirm(this, "Cancel the registration of " + Db.str(model.getValueAt(mr, 3)) + " for "
                + Db.str(model.getValueAt(mr, 1)) + "?")) return;
        try {
            Db.update("DELETE FROM registrations WHERE id=?", id);
            Db.log("Cancelled registration: " + Db.str(model.getValueAt(mr, 3)) + " from " + Db.str(model.getValueAt(mr, 1)));
            refresh();
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }
}
