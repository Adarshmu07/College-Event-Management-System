import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StudentsPage extends Page {

    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField search = UI.field(18);
    private final JComboBox<String> deptBox = new JComboBox<>();
    private boolean loading = false;

    public StudentsPage() {
        model = new DefaultTableModel(new String[]{"ID", "Roll No", "Name", "Department", "Year", "Email", "Phone", "Events Joined", "Attended"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UI.styleTable(table);
        sorter = new TableRowSorter<DefaultTableModel>(model);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(170);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showHistory();
            }
        });

        UI.styleCombo(deptBox, 200);
        deptBox.addItem("All Departments");

        JPanel filters = UI.flow();
        filters.add(UI.label("Search", Font.BOLD, 12, UI.MUTED));
        filters.add(search);
        filters.add(deptBox);

        JPanel actions = UI.flow();
        UI.RoundButton add = UI.primary("+ Add Student");
        UI.RoundButton edit = UI.outline("Edit");
        UI.RoundButton del = UI.danger("Delete");
        UI.RoundButton hist = UI.outline("Event History");
        UI.RoundButton export = UI.outline("Export CSV");
        add.addActionListener(e -> showForm(0));
        edit.addActionListener(e -> {
            int id = selectedId();
            if (id < 0) UI.warn(this, "Select a student from the table first.");
            else showForm(id);
        });
        del.addActionListener(e -> deleteSelected());
        hist.addActionListener(e -> showHistory());
        export.addActionListener(e -> UI.exportCsv(this, table, "students"));
        del.setEnabled(Db.isAdmin());
        if (!Db.isAdmin()) del.setToolTipText("Only administrators can delete students");
        actions.add(add);
        actions.add(edit);
        actions.add(del);
        actions.add(hist);
        actions.add(export);

        add(UI.toolbar(filters, actions), BorderLayout.NORTH);
        add(UI.tableCard(table), BorderLayout.CENTER);

        UI.onChange(search, this::applyFilter);
        deptBox.addActionListener(e -> {
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
        String dept = (String) deptBox.getSelectedItem();
        if (dept != null && !dept.startsWith("All")) fs.add(RowFilter.regexFilter("^" + Pattern.quote(dept) + "$", 3));
        sorter.setRowFilter(fs.isEmpty() ? null : RowFilter.<DefaultTableModel, Integer>andFilter(fs));
    }

    @Override
    void refresh() {
        loading = true;
        try {
            model.setRowCount(0);
            for (Object[] r : Db.query("SELECT s.id, s.roll_no, s.name, s.department, s.year, s.email, s.phone, "
                    + "(SELECT COUNT(*) FROM registrations x WHERE x.student_id = s.id), "
                    + "(SELECT COUNT(*) FROM registrations x WHERE x.student_id = s.id AND x.attendance = 'Present') "
                    + "FROM students s ORDER BY s.name")) {
                model.addRow(new Object[]{r[0], r[1], r[2], r[3], r[4], r[5], r[6], Db.num(r[7]), Db.num(r[8])});
            }
            String keep = (String) deptBox.getSelectedItem();
            deptBox.removeAllItems();
            deptBox.addItem("All Departments");
            for (Object[] r : Db.query("SELECT DISTINCT department FROM students WHERE department IS NOT NULL AND department <> '' ORDER BY department")) {
                deptBox.addItem(Db.str(r[0]));
            }
            if (keep != null) deptBox.setSelectedItem(keep);
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
            Object[] st = null;
            if (id > 0) {
                List<Object[]> r = Db.query("SELECT roll_no, name, department, year, email, phone FROM students WHERE id=?", id);
                if (r.isEmpty()) return;
                st = r.get(0);
            }
            JTextField roll = UI.field(22);
            JTextField name = UI.field(22);
            JComboBox<String> dept = new JComboBox<>(new String[]{"Computer Science", "Information Technology", "Electronics",
                    "Mechanical", "Civil", "Business Administration"});
            dept.setEditable(true);
            UI.styleCombo(dept, 200);
            JComboBox<String> year = new JComboBox<>(new String[]{"1st Year", "2nd Year", "3rd Year", "4th Year"});
            UI.styleCombo(year, 200);
            JTextField email = UI.field(22);
            JTextField phone = UI.field(22);
            if (st != null) {
                roll.setText(Db.str(st[0]));
                name.setText(Db.str(st[1]));
                dept.setSelectedItem(Db.str(st[2]));
                year.setSelectedItem(Db.str(st[3]));
                email.setText(Db.str(st[4]));
                phone.setText(Db.str(st[5]));
            }

            JPanel form = UI.formPanel();
            UI.row(form, 0, "Roll number", roll);
            UI.row(form, 1, "Full name", name);
            UI.row(form, 2, "Department", dept);
            UI.row(form, 3, "Year", year);
            UI.row(form, 4, "Email", email);
            UI.row(form, 5, "Phone (10 digits)", phone);

            UI.formDialog(this, id == 0 ? "Add New Student" : "Edit Student",
                    "Roll numbers must be unique.", form, () -> {
                String rn = roll.getText().trim();
                String nm = name.getText().trim();
                String em = email.getText().trim();
                String ph = phone.getText().trim();
                if (rn.isEmpty() || nm.isEmpty()) {
                    UI.warn(this, "Roll number and name are required.");
                    return false;
                }
                if (!em.isEmpty() && !em.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
                    UI.warn(this, "Please enter a valid email address.");
                    return false;
                }
                if (!ph.isEmpty() && !ph.matches("\\d{10}")) {
                    UI.warn(this, "Phone number must be exactly 10 digits.");
                    return false;
                }
                String dp = String.valueOf(dept.getSelectedItem()).trim();
                String yr = String.valueOf(year.getSelectedItem());
                try {
                    if (id == 0) {
                        Db.update("INSERT INTO students(roll_no, name, department, year, email, phone) VALUES (?,?,?,?,?,?)",
                                rn, nm, dp, yr, em, ph);
                    } else {
                        Db.update("UPDATE students SET roll_no=?, name=?, department=?, year=?, email=?, phone=? WHERE id=?",
                                rn, nm, dp, yr, em, ph, id);
                    }
                    Db.log((id == 0 ? "Added student: " : "Updated student: ") + nm + " (" + rn + ")");
                    refresh();
                    return true;
                } catch (SQLException ex) {
                    if (ex.getMessage() != null && ex.getMessage().contains("UNIQUE")) {
                        UI.warn(this, "A student with roll number " + rn + " already exists.");
                    } else {
                        UI.error(this, "Database error: " + ex.getMessage());
                    }
                    return false;
                }
            }, id == 0 ? "Add Student" : "Save Changes");
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }

    // ---------- Delete ----------
    private void deleteSelected() {
        int id = selectedId();
        if (id < 0) {
            UI.warn(this, "Select a student from the table first.");
            return;
        }
        try {
            String name = Db.str(model.getValueAt(UI.selectedModelRow(table), 2));
            long regs = Db.count("SELECT COUNT(*) FROM registrations WHERE student_id=?", id);
            String msg = "Delete the student \"" + name + "\"?";
            if (regs > 0) msg += "\n\n" + regs + " event registration(s) of this student will also be removed.";
            if (!UI.confirm(this, msg)) return;
            Db.update("DELETE FROM students WHERE id=?", id);
            Db.log("Deleted student: " + name);
            refresh();
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }

    // ---------- Event history ----------
    private void showHistory() {
        int id = selectedId();
        if (id < 0) {
            UI.warn(this, "Select a student from the table first.");
            return;
        }
        try {
            String name = Db.str(model.getValueAt(UI.selectedModelRow(table), 2));
            List<Object[]> rows = new ArrayList<>();
            int present = 0;
            for (Object[] r : Db.query("SELECT e.name, e.event_date, v.name, r.attendance, r.rating, r.feedback "
                    + "FROM registrations r JOIN events e ON e.id = r.event_id JOIN venues v ON v.id = e.venue_id "
                    + "WHERE r.student_id=? ORDER BY e.event_date DESC", id)) {
                if ("Present".equals(Db.str(r[3]))) present++;
                int rating = Db.num(r[4]);
                rows.add(new Object[]{r[0], r[1], r[2], r[3], rating > 0 ? rating + " / 5" : "-", r[5]});
            }
            String summary = "Registered for " + rows.size() + " event(s)   |   Attended: " + present;
            UI.showTable(this, "Event History - " + name, summary,
                    new String[]{"Event", "Date", "Venue", "Attendance", "Rating", "Feedback"}, rows, 3);
        } catch (SQLException ex) {
            UI.error(this, "Database error: " + ex.getMessage());
        }
    }
}
