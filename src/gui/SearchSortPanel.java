package gui;

import controller.LibraryManager;
import controller.SearchEngine;
import controller.SortEngine;
import model.LibraryItem;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Comparator;
import java.util.List;

/**
 * "Search & Sort" tab: lets the user pick a search algorithm (Linear / Binary / Recursive)
 * and a sort algorithm (Selection / Insertion / Merge / Quick) via dropdowns.
 */
public class SearchSortPanel extends JPanel {
    private final LibraryManager manager;
    private final MainWindow mainWindow;
    private final SearchEngine searchEngine = new SearchEngine();
    private final SortEngine sortEngine = new SortEngine();

    private final JTextField searchField = new JTextField(15);
    private final JComboBox<String> searchByCombo = new JComboBox<>(new String[]{"Title", "Author", "Type"});
    private final JComboBox<String> searchAlgoCombo =
            new JComboBox<>(new String[]{"Linear Search", "Binary Search", "Recursive Search"});

    private final JComboBox<String> sortFieldCombo = new JComboBox<>(new String[]{"Title", "Author", "Year"});
    private final JComboBox<String> sortAlgoCombo =
            new JComboBox<>(new String[]{"Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort"});

    private final ItemTableModel resultModel;
    private final JLabel infoLabel = new JLabel(" ");

    private boolean sortedByTitleAscending = false;

    public SearchSortPanel(LibraryManager manager, MainWindow mainWindow) {
        this.manager = manager;
        this.mainWindow = mainWindow;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        resultModel = new ItemTableModel(manager.getDatabase().getItems());
        JTable resultTable = new JTable(resultModel);
        resultTable.setDefaultRenderer(Object.class, new ItemRowRenderer(resultModel));
        resultTable.setRowHeight(22);

        add(buildControls(), BorderLayout.NORTH);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);
        add(infoLabel, BorderLayout.SOUTH);
    }

    private JPanel buildControls() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        searchPanel.add(new JLabel("Query:"));
        searchField.setToolTipText("Enter a title, author name, or category to search for");
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("By:"));
        searchPanel.add(searchByCombo);
        searchPanel.add(new JLabel("Algorithm:"));
        searchAlgoCombo.setToolTipText("Binary Search requires the list to be sorted by Title first");
        searchPanel.add(searchAlgoCombo);
        JButton searchBtn = new JButton("Search");
        searchBtn.setMnemonic('S');
        searchBtn.setToolTipText("Run the selected search (Alt+S)");
        searchBtn.addActionListener(e -> doSearch());
        searchPanel.add(searchBtn);
        JButton resetBtn = new JButton("Show All");
        resetBtn.addActionListener(e -> resetResults());
        searchPanel.add(resetBtn);

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sortPanel.setBorder(BorderFactory.createTitledBorder("Sort"));
        sortPanel.add(new JLabel("Field:"));
        sortPanel.add(sortFieldCombo);
        sortPanel.add(new JLabel("Algorithm:"));
        sortAlgoCombo.setToolTipText("Choose which sorting algorithm to apply");
        sortPanel.add(sortAlgoCombo);
        JButton sortBtn = new JButton("Sort");
        sortBtn.setMnemonic('O');
        sortBtn.setToolTipText("Sort the whole catalogue (Alt+O)");
        sortBtn.addActionListener(e -> doSort());
        sortPanel.add(sortBtn);

        container.add(searchPanel);
        container.add(sortPanel);
        return container;
    }

    public void refresh() {
        resultModel.setItems(manager.getDatabase().getItems());
    }

    private void doSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search term.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String by = (String) searchByCombo.getSelectedItem();
        String algo = (String) searchAlgoCombo.getSelectedItem();
        List<LibraryItem> items = manager.getDatabase().getItems();

        if ("Type".equals(by)) {
            List<LibraryItem> matches = items.stream()
                    .filter(i -> i.getCategory().equalsIgnoreCase(query))
                    .toList();
            resultModel.setItems(matches);
            infoLabel.setText(matches.size() + " item(s) found for type \"" + query + "\".");
            return;
        }

        LibraryItem found;
        if ("Linear Search".equals(algo)) {
            found = "Author".equals(by) ? linearByAuthor(items, query) : searchEngine.linearSearchByTitle(items, query);
        } else if ("Binary Search".equals(algo)) {
            if (!"Title".equals(by)) {
                JOptionPane.showMessageDialog(this, "Binary Search is only supported for Title lookups here.",
                        "Unsupported Combination", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!sortedByTitleAscending) {
                sortEngine.sort(items, SortEngine.byTitle(), SortEngine.Algorithm.MERGE);
                sortedByTitleAscending = true;
                JOptionPane.showMessageDialog(this,
                        "The catalogue was not sorted by title, so it was sorted automatically "
                                + "(Merge Sort) before running Binary Search.",
                        "Auto-Sorted", JOptionPane.INFORMATION_MESSAGE);
            }
            found = searchEngine.binarySearchByTitle(items, query);
        } else {
            found = searchEngine.recursiveSearch(items, query, "Author".equals(by), 0);
        }

        if (found != null) {
            manager.recordAccess(found);
            resultModel.setItems(List.of(found));
            infoLabel.setText("Found: " + manager.processItem(found));
        } else {
            resultModel.setItems(List.of());
            infoLabel.setText("No match found for \"" + query + "\".");
        }
        mainWindow.refreshAll();
    }

    private LibraryItem linearByAuthor(List<LibraryItem> items, String author) {
        for (LibraryItem item : items) {
            if (item.getAuthor().equalsIgnoreCase(author)) return item;
        }
        return null;
    }

    private void resetResults() {
        resultModel.setItems(manager.getDatabase().getItems());
        infoLabel.setText(" ");
    }

    private void doSort() {
        String field = (String) sortFieldCombo.getSelectedItem();
        String algoName = (String) sortAlgoCombo.getSelectedItem();
        SortEngine.Algorithm algorithm = switch (algoName) {
            case "Selection Sort" -> SortEngine.Algorithm.SELECTION;
            case "Insertion Sort" -> SortEngine.Algorithm.INSERTION;
            case "Merge Sort" -> SortEngine.Algorithm.MERGE;
            default -> SortEngine.Algorithm.QUICK;
        };
        Comparator<LibraryItem> comparator = switch (field) {
            case "Title" -> SortEngine.byTitle();
            case "Author" -> SortEngine.byAuthor();
            default -> SortEngine.byYear();
        };

        long start = System.nanoTime();
        sortEngine.sort(manager.getDatabase().getItems(), comparator, algorithm);
        long elapsedMicros = (System.nanoTime() - start) / 1000;

        sortedByTitleAscending = "Title".equals(field);
        resultModel.setItems(manager.getDatabase().getItems());
        infoLabel.setText("Sorted by " + field + " using " + algoName + " in " + elapsedMicros + " microseconds.");
        mainWindow.refreshAll();
    }
}
