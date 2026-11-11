import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
 
public class DBConnection {
 
    // The database login is kept in this file (NOT inside the code),
    // so your password never gets uploaded to GitHub.
    private static final String CONFIG_FILE = "config.properties";
 
    private static Properties loadConfig() throws SQLException {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
        } catch (IOException e) {
            throw new SQLException("Cannot read " + CONFIG_FILE + ".\n"
                    + "Copy config.properties.example to config.properties "
                    + "and fill in your database details.", e);
        }
        return props;
    }
 
    public static Connection getConnection() throws SQLException {
        Properties p = loadConfig();
        return DriverManager.getConnection(
                p.getProperty("db.url"),
                p.getProperty("db.user"),
                p.getProperty("db.password", ""));
    }
 
    // Creates the items table in the cloud database if it isn't there yet.
    // This runs every time the app starts, so you don't need to import database.sql.
    public static void createTableIfMissing() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS items ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(100) NOT NULL, "
                + "category VARCHAR(50) NOT NULL, "
                + "quantity INT NOT NULL DEFAULT 0, "
                + "price DECIMAL(10,2) NOT NULL DEFAULT 0.00)";
        try (Connection con = getConnection();
             Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }
}
