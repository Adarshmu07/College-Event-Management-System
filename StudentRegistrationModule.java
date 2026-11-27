import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.util.List;

/**
 * PART 2 of 2 - Partner 2: Students and Registrations
 * Contains: Students tab (add/update/delete/search students) and
 *           Registrations tab (register, cancel, seat check, CSV export).
 * Extends CollegeEventManager (Partner 1's file) so it can reuse DB, button(), table(),
 * model(), fill(), addRow(), error(), info(), confirm(), Item and the colours.
 * Do not run this file directly; run CollegeEventManager.
 */
public class StudentRegistrationModule extends CollegeEventManager {

    // ===================== STUDENTS =====================
    static class StudentsPanel extends JPanel implements Refreshable {
        DefaultTableModel m = model("ID", "Roll No", "Name", "Department", "Year", "Email", "Phone");
        JTable t = table(m);
        JTextField roll = new JTextField(), name = new JTextField(), dept = new JTextField(),
                email = new JTextField(), phone = new JTextField(), search = new JTextField(20);
        JComboBox<Integer> year = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        int selectedId = -1;

        StudentsPanel() {
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
            form.setBorder(BorderFactory.createTitledBorder("Student Details"));
            form.setBackground(Color.WHITE);
            addRow(form, 0, "Roll No *", roll);
            addRow(form, 1, "Full Name *", name);
            addRow(form, 2, "Department", dept);
            addRow(form, 3, "Year", year);
            addRow(form, 4, "Email", email);
            addRow(form, 5, "Phone", phone);

            JPanel btns = new JPanel(new GridLayout(2, 2, 8, 8));
            btns.setOpaque(false);
            JButton add = button("Add", SUCCESS), upd = button("Update", PRIMARY),
                    del = button("Delete", DANGER), clr = button("Clear", NEUTRAL);
            btns.add(add); btns.add(upd); btns.add(del); btns.add(clr);
            JPanel left = new JPanel(new BorderLayout(0, 10));
            left.setOpaque(false);
            JPanel formWrap = new JPanel(new BorderLayout());
            formWrap.setOpaque(false);
            formWrap.add(form, BorderLayout.NORTH);
            left.add(formWrap, BorderLayout.CENTER);
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

        void save(boolean isNew) {
            if (!isNew && selectedId < 0) { error("Select a student from the table first."); return; }
            if (roll.getText().trim().isEmpty() || name.getText().trim().isEmpty()) {
                error("Roll No and Name are required."); return;
            }
            String em = email.getText().trim();
            if (!em.isEmpty() && !em.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) { error("Please enter a valid email address."); return; }
            Object[] p = {roll.getText().trim(), name.getText().trim(), dept.getText().trim(), year.getSelectedItem(), em, phone.getText().trim()};
            boolean ok;
            if (isNew) {
                ok = DB.update("INSERT INTO students(roll_no,name,department,year,email,phone) VALUES(?,?,?,?,?,?)", p);
            } else {
                Object[] q = new Object[p.length + 1];
                System.arraycopy(p, 0, q, 0, p.length);
                q[p.length] = selectedId;
                ok = DB.update("UPDATE students SET roll_no=?,name=?,department=?,year=?,email=?,phone=? WHERE id=?", q);
            }
            if (ok) { refresh(); clear(); info(isNew ? "Student added." : "Student updated."); }
        }

        void delete() {
            if (selectedId < 0) { error("Select a student from the table first."); return; }
            if (confirm("Delete this student and their registrations?")
                    && DB.update("DELETE FROM students WHERE id=?", selectedId)) { refresh(); clear(); }
        }

        void clear() {
            selectedId = -1;
            t.clearSelection();
            roll.setText(""); name.setText(""); dept.setText(""); email.setText(""); phone.setText("");
            year.setSelectedIndex(0);
        }

        void load() {
            int r = t.getSelectedRow();
            if (r < 0) return;
            r = t.convertRowIndexToModel(r);
            selectedId = ((Number) m.getValueAt(r, 0)).intValue();
            roll.setText(str(m.getValueAt(r, 1)));
            name.setText(str(m.getValueAt(r, 2)));
            dept.setText(str(m.getValueAt(r, 3)));
            Object y = m.getValueAt(r, 4);
            if (y != null) year.setSelectedItem(((Number) y).intValue());
            email.setText(str(m.getValueAt(r, 5)));
            phone.setText(str(m.getValueAt(r, 6)));
        }

        public void refresh() {
            String s = "%" + search.getText().trim() + "%";
            fill(m, DB.query("SELECT id,roll_no,name,department,year,email,phone FROM students "
                    + "WHERE roll_no LIKE ? OR name LIKE ? OR department LIKE ? ORDER BY name", s, s, s));
        }
    }

    // ===================== REGISTRATIONS =====================
    static class RegistrationsPanel extends JPanel implements Refreshable {
        JComboBox<Item> events = new JComboBox<>(), students = new JComboBox<>();
        DefaultTableModel m = model("Reg ID", "Roll No", "Name", "Department", "Year", "Registered On");
        JTable t = table(m);
        JLabel seats = new JLabel(" ");
        boolean loading = false;

        RegistrationsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(12, 12, 12, 12));
            setBackground(BG);

            JPanel top = new JPanel(new GridBagLayout());
            top.setBorder(BorderFactory.createTitledBorder("Register a Student for an Event"));
            top.setBackground(Color.WHITE);
            events.setFont(BASE); students.setFont(BASE);
            addRow(top, 0, "Select Event", events);
            addRow(top, 1, "Select Student", students);
            JButton reg = button("Register Student", SUCCESS);
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 1; g.gridy = 2; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(6, 4, 6, 4);
            top.add(reg, g);
            add(top, BorderLayout.NORTH);

            JPanel mid = new JPanel(new BorderLayout(0, 6));
            mid.setOpaque(false);
            seats.setFont(BOLD);
            mid.add(seats, BorderLayout.NORTH);
            mid.add(new JScrollPane(t), BorderLayout.CENTER);
            add(mid, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            bottom.setOpaque(false);
            JButton cancel = button("Cancel Selected Registration", DANGER);
            JButton export = button("Export List to CSV", PRIMARY);
            bottom.add(cancel); bottom.add(export);
            add(bottom, BorderLayout.SOUTH);

            events.addActionListener(e -> { if (!loading) loadList(); });
            reg.addActionListener(e -> register());
            cancel.addActionListener(e -> cancelReg());
            export.addActionListener(e -> exportCsv());
            refresh();
        }

        public void refresh() {
            loading = true;
            Item prevE = (Item) events.getSelectedItem();
            Item prevS = (Item) students.getSelectedItem();
            events.removeAllItems(); students.removeAllItems();
            for (Object[] r : DB.query("SELECT id, name || '  (' || event_date || ')' FROM events ORDER BY event_date"))
                events.addItem(new Item(((Number) r[0]).intValue(), (String) r[1]));
            for (Object[] r : DB.query("SELECT id, roll_no || ' - ' || name FROM students ORDER BY name"))
                students.addItem(new Item(((Number) r[0]).intValue(), (String) r[1]));
            select(events, prevE); select(students, prevS);
            loading = false;
            loadList();
        }

        void select(JComboBox<Item> box, Item prev) {
            if (prev == null) return;
            for (int i = 0; i < box.getItemCount(); i++)
                if (box.getItemAt(i).id == prev.id) { box.setSelectedIndex(i); return; }
        }

        void loadList() {
            Item ev = (Item) events.getSelectedItem();
            if (ev == null) { m.setRowCount(0); seats.setText("No events available. Add an event first."); return; }
            fill(m, DB.query("SELECT r.id, s.roll_no, s.name, s.department, s.year, r.reg_date FROM registrations r "
                    + "JOIN students s ON s.id=r.student_id WHERE r.event_id=? ORDER BY s.name", ev.id));
            int cap = DB.scalar("SELECT capacity FROM events WHERE id=?", ev.id);
            seats.setText("Registered: " + m.getRowCount() + " / " + cap + "   (Seats left: " + Math.max(0, cap - m.getRowCount()) + ")");
        }

        void register() {
            Item ev = (Item) events.getSelectedItem(), st = (Item) students.getSelectedItem();
            if (ev == null || st == null) { error("Please add at least one event and one student first."); return; }
            int cap = DB.scalar("SELECT capacity FROM events WHERE id=?", ev.id);
            int cur = DB.scalar("SELECT COUNT(*) FROM registrations WHERE event_id=?", ev.id);
            if (cur >= cap) { error("Sorry, this event is full."); return; }
            if (DB.scalar("SELECT COUNT(*) FROM registrations WHERE event_id=? AND student_id=?", ev.id, st.id) > 0) {
                error("This student is already registered for this event."); return;
            }
            if (DB.update("INSERT INTO registrations(event_id,student_id,reg_date) VALUES(?,?,?)", ev.id, st.id, LocalDate.now().toString())) {
                loadList(); info("Student registered successfully.");
            }
        }

        void cancelReg() {
            int r = t.getSelectedRow();
            if (r < 0) { error("Select a registration from the list first."); return; }
            int id = ((Number) m.getValueAt(t.convertRowIndexToModel(r), 0)).intValue();
            if (confirm("Cancel this registration?") && DB.update("DELETE FROM registrations WHERE id=?", id)) loadList();
        }

        void exportCsv() {
            if (m.getRowCount() == 0) { error("There is nothing to export for this event."); return; }
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("event_registrations.csv"));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            try (PrintWriter w = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
                for (int c = 0; c < m.getColumnCount(); c++) w.print((c > 0 ? "," : "") + m.getColumnName(c));
                w.println();
                for (int r = 0; r < m.getRowCount(); r++) {
                    for (int c = 0; c < m.getColumnCount(); c++)
                        w.print((c > 0 ? "," : "") + "\"" + str(m.getValueAt(r, c)).replace("\"", "\"\"") + "\"");
                    w.println();
                }
                info("Exported successfully.");
            } catch (IOException ex) { error("Could not save file: " + ex.getMessage()); }
        }
    }
}
