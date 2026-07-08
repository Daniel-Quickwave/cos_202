package controller;

import model.LibraryItem;

import java.util.List;

/**
 * Student-implemented search algorithms. Binary search requires the list to already be
 * sorted by title ascending; callers (see gui.SearchSortPanel) are responsible for that.
 */
public class SearchEngine {

    public LibraryItem linearSearchByTitle(List<LibraryItem> items, String title) {
        for (LibraryItem item : items) {
            if (item.getTitle().equalsIgnoreCase(title)) return item;
        }
        return null;
    }

    public LibraryItem binarySearchByTitle(List<LibraryItem> sortedByTitleItems, String title) {
        int low = 0;
        int high = sortedByTitleItems.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = sortedByTitleItems.get(mid).getTitle().compareToIgnoreCase(title);
            if (cmp == 0) return sortedByTitleItems.get(mid);
            if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return null;
    }

    /** Recursive linear search over title or author (case-insensitive substring match). */
    public LibraryItem recursiveSearch(List<LibraryItem> items, String query, boolean byAuthor, int index) {
        if (index >= items.size()) return null;
        LibraryItem item = items.get(index);
        String field = byAuthor ? item.getAuthor() : item.getTitle();
        if (field.toLowerCase().contains(query.toLowerCase())) return item;
        return recursiveSearch(items, query, byAuthor, index + 1);
    }
}
