package controller;

import model.Book;
import model.Borrowable;
import model.Journal;
import model.LibraryDatabase;
import model.LibraryItem;
import model.Magazine;
import model.UserAccount;
import utils.FileHandler;
import utils.IDGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Central controller: owns the LibraryDatabase, the admin undo stack, the
 * most-frequently-accessed cache (fixed-size array), and reporting logic.
 */
public class LibraryManager {

    private final LibraryDatabase database = new LibraryDatabase();
    private final Deque<AdminAction> undoStack = new ArrayDeque<>();

    private static final int CACHE_SIZE = 5;
    private final LibraryItem[] frequentlyAccessedCache = new LibraryItem[CACHE_SIZE];

    /** Record of an admin add/delete used to support a single-step undo (Stack requirement). */
    private static final class AdminAction {
        enum Type { ADD, DELETE }
        final Type type;
        final LibraryItem item;
        final int index;

        AdminAction(Type type, LibraryItem item, int index) {
            this.type = type;
            this.item = item;
            this.index = index;
        }
    }

    public LibraryDatabase getDatabase() { return database; }

    public LibraryItem addItem(String category, String title, String author, int year, String extra1, String extra2) {
        String id = IDGenerator.nextId(category);
        LibraryItem item = switch (category) {
            case "Book" -> new Book(id, title, author, year, extra1, extra2);
            case "Magazine" -> new Magazine(id, title, author, year, extra1);
            case "Journal" -> new Journal(id, title, author, year, extra1, extra2);
            default -> throw new IllegalArgumentException("Unknown category: " + category);
        };
        database.addItem(item);
        undoStack.push(new AdminAction(AdminAction.Type.ADD, item, database.getItems().size() - 1));
        return item;
    }

    public boolean deleteItem(String itemId) {
        List<LibraryItem> items = database.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(itemId)) {
                LibraryItem removed = items.remove(i);
                undoStack.push(new AdminAction(AdminAction.Type.DELETE, removed, i));
                return true;
            }
        }
        return false;
    }

    /** Pops the undo stack and reverses the most recent add/delete admin action. */
    public String undoLastAction() {
        if (undoStack.isEmpty()) return "Nothing to undo.";
        AdminAction action = undoStack.pop();
        List<LibraryItem> items = database.getItems();
        if (action.type == AdminAction.Type.ADD) {
            items.remove(action.item);
            return "Undo: removed item \"" + action.item.getTitle() + "\".";
        } else {
            int index = Math.min(action.index, items.size());
            items.add(index, action.item);
            return "Undo: restored item \"" + action.item.getTitle() + "\".";
        }
    }

    /** Polymorphic function that can process any LibraryItem subtype uniformly. */
    public String processItem(LibraryItem item) {
        StringBuilder sb = new StringBuilder(item.getSummary());
        sb.append(" | Max borrow days: ").append(item.getMaxBorrowDays());
        sb.append(" | Borrowable: ").append(item instanceof Borrowable ? "Yes" : "No (reference only)");
        return sb.toString();
    }

    public void recordAccess(LibraryItem item) {
        item.recordAccess();
        updateFrequentlyAccessedCache(item);
    }

    private void updateFrequentlyAccessedCache(LibraryItem item) {
        for (LibraryItem cached : frequentlyAccessedCache) {
            if (cached == item) return;
        }
        int slot = -1;
        for (int i = 0; i < frequentlyAccessedCache.length; i++) {
            if (frequentlyAccessedCache[i] == null) { slot = i; break; }
        }
        if (slot == -1) {
            int minIdx = 0;
            for (int i = 1; i < frequentlyAccessedCache.length; i++) {
                if (frequentlyAccessedCache[i].getAccessCount() < frequentlyAccessedCache[minIdx].getAccessCount()) {
                    minIdx = i;
                }
            }
            if (item.getAccessCount() > frequentlyAccessedCache[minIdx].getAccessCount()) slot = minIdx;
        }
        if (slot != -1) frequentlyAccessedCache[slot] = item;
    }

    public LibraryItem[] getFrequentlyAccessedCache() { return frequentlyAccessedCache; }

    public List<LibraryItem> mostBorrowedItems(int topN) {
        List<LibraryItem> sorted = new ArrayList<>(database.getItems());
        sorted.sort((a, b) -> b.getTimesBorrowed() - a.getTimesBorrowed());
        return sorted.subList(0, Math.min(topN, sorted.size()));
    }

    public List<UserAccount> usersWithOverdueItems() {
        List<UserAccount> result = new ArrayList<>();
        for (UserAccount user : database.getUsers().values()) {
            for (String itemId : user.getCurrentlyBorrowed()) {
                LibraryItem item = database.findById(itemId);
                if (item != null && item.getDueDate() != null && item.getDueDate().isBefore(LocalDate.now())) {
                    result.add(user);
                    break;
                }
            }
        }
        return result;
    }

    public Map<String, Integer> categoryDistribution() {
        return database.countByCategory();
    }

    /** Recursive overdue-charge computation across the whole catalogue. */
    public double computeTotalOverdueCharges(double ratePerDay) {
        return computeChargesRecursive(database.getItems(), 0, ratePerDay);
    }

    private double computeChargesRecursive(List<LibraryItem> items, int index, double ratePerDay) {
        if (index >= items.size()) return 0.0;
        LibraryItem item = items.get(index);
        double charge = 0.0;
        if (item.getDueDate() != null && item.getDueDate().isBefore(LocalDate.now())) {
            long daysLate = ChronoUnit.DAYS.between(item.getDueDate(), LocalDate.now());
            charge = daysLate * ratePerDay;
        }
        return charge + computeChargesRecursive(items, index + 1, ratePerDay);
    }

    public void saveToFile(String itemsFile, String usersFile) throws IOException {
        FileHandler.saveItems(database.getItems(), itemsFile);
        FileHandler.saveUsers(database.getUsers(), usersFile);
    }

    public void loadFromFile(String itemsFile, String usersFile) throws IOException {
        database.getItems().clear();
        database.getItems().addAll(FileHandler.loadItems(itemsFile));
        database.getUsers().clear();
        database.getUsers().putAll(FileHandler.loadUsers(usersFile));
    }
}
