package gui;

import model.LibraryItem;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;

/** Custom cell renderer that colour-codes rows: red = overdue, amber = borrowed, white = available. */
public class ItemRowRenderer extends DefaultTableCellRenderer {
    private final ItemTableModel model;

    public ItemRowRenderer(ItemTableModel model) {
        this.model = model;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        int modelRow = table.convertRowIndexToModel(row);
        LibraryItem item = model.getItemAt(modelRow);
        if (!isSelected) {
            if (item.getDueDate() != null && item.getDueDate().isBefore(LocalDate.now())) {
                c.setBackground(new Color(255, 205, 205));
            } else if (!item.isAvailable()) {
                c.setBackground(new Color(255, 245, 200));
            } else {
                c.setBackground(Color.WHITE);
            }
        }
        return c;
    }
}
