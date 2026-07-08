package gui;

import controller.LibraryManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Application entry point and main frame. Uses a CardLayout to switch between a
 * login screen and the tabbed dashboard, and drives a Swing Timer for overdue reminders.
 */
public class MainWindow extends JFrame {
    private final LibraryManager manager = new LibraryManager();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel();
    private final JLabel statusBar = new JLabel("Welcome to SLCAS.");

    private ViewItemsPanel viewItemsPanel;
    private SearchSortPanel searchSortPanel;

    private static final String DATA_DIR = "data";
    private static final String ITEMS_FILE = DATA_DIR + File.separator + "items.txt";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.txt";

    private boolean overdueAlertShown = false;

    public MainWindow() {
        super("Smart Library Circulation & Automation System (SLCAS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        new File(DATA_DIR).mkdirs();
        loadData();
        seedSampleDataIfEmpty();

        setJMenuBar(buildMenuBar());

        cards.setLayout(cardLayout);
        cards.add(buildLoginCard(), "LOGIN");
        cards.add(buildDashboardCard(), "DASHBOARD");
        add(cards, BorderLayout.CENTER);

        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusBar, BorderLayout.SOUTH);

        cardLayout.show(cards, "LOGIN");

        setupOverdueTimer();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveData();
            }
        });
    }

    private JPanel buildLoginCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);

        JLabel title = new JLabel("Smart Library Circulation & Automation System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; panel.add(title, gc);

        JLabel subtitle = new JLabel("Enter your name to continue to the dashboard.");
        gc.gridy = 1; panel.add(subtitle, gc);

        gc.gridwidth = 1;
        JTextField nameField = new JTextField(18);
        gc.gridx = 0; gc.gridy = 2; panel.add(new JLabel("Your Name:"), gc);
        gc.gridx = 1; panel.add(nameField, gc);

        JButton enterBtn = new JButton("Enter Library System");
        enterBtn.setMnemonic('E');
        enterBtn.setToolTipText("Proceed to the dashboard (Alt+E)");
        enterBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            statusBar.setText((name.isEmpty() ? "Guest" : name) + " is now using SLCAS.");
            cardLayout.show(cards, "DASHBOARD");
        });
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2; panel.add(enterBtn, gc);

        return panel;
    }

    private JPanel buildDashboardCard() {
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();

        viewItemsPanel = new ViewItemsPanel(manager);
        BorrowPanel borrowPanel = new BorrowPanel(manager, this);
        AdminPanel adminPanel = new AdminPanel(manager, this);
        searchSortPanel = new SearchSortPanel(manager, this);

        tabs.addTab("View Items", viewItemsPanel);
        tabs.addTab("Borrow/Return", borrowPanel);
        tabs.addTab("Admin", adminPanel);
        tabs.addTab("Search & Sort", searchSortPanel);
        tabs.setMnemonicAt(0, KeyEvent.VK_1);
        tabs.setMnemonicAt(1, KeyEvent.VK_2);
        tabs.setMnemonicAt(2, KeyEvent.VK_3);
        tabs.setMnemonicAt(3, KeyEvent.VK_4);

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        JMenuItem saveItem = new JMenuItem("Save Data", 'S');
        saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveItem.addActionListener(e -> {
            saveData();
            statusBar.setText("Data saved to " + DATA_DIR + ".");
        });

        JMenuItem loadItem = new JMenuItem("Reload Data", 'R');
        loadItem.addActionListener(e -> {
            loadData();
            refreshAll();
            statusBar.setText("Data reloaded from " + DATA_DIR + ".");
        });

        JMenuItem exitItem = new JMenuItem("Exit", 'X');
        exitItem.addActionListener(e -> {
            saveData();
            System.exit(0);
        });

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu reportsMenu = new JMenu("Reports");
        reportsMenu.setMnemonic('P');
        JMenuItem reportsItem = new JMenuItem("Generate Reports...", 'G');
        reportsItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Open the Admin tab and click \"Generate Reports\" to view detailed statistics.",
                "Reports", JOptionPane.INFORMATION_MESSAGE));
        reportsMenu.add(reportsItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        JMenuItem aboutItem = new JMenuItem("About", 'A');
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Smart Library Circulation & Automation System (SLCAS)\nCOS 202 Project\nBuilt with Java Swing.",
                "About SLCAS", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(reportsMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void setupOverdueTimer() {
        Timer timer = new Timer(15000, e -> {
            int overdueCount = manager.usersWithOverdueItems().size();
            if (overdueCount > 0) {
                statusBar.setText("Reminder: " + overdueCount + " user(s) have overdue items.");
                if (!overdueAlertShown) {
                    overdueAlertShown = true;
                    JOptionPane.showMessageDialog(this,
                            overdueCount + " user(s) currently have overdue items. Check Admin > Generate Reports for details.",
                            "Overdue Reminder", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                overdueAlertShown = false;
                statusBar.setText("No overdue items. System running normally.");
            }
        });
        timer.setRepeats(true);
        timer.start();
    }

    /** Called by child panels after any mutation so all views stay in sync. */
    public void refreshAll() {
        if (viewItemsPanel != null) viewItemsPanel.refresh();
        if (searchSortPanel != null) searchSortPanel.refresh();
    }

    private void loadData() {
        try {
            manager.loadFromFile(ITEMS_FILE, USERS_FILE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load saved data: " + ex.getMessage(),
                    "Load Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveData() {
        try {
            manager.saveToFile(ITEMS_FILE, USERS_FILE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save data: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seedSampleDataIfEmpty() {
        if (!manager.getDatabase().getItems().isEmpty()) return;
        manager.addItem("Book", "Things Fall Apart", "Chinua Achebe", 1958, "978-0435905255", "Fiction");
        manager.addItem("Book", "Clean Code", "Robert C. Martin", 2008, "978-0132350884", "Technology");
        manager.addItem("Book", "Half of a Yellow Sun", "Chimamanda Ngozi Adichie", 2006, "978-0007200283", "Fiction");
        manager.addItem("Magazine", "National Geographic", "Various", 2024, "May 2024", "");
        manager.addItem("Magazine", "TIME", "Various", 2024, "Jun 2024", "");
        manager.addItem("Journal", "Journal of Computer Science Research", "ACM", 2023, "Vol. 12", "ACM Press");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // fall back to the default cross-platform look and feel
            }
            new MainWindow().setVisible(true);
        });
    }
}
