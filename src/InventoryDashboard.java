import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class InventoryDashboard extends JFrame {

    private static final int LOW_STOCK_LIMIT = 5;

    private final ItemDAO dao = new ItemDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Category", "Quantity", "Price"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final CardLayout tableCards = new CardLayout();
    private final JPanel tableContent = new JPanel(tableCards);

    private final JTextField nameField = new JTextField(15);
    private final JTextField categoryField = new JTextField(15);
    private final JTextField quantityField = new JTextField(15);
    private final JTextField priceField = new JTextField(15);

    private final JLabel totalItemsLbl = new JLabel("0");
    private final JLabel totalQtyLbl = new JLabel("0");
    private final JLabel totalValueLbl = new JLabel("0.00");
    private final JLabel lowStockLbl = new JLabel("0");

    private int selectedId = -1;

    public InventoryDashboard() {
        setTitle("Yuan Pogi System");
        setSize(950, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        add(buildSummaryPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.WEST);

        loadItems();
    }

    private JPanel buildSummaryPanel() {
        JPanel p = new JPanel(new GridLayout(1, 4, 10, 0));
        p.add(card("Total Items", totalItemsLbl));
        p.add(card("Total Quantity", totalQtyLbl));
        p.add(card("Total Value (PHP)", totalValueLbl));
        p.add(card("Low Stock (<= " + LOW_STOCK_LIMIT + ")", lowStockLbl));
        return p;
    }

    private JPanel card(String title, JLabel valueLabel) {
        JPanel c = new JPanel(new GridLayout(2, 1));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 15, 10, 15)));
        JLabel t = new JLabel(title);
        t.setForeground(Color.GRAY);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 22f));
        c.add(t);
        c.add(valueLabel);
        return c;
    }

    private JPanel buildTablePanel() {
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });

        tableContent.add(new JScrollPane(table), "items");
        tableContent.add(buildEmptyStatePanel(), "empty");
        return tableContent;
    }

    private JPanel buildEmptyStatePanel() {
        JPanel emptyState = new JPanel(new BorderLayout(0, 12));
        emptyState.setBackground(Color.WHITE);

        JLabel photo = new JLabel();
        photo.setHorizontalAlignment(SwingConstants.CENTER);
        java.io.File photoFile = new java.io.File("images/empty-inventory.jpg");
        if (photoFile.isFile()) {
            ImageIcon source = new ImageIcon(photoFile.getPath());
            Image scaled = source.getImage().getScaledInstance(260, 260, Image.SCALE_SMOOTH);
            photo.setIcon(new ImageIcon(scaled));
        }

        JLabel message = new JLabel("No items yet", SwingConstants.CENTER);
        message.setFont(message.getFont().deriveFont(Font.BOLD, 18f));
        JLabel hint = new JLabel("Add an item using the form to get started.", SwingConstants.CENTER);
        JPanel text = new JPanel(new GridLayout(2, 1, 0, 4));
        text.setBackground(Color.WHITE);
        text.add(message);
        text.add(hint);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Color.WHITE);
        if (photo.getIcon() != null) content.add(photo, BorderLayout.CENTER);
        content.add(text, BorderLayout.SOUTH);
        emptyState.add(content, BorderLayout.CENTER);
        emptyState.setBorder(new EmptyBorder(24, 24, 24, 24));
        return emptyState;
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Item Details"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Name", "Category", "Quantity", "Price"};
        JTextField[] fields = {nameField, categoryField, quantityField, priceField};
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i;
            form.add(new JLabel(labels[i]), g);
            g.gridx = 1;
            form.add(fields[i], g);
        }

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");

        addBtn.addActionListener(e -> addItem());
        updateBtn.addActionListener(e -> updateItem());
        deleteBtn.addActionListener(e -> deleteItem());
        clearBtn.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new GridLayout(2, 2, 5, 5));
        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(clearBtn);

        g.gridx = 0; g.gridy = labels.length; g.gridwidth = 2;
        form.add(buttons, g);

        g.gridy++; g.weighty = 1.0;
        form.add(new JPanel(), g);
        return form;
    }

    private void loadItems() {
        try {
            List<Item> items = dao.getAll();
            model.setRowCount(0);
            int totalQty = 0, low = 0;
            double totalValue = 0;
            for (Item i : items) {
                model.addRow(new Object[]{i.getId(), i.getName(), i.getCategory(),
                        i.getQuantity(), String.format("%.2f", i.getPrice())});
                totalQty += i.getQuantity();
                totalValue += i.getQuantity() * i.getPrice();
                if (i.getQuantity() <= LOW_STOCK_LIMIT) low++;
            }
            totalItemsLbl.setText(String.valueOf(items.size()));
            totalQtyLbl.setText(String.valueOf(totalQty));
            totalValueLbl.setText(String.format("%,.2f", totalValue));
            lowStockLbl.setText(String.valueOf(low));
            tableCards.show(tableContent, items.isEmpty() ? "empty" : "items");
        } catch (SQLException ex) {
            showError("Could not load data:\n" + ex.getMessage());
        }
    }

    private void addItem() {
        Item item = readForm(0);
        if (item == null) return;
        try {
            dao.add(item);
            clearForm();
            loadItems();
        } catch (SQLException ex) {
            showError("Could not add item:\n" + ex.getMessage());
        }
    }

    private void updateItem() {
        if (selectedId == -1) {
            showError("Select an item from the table first.");
            return;
        }
        Item item = readForm(selectedId);
        if (item == null) return;
        try {
            dao.update(item);
            clearForm();
            loadItems();
        } catch (SQLException ex) {
            showError("Could not update item:\n" + ex.getMessage());
        }
    }

    private void deleteItem() {
        if (selectedId == -1) {
            showError("Select an item from the table first.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete this item?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(selectedId);
            clearForm();
            loadItems();
        } catch (SQLException ex) {
            showError("Could not delete item:\n" + ex.getMessage());
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        selectedId = (int) model.getValueAt(row, 0);
        nameField.setText(model.getValueAt(row, 1).toString());
        categoryField.setText(model.getValueAt(row, 2).toString());
        quantityField.setText(model.getValueAt(row, 3).toString());
        priceField.setText(model.getValueAt(row, 4).toString());
    }

    private void clearForm() {
        selectedId = -1;
        nameField.setText("");
        categoryField.setText("");
        quantityField.setText("");
        priceField.setText("");
        table.clearSelection();
    }

    private Item readForm(int id) {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        if (name.isEmpty() || category.isEmpty()) {
            showError("Name and Category are required.");
            return null;
        }
        try {
            int qty = Integer.parseInt(quantityField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            if (qty < 0 || price < 0) {
                showError("Quantity and Price cannot be negative.");
                return null;
            }
            return new Item(id, name, category, qty, price);
        } catch (NumberFormatException ex) {
            showError("Quantity must be a whole number and Price must be a number.");
            return null;
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
