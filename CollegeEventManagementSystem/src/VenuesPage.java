import javax.swing.*;
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

public class VenuesPage extends Page {

    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField search = UI.field(18);

    public VenuesPage() {
        model = new DefaultTableModel(new String[]{"ID", "Venue", "Location", "Capacity", "Facilities", "Events Hosted", "Upcoming"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UI.styleTable(table);
        sorter = new TableRowSorter<DefaultTableModel>(model);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(4).setPreferredWidth(230);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showSchedule();
            }
        });

        JPanel filters = UI.flow();
        filters.add(UI.label("Search", Font.BOLD, 12, UI.MUTED));
        filters.add(search);

        JPanel actions = UI.flow();
        UI.RoundButton add = UI.primary("+ Add Venue");
        UI.RoundButton edit = UI.outline("Edit");
        UI.RoundButton del = UI.danger("Delete");
        UI.RoundButton sched = UI.outline("View Schedule");
        UI.RoundButton export = UI.outline("Export CSV");
        add.addActionListener(e -> showForm(0));
        edit.addActionListener(e -> {
            int id = selectedId();
            if (id < 0) UI.warn(this, "Select a venue from the table first.");
            else showForm(id);
        });
        del.addActionListener(e -> deleteSelected());
        sched.addActionListener(e -> showSchedule());
        export.addActionListener(e -> UI.exportCsv(this, table, "venues"));
        del.setEnabled(Db.isAdmin());
        if (!Db.isAdmin()) del.setToolTipText("Only administrators can delete venues");
        actions.add(add);
        actions.add(edit);
        actions.add(del);
        actions.add(sched);
        actions.add(export);

        add(UI.toolbar(filters, actions), BorderLayout.NORTH);
        add(UI.tableCard(table), BorderLayout.CENTER);

        UI.onChange(search, this::applyFilter);
    }

    private int selectedId() {
        int r = UI.selectedModelRow(table);
        return r < 0 ? -1 : Db.num(model.getValueAt(r, 0));
    }

    private void applyFilter() {
        String q = search.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.<DefaultTableModel, Integer>regexFilter("(?i)" + Pattern.quote(q)));
    }

    @Override
    void refresh() {
        try {
            model.setRowCount(0);
            for (Object[] r : Db.query("SELECT v.id, v.name, v.location, v.capacity, v.facilities, "
                    + "(SELECT COUNT(*) FROM events e WHERE e.venue_id = v.id), "
                    + "(SELECT COUNT(*) FROM events e WHERE e.venue_id = v.id AND e.event_date >= ?) "
                    + "FROM venues v ORDER BY v.name", LocalDate.now().toString())) {
                model.addRow(new Object[]{r[0], r[1], r[2], r[3], r[4], Db.num(r[5]), Db.num(r[6])});
            }
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
        applyFilter();
    }

    // ---------- Add / edit ----------
    private void showForm(int id) {
        try {
            Object[] v = null;
            if (id > 0) {
                List<Object[]> r = Db.query("SELECT name, location, capacity, facilities FROM venues WHERE id=?", id);
                if (r.isEmpty()) return;
                v = r.get(0);
            }
            JTextField name = UI.field(22);
            JTextField location = UI.field(22);
            JSpinner capacity = new JSpinner(new SpinnerNumberModel(100, 1, 100000, 10));
            capacity.setFont(UI.font(Font.PLAIN, 13));
            JTextField facilities = UI.field(22);
            if (v != null) {
                name.setText(Db.str(v[0]));
                location.setText(Db.str(v[1]));
                capacity.setValue(Db.num(v[2]));
                facilities.setText(Db.str(v[3]));
            }

            JPanel form = UI.formPanel();
            UI.row(form, 0, "Venue name", name);
            UI.row(form, 1, "Location", location);
            UI.row(form, 2, "Capacity", capacity);
            UI.row(form, 3, "Facilities", facilities);

            UI.formDialog(this, id == 0 ? "Add New Venue" : "Edit Venue",
                    "Event seat limits can never exceed the venue capacity.", form, () -> {
                String nm = name.getText().trim();
                if (nm.isEmpty()) {
                    UI.warn(this, "Please enter the venue name.");
                    return false;
                }
                int cap = (Integer) capacity.getValue();
                try {
                    if (id > 0) {
                        long biggest = Db.count("SELECT MAX(max_participants) FROM events WHERE venue_id=?", id);
                        if (cap < biggest) {
                            UI.warn(this, "An event at this venue allows " + biggest + " participants. Capacity cannot be lower than that.");
                            return false;
                        }
                        Db.update("UPDATE venues SET name=?, location=?, capacity=?, facilities=? WHERE id=?",
                                nm, location.getText().trim(), cap, facilities.getText().trim(), id);
                    } else {
                        Db.update("INSERT INTO venues(name, location, capacity, facilities) VALUES (?,?,?,?)",
                                nm, location.getText().trim(), cap, facilities.getText().trim());
                    }
                    Db.log((id == 0 ? "Added venue: " : "Updated venue: ") + nm);
                    refresh();
                    return true;
                } catch (SQLException ex) {
                    if (ex.getMessage() != null && ex.getMessage().contains("UNIQUE")) {
                        UI.warn(this, "A venue named \"" + nm + "\" already exists.");
                    } else {
                        UI.error(this, "Database error: " + ex.getMessage());
                    }
                    return false;
                }
            }, id == 0 ? "Add Venue" : "Save Changes");
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }

    // ---------- Delete ----------
    private void deleteSelected() {
        int id = selectedId();
        if (id < 0) {
            UI.warn(this, "Select a venue from the table first.");
            return;
        }
        try {
            String name = Db.str(model.getValueAt(UI.selectedModelRow(table), 1));
            long events = Db.count("SELECT COUNT(*) FROM events WHERE venue_id=?", id);
            if (events > 0) {
                UI.warn(this, "\"" + name + "\" hosts " + events + " event(s). Move or delete those events before removing the venue.");
                return;
            }
            if (!UI.confirm(this, "Delete the venue \"" + name + "\"?")) return;
            Db.update("DELETE FROM venues WHERE id=?", id);
            Db.log("Deleted venue: " + name);
            refresh();
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }

    // ---------- Schedule ----------
    private void showSchedule() {
        int id = selectedId();
        if (id < 0) {
            UI.warn(this, "Select a venue from the table first.");
            return;
        }
        try {
            String name = Db.str(model.getValueAt(UI.selectedModelRow(table), 1));
            List<Object[]> rows = new ArrayList<>();
            String today = LocalDate.now().toString();
            for (Object[] r : Db.query("SELECT e.name, e.event_date, e.start_time, e.end_time, e.organizer, "
                    + "(SELECT COUNT(*) FROM registrations x WHERE x.event_id = e.id), e.max_participants "
                    + "FROM events e WHERE e.venue_id=? ORDER BY e.event_date DESC, e.start_time", id)) {
                String date = Db.str(r[1]);
                int cmp = date.compareTo(today);
                String status = cmp > 0 ? "Upcoming" : (cmp == 0 ? "Today" : "Completed");
                rows.add(new Object[]{r[0], r[1], r[2] + " - " + r[3], r[4], Db.num(r[5]) + " / " + Db.num(r[6]), status});
            }
            UI.showTable(this, "Schedule - " + name, rows.size() + " event(s) booked at this venue",
                    new String[]{"Event", "Date", "Time", "Organizer", "Seats", "Status"}, rows, 5);
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }
}
