package gui;

import controller.BorrowController;
import controller.LibraryManager;
import model.LibraryItem;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Queue;

/** "Borrow/Return" tab: text-field driven borrow/return workflow with reservation-queue lookup. */
public class BorrowPanel extends JPanel {
    private final LibraryManager manager;
    private final BorrowController borrowController;
    private final MainWindow mainWindow;

    private final JTextField userIdField = new JTextField(10);
    private final JTextField userNameField = new JTextField(12);
    private final JTextField itemIdField = new JTextField(10);
    private final JTextArea outputArea = new JTextArea(10, 40);

    public BorrowPanel(LibraryManager manager, MainWindow mainWindow) {
        this.manager = manager;
        this.mainWindow = mainWindow;
        this.borrowController = new BorrowController(manager.getDatabase());

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0; form.add(new JLabel("User ID:"), gc);
        gc.gridx = 1; userIdField.setToolTipText("Unique ID for the patron, e.g. U001"); form.add(userIdField, gc);

        gc.gridx = 0; gc.gridy = 1; form.add(new JLabel("User Name:"), gc);
        gc.gridx = 1; userNameField.setToolTipText("Patron's display name"); form.add(userNameField, gc);

        gc.gridx = 0; gc.gridy = 2; form.add(new JLabel("Item ID:"), gc);
        gc.gridx = 1; itemIdField.setToolTipText("ID shown in the View Items tab, e.g. BK0001"); form.add(itemIdField, gc);

        JButton borrowBtn = new JButton("Borrow");
        borrowBtn.setMnemonic('B');
        borrowBtn.setToolTipText("Borrow the specified item for this user (Alt+B)");
        JButton returnBtn = new JButton("Return");
        returnBtn.setMnemonic('T');
        returnBtn.setToolTipText("Return the specified item (Alt+T)");
        JButton queueBtn = new JButton("View Reservation Queue");
        queueBtn.setToolTipText("Show who is waiting for this item");

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.add(borrowBtn);
        buttonsPanel.add(returnBtn);
        buttonsPanel.add(queueBtn);

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2; form.add(buttonsPanel, gc);

        add(form, BorderLayout.NORTH);
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        borrowBtn.addActionListener(e -> doBorrow());
        returnBtn.addActionListener(e -> doReturn());
        queueBtn.addActionListener(e -> viewQueue());
    }

    private void doBorrow() {
        String userId = userIdField.getText().trim();
        String userName = userNameField.getText().trim();
        String itemId = itemIdField.getText().trim();
        if (userId.isEmpty() || userName.isEmpty() || itemId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in User ID, User Name, and Item ID.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String result = borrowController.borrowItem(itemId, userId, userName);
            log(result);
            mainWindow.refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error borrowing item: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doReturn() {
        String itemId = itemIdField.getText().trim();
        if (itemId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide an Item ID to return.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String result = borrowController.returnItem(itemId);
            log(result);
            mainWindow.refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error returning item: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewQueue() {
        String itemId = itemIdField.getText().trim();
        if (itemId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide an Item ID.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Queue<String> queue = manager.getDatabase().getReservationQueue(itemId);
        LibraryItem item = manager.getDatabase().findById(itemId);
        String title = item == null ? itemId : item.getTitle();
        if (queue.isEmpty()) {
            log("No one is waiting for \"" + title + "\".");
        } else {
            log("Reservation queue for \"" + title + "\": " + String.join(" -> ", queue));
        }
    }

    private void log(String message) {
        outputArea.append(message + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }
}
