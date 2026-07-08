package gui;

import controller.LibraryManager;
import model.LibraryItem;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.stream.Collectors;

/** "View Items" tab: browsable, filterable, colour-coded catalogue table. */
public class ViewItemsPanel extends JPanel {
    private final LibraryManager manager;
    private final ItemTableModel tableModel;
    private final JComboBox<String> categoryFilter;
    private final JLabel countLabel = new JLabel();

    public ViewItemsPanel(LibraryManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tableModel = new ItemTableModel(manager.getDatabase().getItems());
        JTable table = new JTable(tableModel);
        table.setDefaultRenderer(Object.class, new ItemRowRenderer(tableModel));
        table.setRowHeight(22);
        table.setAutoCreateRowSorter(true);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Filter by category:"));
        categoryFilter = new JComboBox<>(new String[]{"All", "Book", "Magazine", "Journal"});
        categoryFilter.setToolTipText("Filter the item list by category");
        categoryFilter.addActionListener(e -> applyFilter());
        top.add(categoryFilter);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setMnemonic('R');
        refreshBtn.setToolTipText("Reload the item table (Alt+R)");
        refreshBtn.addActionListener(e -> refresh());
        top.add(refreshBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(countLabel, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        applyFilter();
    }

    private void applyFilter() {
        String category = (String) categoryFilter.getSelectedItem();
        List<LibraryItem> items = manager.getDatabase().getItems();
        List<LibraryItem> filtered = "All".equals(category)
                ? items
                : items.stream().filter(i -> i.getCategory().equals(category)).collect(Collectors.toList());
        tableModel.setItems(filtered);
        countLabel.setText("Showing " + filtered.size() + " of " + items.size() + " total items.");
    }
}
