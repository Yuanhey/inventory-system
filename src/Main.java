import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
 
public class Main {
    public static void main(String[] args) {
        try {
            DBConnection.createTableIfMissing();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to the database:\n" + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> new InventoryDashboard().setVisible(true));
    }
}
 