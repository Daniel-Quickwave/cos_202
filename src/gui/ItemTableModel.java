package gui;

import model.LibraryItem;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/** Custom table model backing every JTable in the GUI, kept in sync with the live item list. */
public class ItemTableModel extends AbstractTableModel {
    private final String[] columns = {"ID", "Category", "Title", "Author", "Year", "Status", "Due Date"};
    private List<LibraryItem> items;

    public ItemTableModel(List<LibraryItem> items) {
        this.items = items;
    }

    public void setItems(List<LibraryItem> items) {
        this.items = items;
        fireTableDataChanged();
    }

    public LibraryItem getItemAt(int row) {
        return items.get(row);
    }

    @Override
    public int getRowCount() { return items.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int col) { return columns[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        LibraryItem item = items.get(row);
        return switch (col) {
            case 0 -> item.getId();
            case 1 -> item.getCategory();
            case 2 -> item.getTitle();
            case 3 -> item.getAuthor();
            case 4 -> item.getYear();
            case 5 -> item.isAvailable() ? "Available" : "Borrowed";
            case 6 -> item.getDueDate() == null ? "-" : item.getDueDate().toString();
            default -> "";
        };
    }
}
