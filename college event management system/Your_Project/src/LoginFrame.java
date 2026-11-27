import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("College Event Management System");
        setSize(880, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(brandPanel());
        root.add(formPanel());
        setContentPane(root);
    }

    private JPanel brandPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = UI.g2(g);
                g2.setPaint(new GradientPaint(0, 0, new Color(55, 48, 163), getWidth(), getHeight(), new Color(124, 58, 237)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillOval(-80, getHeight() - 220, 320, 320);
                g2.fillOval(getWidth() - 160, -90, 260, 260);
                g2.setColor(Color.WHITE);
                g2.setFont(UI.font(Font.BOLD, 16));
                g2.drawString("CEMS", 48, 70);
                g2.setFont(UI.font(Font.BOLD, 36));
                g2.drawString("College Event", 48, 190);
                g2.drawString("Management", 48, 236);
                g2.setFont(UI.font(Font.PLAIN, 15));
                g2.setColor(new Color(224, 231, 255));
                String[] points = {
                    "Plan events and manage venues",
                    "Register students with seat limits",
                    "Track attendance and feedback",
                    "Live dashboard and reports"
                };
                for (int i = 0; i < points.length; i++) {
                    g2.drawString("-  " + points[i], 48, 300 + i * 28);
                }
                g2.dispose();
            }
        };
    }

    private JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(30, 50, 30, 50));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        JTextField user = UI.field(18);
        JPasswordField pass = new JPasswordField(18);
        UI.styleField(pass);
        char defaultEcho = pass.getEchoChar();
        JCheckBox show = new JCheckBox("Show password");
        show.setOpaque(false);
        show.setFont(UI.font(Font.PLAIN, 12));
        show.setFocusPainted(false);
        show.addActionListener(e -> pass.setEchoChar(show.isSelected() ? (char) 0 : defaultEcho));
        JLabel error = UI.label(" ", Font.PLAIN, 12, UI.RED);
        UI.RoundButton login = UI.primary("Sign In");
        JLabel hint = UI.label("Admin: admin / admin123     Staff: staff / staff123", Font.PLAIN, 11, UI.MUTED);

        int y = 0;
        g.gridy = y++;
        g.insets = new Insets(0, 0, 4, 0);
        p.add(UI.label("Welcome back", Font.BOLD, 28, UI.TEXT), g);
        g.gridy = y++;
        g.insets = new Insets(0, 0, 26, 0);
        p.add(UI.label("Sign in to manage your college events", Font.PLAIN, 13, UI.MUTED), g);
        g.gridy = y++;
        g.insets = new Insets(0, 0, 6, 0);
        p.add(UI.label("Username", Font.BOLD, 12, UI.MUTED), g);
        g.gridy = y++;
        g.insets = new Insets(0, 0, 14, 0);
        p.add(user, g);
        g.gridy = y++;
        g.insets = new Insets(0, 0, 6, 0);
        p.add(UI.label("Password", Font.BOLD, 12, UI.MUTED), g);
        g.gridy = y++;
        g.insets = new Insets(0, 0, 6, 0);
        p.add(pass, g);
        g.gridy = y++;
        g.insets = new Insets(0, 0, 6, 0);
        p.add(show, g);
        g.gridy = y++;
        g.insets = new Insets(0, 0, 10, 0);
        p.add(error, g);
        g.gridy = y++;
        g.insets = new Insets(0, 0, 16, 0);
        p.add(login, g);
        g.gridy = y++;
        g.insets = new Insets(0, 0, 0, 0);
        p.add(hint, g);

        Runnable doLogin = () -> {
            String u = user.getText().trim();
            String pw = new String(pass.getPassword());
            if (u.isEmpty() || pw.isEmpty()) {
                error.setText("Please enter username and password.");
                return;
            }
            try {
                if (Db.login(u, pw)) {
                    dispose();
                    SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
                } else {
                    error.setText("Invalid username or password.");
                }
            } catch (SQLException ex) {
                error.setText("Database error: " + ex.getMessage());
            }
        };
        login.addActionListener(e -> doLogin.run());
        pass.addActionListener(e -> doLogin.run());
        user.addActionListener(e -> pass.requestFocusInWindow());
        return p;
    }
}
