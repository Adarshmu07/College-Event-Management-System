import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

/** Base class for every screen shown inside the main window. */
abstract class Page extends JPanel {
    Page() {
        super(new BorderLayout());
        setBackground(UI.BG);
        setBorder(new EmptyBorder(22, 26, 22, 26));
    }

    /** Reloads the data shown on this screen. */
    abstract void refresh();
}
