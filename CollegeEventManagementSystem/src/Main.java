import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        try {
            Db.init();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Could not open the database:\n" + ex.getMessage()
                            + "\n\nMake sure the sqlite-jdbc jar files are inside the lib folder.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
