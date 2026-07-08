package model;

import java.util.ArrayList;
import java.util.List;

/** Encapsulates a library patron's identity and borrowing history (composition target of LibraryDatabase). */
public class UserAccount {
    private final String userId;
    private final String name;
    private final List<String> borrowingHistory = new ArrayList<>();
    private final List<String> currentlyBorrowed = new ArrayList<>();

    public UserAccount(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public void addToHistory(String itemId) { borrowingHistory.add(itemId); }
    public void addCurrentBorrow(String itemId) { currentlyBorrowed.add(itemId); }
    public void removeCurrentBorrow(String itemId) { currentlyBorrowed.remove(itemId); }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public List<String> getBorrowingHistory() { return borrowingHistory; }
    public List<String> getCurrentlyBorrowed() { return currentlyBorrowed; }

    public String toDataLine() {
        return String.join("|",
                userId,
                name.replace("|", "/"),
                String.join(",", currentlyBorrowed),
                String.join(",", borrowingHistory));
    }
}
