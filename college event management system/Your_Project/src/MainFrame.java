import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, Page> pages = new LinkedHashMap<>();
    private final Map<String, NavButton> navs = new LinkedHashMap<>();
    private final JLabel pageTitle = UI.label("", Font.BOLD, 22, UI.TEXT);

    public MainFrame() {
        setTitle("College Event Management System");
        setSize(1360, 800);
        setMinimumSize(new Dimension(1180, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        pages.put("Dashboard", new DashboardPage(this::show));
        pages.put("Events", new EventsPage());
        pages.put("Students", new StudentsPage());
        pages.put("Registrations", new RegistrationsPage());
        pages.put("Venues", new VenuesPage());
        pages.put("Reports", new ReportsPage());

        content.setBackground(UI.BG);
        for (Map.Entry<String, Page> e : pages.entrySet()) {
            content.add(e.getValue(), e.getKey());
        }

        JPanel main = new JPanel(new BorderLayout());
        main.add(topBar(), BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(sidebar(), BorderLayout.WEST);
        add(main, BorderLayout.CENTER);

        show("Dashboard");
    }

    private void show(String name) {
        cards.show(content, name);
        pages.get(name).refresh();
        pageTitle.setText(name);
        for (Map.Entry<String, NavButton> e : navs.entrySet()) {
            e.getValue().setActive(e.getKey().equals(name));
        }
    }

    private JPanel topBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, UI.BORDER), new EmptyBorder(16, 26, 16, 26)));
        top.add(pageTitle, BorderLayout.WEST);
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
        top.add(UI.label(date, Font.PLAIN, 13, UI.MUTED), BorderLayout.EAST);
        return top;
    }

    private JPanel sidebar() {
        JPanel s = new JPanel(new BorderLayout());
        s.setBackground(UI.SIDEBAR);
        s.setPreferredSize(new Dimension(236, 0));

        JPanel brand = new JPanel(new GridLayout(2, 1, 0, 2));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(26, 24, 22, 16));
        brand.add(UI.label("CEMS", Font.BOLD, 26, Color.WHITE));
        brand.add(UI.label("College Event Manager", Font.PLAIN, 12, new Color(156, 163, 175)));

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(6, 14, 6, 14));
        for (String name : pages.keySet()) {
            NavButton b = new NavButton(name);
            b.addActionListener(e -> show(name));
            navs.put(name, b);
            nav.add(b);
            nav.add(Box.createVerticalStrut(6));
        }

        JPanel user = new JPanel(new BorderLayout(0, 12));
        user.setOpaque(false);
        user.setBorder(new EmptyBorder(16, 20, 22, 20));
        JPanel who = new JPanel(new GridLayout(2, 1, 0, 2));
        who.setOpaque(false);
        who.add(UI.label(Db.currentName, Font.BOLD, 14, Color.WHITE));
        who.add(UI.label(Db.currentRole, Font.PLAIN, 12, new Color(156, 163, 175)));
        UI.RoundButton logout = UI.danger("Logout");
        logout.addActionListener(e -> {
            if (UI.confirm(this, "Do you want to log out?")) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        });
        user.add(who, BorderLayout.CENTER);
        user.add(logout, BorderLayout.SOUTH);

        s.add(brand, BorderLayout.NORTH);
        s.add(nav, BorderLayout.CENTER);
        s.add(user, BorderLayout.SOUTH);
        return s;
    }

    private static class NavButton extends JButton {
        private boolean active;

        NavButton(String text) {
            super(text);
            setFont(UI.font(Font.BOLD, 14));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setPreferredSize(new Dimension(200, 44));
        }

        void setActive(boolean a) {
            active = a;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = UI.g2(g);
            if (active) {
                g2.setColor(UI.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            } else if (getModel().isRollover()) {
                g2.setColor(UI.SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            g2.setFont(getFont());
            g2.setColor(active ? Color.WHITE : new Color(209, 213, 219));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 20, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
