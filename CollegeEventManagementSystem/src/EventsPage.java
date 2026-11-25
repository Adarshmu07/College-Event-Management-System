import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EventsPage extends Page {

    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField search = UI.field(18);
    private final JComboBox<String> catBox = new JComboBox<>();
    private final JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"All Status", "Upcoming", "Today", "Completed"});
    private boolean loading = false;

    public EventsPage() {
        model = new DefaultTableModel(new String[]{"ID", "Event", "Category", "Date", "Time", "Venue", "Organizer", "Seats", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UI.styleTable(table);
        sorter = new TableRowSorter<DefaultTableModel>(model);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);
        table.getColumnModel().getColumn(7).setPreferredWidth(150);
        table.getColumnModel().getColumn(7).setCellRenderer(new UI.SeatsRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new UI.Badge());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showParticipants();
            }
        });

        UI.styleCombo(catBox, 160);
        UI.styleCombo(statusBox, 140);
        catBox.addItem("All Categories");

        JPanel filters = UI.flow();
        filters.add(UI.label("Search", Font.BOLD, 12, UI.MUTED));
        filters.add(search);
        filters.add(catBox);
        filters.add(statusBox);

        JPanel actions = UI.flow();
        UI.RoundButton add = UI.primary("+ Add Event");
        UI.RoundButton edit = UI.outline("Edit");
        UI.RoundButton del = UI.danger("Delete");
        UI.RoundButton parts = UI.outline("Participants");
        UI.RoundButton export = UI.outline("Export CSV");
        add.addActionListener(e -> showForm(0));
        edit.addActionListener(e -> {
            int id = selectedId();
            if (id < 0) UI.warn(this, "Select an event from the table first.");
            else showForm(id);
        });
        del.addActionListener(e -> deleteSelected());
        parts.addActionListener(e -> showParticipants());
        export.addActionListener(e -> UI.exportCsv(this, table, "events"));
        del.setEnabled(Db.isAdmin());
        if (!Db.isAdmin()) del.setToolTipText("Only administrators can delete events");
        actions.add(add);
        actions.add(edit);
        actions.add(del);
        actions.add(parts);
        actions.add(export);

        add(UI.toolbar(filters, actions), BorderLayout.NORTH);
        add(UI.tableCard(table), BorderLayout.CENTER);

        UI.onChange(search, this::applyFilter);
        catBox.addActionListener(e -> {
            if (!loading) applyFilter();
        });
        statusBox.addActionListener(e -> {
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
        String cat = (String) catBox.getSelectedItem();
        if (cat != null && !cat.startsWith("All")) fs.add(RowFilter.regexFilter("^" + Pattern.quote(cat) + "$", 2));
        String st = (String) statusBox.getSelectedItem();
        if (st != null && !st.startsWith("All")) fs.add(RowFilter.regexFilter("^" + Pattern.quote(st) + "$", 8));
        sorter.setRowFilter(fs.isEmpty() ? null : RowFilter.<DefaultTableModel, Integer>andFilter(fs));
    }

    @Override
    void refresh() {
        loading = true;
        try {
            String today = LocalDate.now().toString();
            model.setRowCount(0);
            for (Object[] r : Db.query("SELECT e.id, e.name, e.category, e.event_date, e.start_time, e.end_time, v.name, e.organizer, "
                    + "e.max_participants, (SELECT COUNT(*) FROM registrations x WHERE x.event_id = e.id) "
                    + "FROM events e JOIN venues v ON v.id = e.venue_id ORDER BY e.event_date DESC, e.start_time")) {
                String date = Db.str(r[3]);
                int cmp = date.compareTo(today);
                String status = cmp > 0 ? "Upcoming" : (cmp == 0 ? "Today" : "Completed");
                model.addRow(new Object[]{r[0], r[1], r[2], r[3], r[4] + " - " + r[5], r[6], r[7],
                        new UI.Seats(Db.num(r[9]), Db.num(r[8])), status});
            }
            String keep = (String) catBox.getSelectedItem();
            catBox.removeAllItems();
            catBox.addItem("All Categories");
            for (Object[] r : Db.query("SELECT DISTINCT category FROM events WHERE category IS NOT NULL ORDER BY category")) {
                catBox.addItem(Db.str(r[0]));
            }
            if (keep != null) catBox.setSelectedItem(keep);
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        } finally {
            loading = false;
        }
        applyFilter();
    }

    // ---------- Add / edit ----------
    private void showForm(int id) {
        try {
            Object[] ev = null;
            if (id > 0) {
                List<Object[]> r = Db.query("SELECT name, category, event_date, start_time, end_time, venue_id, organizer, "
                        + "description, max_participants FROM events WHERE id=?", id);
                if (r.isEmpty()) return;
                ev = r.get(0);
            }
            JTextField name = UI.field(24);
            JComboBox<String> cat = new JComboBox<>(new String[]{"Technical", "Cultural", "Sports", "Workshop", "Literary", "Social", "Academic", "Career"});
            cat.setEditable(true);
            UI.styleCombo(cat, 200);
            JSpinner date = UI.dateSpinner(ev == null ? LocalDate.now().plusDays(7).toString() : Db.str(ev[2]));
            JSpinner start = UI.timeSpinner(ev == null ? "10:00" : Db.str(ev[3]));
            JSpinner end = UI.timeSpinner(ev == null ? "13:00" : Db.str(ev[4]));
            JComboBox<UI.Item> venue = new JComboBox<>();
            UI.styleCombo(venue, 200);
            for (Object[] v : Db.query("SELECT id, name, capacity FROM venues ORDER BY name")) {
                venue.addItem(new UI.Item(Db.num(v[0]), Db.str(v[1]) + "  (capacity " + Db.num(v[2]) + ")"));
            }
            JTextField organizer = UI.field(24);
            JSpinner max = new JSpinner(new SpinnerNumberModel(50, 1, 5000, 1));
            max.setFont(UI.font(Font.PLAIN, 13));
            JTextArea desc = new JTextArea(3, 24);
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);
            desc.setFont(UI.font(Font.PLAIN, 13));
            desc.setBorder(new javax.swing.border.EmptyBorder(6, 8, 6, 8));
            JScrollPane descScroll = new JScrollPane(desc);
            descScroll.setBorder(new LineBorder(UI.BORDER, 1, true));

            if (ev != null) {
                name.setText(Db.str(ev[0]));
                cat.setSelectedItem(Db.str(ev[1]));
                UI.select(venue, Db.num(ev[5]));
                organizer.setText(Db.str(ev[6]));
                desc.setText(Db.str(ev[7]));
                max.setValue(Db.num(ev[8]));
            }

            JPanel form = UI.formPanel();
            UI.row(form, 0, "Event name", name);
            UI.row(form, 1, "Category", cat);
            UI.row(form, 2, "Date", date);
            UI.row(form, 3, "Start time", start);
            UI.row(form, 4, "End time", end);
            UI.row(form, 5, "Venue", venue);
            UI.row(form, 6, "Organizer", organizer);
            UI.row(form, 7, "Max participants", max);
            UI.row(form, 8, "Description", descScroll);

            UI.formDialog(this, id == 0 ? "Add New Event" : "Edit Event",
                    "Venue clashes and seat limits are checked automatically.", form, () -> {
                try {
                    String n = name.getText().trim();
                    if (n.isEmpty()) {
                        UI.warn(this, "Please enter the event name.");
                        return false;
                    }
                    String d = UI.dateValue(date);
                    String st = UI.timeValue(start);
                    String en = UI.timeValue(end);
                    if (en.compareTo(st) <= 0) {
                        UI.warn(this, "End time must be after the start time.");
                        return false;
                    }
                    if (id == 0 && d.compareTo(LocalDate.now().toString()) < 0) {
                        UI.warn(this, "A new event cannot be dated in the past.");
                        return false;
                    }
                    UI.Item v = (UI.Item) venue.getSelectedItem();
                    if (v == null) {
                        UI.warn(this, "Please add a venue first (Venues screen).");
                        return false;
                    }
                    int mx = (Integer) max.getValue();
                    long cap = Db.count("SELECT capacity FROM venues WHERE id=?", v.id);
                    if (mx > cap) {
                        UI.warn(this, "Max participants (" + mx + ") is more than the venue capacity (" + cap + ").");
                        return false;
                    }
                    if (id > 0) {
                        long reg = Db.count("SELECT COUNT(*) FROM registrations WHERE event_id=?", id);
                        if (mx < reg) {
                            UI.warn(this, "Already " + reg + " students are registered. Max participants cannot be lower.");
                            return false;
                        }
                    }
                    long clash = Db.count("SELECT COUNT(*) FROM events WHERE venue_id=? AND event_date=? AND id<>? "
                            + "AND start_time < ? AND end_time > ?", v.id, d, id, en, st);
                    if (clash > 0) {
                        UI.warn(this, "This venue is already booked for another event at that date and time.");
                        return false;
                    }
                    String c = String.valueOf(cat.getSelectedItem()).trim();
                    if (id == 0) {
                        Db.update("INSERT INTO events(name, category, event_date, start_time, end_time, venue_id, organizer, "
                                        + "description, max_participants) VALUES (?,?,?,?,?,?,?,?,?)",
                                n, c, d, st, en, v.id, organizer.getText().trim(), desc.getText().trim(), mx);
                    } else {
                        Db.update("UPDATE events SET name=?, category=?, event_date=?, start_time=?, end_time=?, venue_id=?, "
                                        + "organizer=?, description=?, max_participants=? WHERE id=?",
                                n, c, d, st, en, v.id, organizer.getText().trim(), desc.getText().trim(), mx, id);
                    }
                    Db.log((id == 0 ? "Created event: " : "Updated event: ") + n);
                    refresh();
                    return true;
                } catch (SQLException ex) {
                    UI.error(this, "Database error: " + ex.getMessage());
                    return false;
                }
            }, id == 0 ? "Add Event" : "Save Changes");
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }

    // ---------- Delete ----------
    private void deleteSelected() {
        int id = selectedId();
        if (id < 0) {
            UI.warn(this, "Select an event from the table first.");
            return;
        }
        try {
            String name = Db.str(model.getValueAt(UI.selectedModelRow(table), 1));
            long regs = Db.count("SELECT COUNT(*) FROM registrations WHERE event_id=?", id);
            String msg = "Delete the event \"" + name + "\"?";
            if (regs > 0) msg += "\n\n" + regs + " student registration(s) for this event will also be removed.";
            if (!UI.confirm(this, msg)) return;
            Db.update("DELETE FROM events WHERE id=?", id);
            Db.log("Deleted event: " + name);
            refresh();
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }

    // ---------- Participants ----------
    private void showParticipants() {
        int id = selectedId();
        if (id < 0) {
            UI.warn(this, "Select an event from the table first.");
            return;
        }
        try {
            List<Object[]> ev = Db.query("SELECT name, max_participants FROM events WHERE id=?", id);
            if (ev.isEmpty()) return;
            String name = Db.str(ev.get(0)[0]);
            int max = Db.num(ev.get(0)[1]);
            List<Object[]> rows = new ArrayList<>();
            int present = 0, ratingSum = 0, ratingCount = 0;
            for (Object[] r : Db.query("SELECT s.roll_no, s.name, s.department, s.year, r.reg_date, r.attendance, r.rating "
                    + "FROM registrations r JOIN students s ON s.id = r.student_id WHERE r.event_id=? ORDER BY s.name", id)) {
                int rating = Db.num(r[6]);
                if ("Present".equals(Db.str(r[5]))) present++;
                if (rating > 0) {
                    ratingSum += rating;
                    ratingCount++;
                }
                rows.add(new Object[]{r[0], r[1], r[2], r[3], r[4], r[5], rating > 0 ? rating + " / 5" : "-"});
            }
            String summary = rows.size() + " of " + max + " seats filled   |   Present: " + present
                    + "   |   Average rating: " + (ratingCount == 0 ? "-" : String.format("%.1f / 5", (double) ratingSum / ratingCount));
            UI.showTable(this, "Participants - " + name, summary,
                    new String[]{"Roll No", "Student", "Department", "Year", "Registered On", "Attendance", "Rating"}, rows, 5);
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }
}
