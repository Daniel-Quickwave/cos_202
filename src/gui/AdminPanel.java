package gui;

import controller.LibraryManager;
import model.LibraryItem;
import model.UserAccount;
import utils.FileHandler;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * "Admin" tab: add/delete items with an undo stack, import/export via a file chooser,
 * and a reports dialog. The add-item form swaps category-specific fields at runtime
 * using an inner CardLayout (dynamic components requirement).
 */
public class AdminPanel extends JPanel {
    private final LibraryManager manager;
    private final MainWindow mainWindow;

    private final JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Book", "Magazine", "Journal"});
    private final JTextField titleField = new JTextField(15);
    private final JTextField authorField = new JTextField(15);
    private final JTextField yearField = new JTextField(6);

    private final CardLayout dynamicLayout = new CardLayout();
    private final JPanel dynamicFieldsPanel = new JPanel(dynamicLayout);

    private final JTextField isbnField = new JTextField(12);
    private final JTextField genreField = new JTextField(12);
    private final JTextField issueField = new JTextField(12);
    private final JTextField volumeField = new JTextField(8);
    private final JTextField publisherField = new JTextField(12);

    private final JTextField deleteIdField = new JTextField(10);
    private final JTextArea logArea = new JTextArea(8, 50);

    public AdminPanel(LibraryManager manager, MainWindow mainWindow) {
        this.manager = manager;
        this.mainWindow = mainWindow;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildFormPanel(), BorderLayout.NORTH);

        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        add(buildActionsPanel(), BorderLayout.SOUTH);

        categoryCombo.addActionListener(e ->
                dynamicLayout.show(dynamicFieldsPanel, (String) categoryCombo.getSelectedItem()));
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New Library Item"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0; panel.add(new JLabel("Category:"), gc);
        gc.gridx = 1; categoryCombo.setToolTipText("Select the type of item to add"); panel.add(categoryCombo, gc);

        gc.gridx = 0; gc.gridy = 1; panel.add(new JLabel("Title:"), gc);
        gc.gridx = 1; panel.add(titleField, gc);

        gc.gridx = 0; gc.gridy = 2; panel.add(new JLabel("Author:"), gc);
        gc.gridx = 1; panel.add(authorField, gc);

        gc.gridx = 0; gc.gridy = 3; panel.add(new JLabel("Year:"), gc);
        gc.gridx = 1; panel.add(yearField, gc);

        JPanel bookFields = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bookFields.add(new JLabel("ISBN:")); bookFields.add(isbnField);
        bookFields.add(new JLabel("Genre:")); bookFields.add(genreField);

        JPanel magazineFields = new JPanel(new FlowLayout(FlowLayout.LEFT));
        magazineFields.add(new JLabel("Issue #:")); magazineFields.add(issueField);

        JPanel journalFields = new JPanel(new FlowLayout(FlowLayout.LEFT));
        journalFields.add(new JLabel("Volume:")); journalFields.add(volumeField);
        journalFields.add(new JLabel("Publisher:")); journalFields.add(publisherField);

        dynamicFieldsPanel.add(bookFields, "Book");
        dynamicFieldsPanel.add(magazineFields, "Magazine");
        dynamicFieldsPanel.add(journalFields, "Journal");

        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2; panel.add(dynamicFieldsPanel, gc);

        JButton addBtn = new JButton("Add Item");
        addBtn.setMnemonic('A');
        addBtn.setToolTipText("Add this item to the library catalogue (Alt+A)");
        addBtn.addActionListener(e -> addItem());
        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 2; panel.add(addBtn, gc);

