import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public List<Item> getAll() throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items ORDER BY id";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new Item(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")));
            }
        }
        return items;
    }

    public void add(Item item) throws SQLException {
        String sql = "INSERT INTO items (name, category, quantity, price) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getPrice());
            ps.executeUpdate();
        }
    }

    public void update(Item item) throws SQLException {
        String sql = "UPDATE items SET name=?, category=?, quantity=?, price=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getPrice());
            ps.setInt(5, item.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM items WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
