package model;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

/**
 * Encapsulates and composes the collections that make up the library's state:
 * the catalogue of items, the registered user accounts, and per-item reservation queues.
 */
public class LibraryDatabase {
    private final List<LibraryItem> items = new ArrayList<>();
    private final Map<String, UserAccount> users = new LinkedHashMap<>();
    private final Map<String, Queue<String>> reservationQueues = new LinkedHashMap<>();

    public List<LibraryItem> getItems() { return items; }
    public Map<String, UserAccount> getUsers() { return users; }

    public void addItem(LibraryItem item) { items.add(item); }

    public boolean removeItem(String itemId) {
        return items.removeIf(i -> i.getId().equals(itemId));
    }

    public LibraryItem findById(String id) {
        for (LibraryItem item : items) {
            if (item.getId().equals(id)) return item;
        }
        return null;
    }

    public UserAccount getOrCreateUser(String userId, String name) {
        return users.computeIfAbsent(userId, k -> new UserAccount(userId, name));
    }

    public Queue<String> getReservationQueue(String itemId) {
        return reservationQueues.computeIfAbsent(itemId, k -> new LinkedList<>());
    }

    /** Recursively tallies items per category (recursive algorithm requirement). */
    public Map<String, Integer> countByCategory() {
        Map<String, Integer> counts = new TreeMap<>();
        countByCategoryRecursive(items, 0, counts);
        return counts;
    }

    private void countByCategoryRecursive(List<LibraryItem> list, int index, Map<String, Integer> counts) {
        if (index >= list.size()) return;
        LibraryItem item = list.get(index);
        counts.merge(item.getCategory(), 1, Integer::sum);
        countByCategoryRecursive(list, index + 1, counts);
    }
}