        return panel;
    }

    private JPanel buildActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Admin Actions"));

        panel.add(new JLabel("Item ID:"));
        panel.add(deleteIdField);

        JButton deleteBtn = new JButton("Delete Item");
        deleteBtn.setMnemonic('D');
        deleteBtn.setToolTipText("Delete the item with this ID (Alt+D)");
        deleteBtn.addActionListener(e -> deleteItem());
        panel.add(deleteBtn);

        JButton undoBtn = new JButton("Undo Last Action");
        undoBtn.setMnemonic('U');
        undoBtn.setToolTipText("Undo the last add/delete operation (Alt+U)");
        undoBtn.addActionListener(e -> undo());
        panel.add(undoBtn);

        JButton importBtn = new JButton("Import...");
        importBtn.setToolTipText("Import items from a pipe-delimited text file");
        importBtn.addActionListener(e -> importItems());
        panel.add(importBtn);

        JButton exportBtn = new JButton("Export as JSON...");
        exportBtn.setToolTipText("Export the current catalogue to a JSON file");
        exportBtn.addActionListener(e -> exportItems());
        panel.add(exportBtn);

        JButton reportsBtn = new JButton("Generate Reports");
        reportsBtn.setMnemonic('R');
        reportsBtn.setToolTipText("Show most borrowed items, overdue users, and category distribution (Alt+R)");
        reportsBtn.addActionListener(e -> showReports());
        panel.add(reportsBtn);

        return panel;
    }

    private void addItem() {
        try {
            String category = (String) categoryCombo.getSelectedItem();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String yearText = yearField.getText().trim();
            if (title.isEmpty() || author.isEmpty() || yearText.isEmpty()) {
                throw new IllegalArgumentException("Title, Author, and Year are required.");
            }
            int year = Integer.parseInt(yearText);

            String extra1 = "";
            String extra2 = "";
            switch (category) {
                case "Book" -> { extra1 = isbnField.getText().trim(); extra2 = genreField.getText().trim(); }
                case "Magazine" -> extra1 = issueField.getText().trim();
                case "Journal" -> { extra1 = volumeField.getText().trim(); extra2 = publisherField.getText().trim(); }
            }

            LibraryItem item = manager.addItem(category, title, author, year, extra1, extra2);
            log("Added " + category + " \"" + item.getTitle() + "\" with ID " + item.getId() + ".");
            clearForm();
            mainWindow.refreshAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year must be a valid whole number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        titleField.setText("");
        authorField.setText("");
        yearField.setText("");
        isbnField.setText("");
        genreField.setText("");
        issueField.setText("");
        volumeField.setText("");
        publisherField.setText("");
    }

    private void deleteItem() {
        String id = deleteIdField.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Item ID to delete.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete item " + id + "? This can be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean removed = manager.deleteItem(id);
        log(removed ? "Deleted item " + id + "." : "Item " + id + " not found.");
        mainWindow.refreshAll();
    }

    private void undo() {
        log(manager.undoLastAction());
        mainWindow.refreshAll();
    }

    private void importItems() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import Library Items (pipe-delimited text file)");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                List<LibraryItem> imported = FileHandler.loadItems(file.getAbsolutePath());
                manager.getDatabase().getItems().addAll(imported);
                log("Imported " + imported.size() + " items from " + file.getName() + ".");
                mainWindow.refreshAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to import: " + ex.getMessage(),
                        "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportItems() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Catalogue as JSON");
        chooser.setSelectedFile(new File("library_export.json"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                FileHandler.exportItemsAsJson(manager.getDatabase().getItems(), file.getAbsolutePath());
                log("Exported catalogue to " + file.getName() + ".");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showReports() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Most Borrowed Items ===\n");
        List<LibraryItem> top = manager.mostBorrowedItems(5);
        if (top.isEmpty()) sb.append("No borrowing activity yet.\n");
        for (LibraryItem item : top) {
            sb.append(item.getTitle()).append(" - borrowed ").append(item.getTimesBorrowed()).append(" time(s)\n");
        }

        sb.append("\n=== Users With Overdue Items ===\n");
        List<UserAccount> overdueUsers = manager.usersWithOverdueItems();
        if (overdueUsers.isEmpty()) sb.append("No overdue items.\n");
        for (UserAccount user : overdueUsers) {
            sb.append(user.getName()).append(" (").append(user.getUserId()).append(")\n");
        }

        sb.append("\n=== Category Distribution ===\n");
        Map<String, Integer> distribution = manager.categoryDistribution();
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        sb.append("\nEstimated total overdue charges (@ N50/day): N")
          .append(manager.computeTotalOverdueCharges(50.0));

        JTextArea area = new JTextArea(sb.toString(), 18, 45);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Library Reports", JOptionPane.INFORMATION_MESSAGE);
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
