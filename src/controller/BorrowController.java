package controller;

import model.Borrowable;
import model.LibraryDatabase;
import model.LibraryItem;
import model.UserAccount;

import java.util.Queue;

/** Coordinates borrow/return workflows, including automatic hand-off from the reservation queue. */
public class BorrowController {
    private final LibraryDatabase database;

    public BorrowController(LibraryDatabase database) {
        this.database = database;
    }

    public String borrowItem(String itemId, String userId, String userName) {
        LibraryItem item = database.findById(itemId);
        if (item == null) return "Item not found.";
        if (!(item instanceof Borrowable borrowable)) {
            return item.getCategory() + " items are reference-only and cannot be borrowed.";
        }

        UserAccount user = database.getOrCreateUser(userId, userName);

        if (!borrowable.isAvailable()) {
            database.getReservationQueue(itemId).offer(userId);
            return "\"" + item.getTitle() + "\" is currently borrowed. " + userName
                    + " has been added to the reservation queue (position "
                    + database.getReservationQueue(itemId).size() + ").";
        }

        borrowable.borrowItem(userId, item.getMaxBorrowDays());
        user.addCurrentBorrow(itemId);
        user.addToHistory(itemId);
        item.recordAccess();
        return userName + " successfully borrowed \"" + item.getTitle() + "\". Due back: " + item.getDueDate() + ".";
    }

    public String returnItem(String itemId) {
        LibraryItem item = database.findById(itemId);
        if (item == null) return "Item not found.";
        if (!(item instanceof Borrowable borrowable)) {
            return "This item is reference-only and does not need to be returned.";
        }

        String previousBorrowerId = item.getBorrowerId();
        if (!borrowable.returnItem()) return "That item was not currently borrowed.";

        if (previousBorrowerId != null) {
            UserAccount previousUser = database.getUsers().get(previousBorrowerId);
            if (previousUser != null) previousUser.removeCurrentBorrow(itemId);
        }

        StringBuilder result = new StringBuilder("\"" + item.getTitle() + "\" was returned successfully.");

        Queue<String> queue = database.getReservationQueue(itemId);
        if (!queue.isEmpty()) {
            String nextUserId = queue.poll();
            borrowable.borrowItem(nextUserId, item.getMaxBorrowDays());
            UserAccount nextUser = database.getUsers().get(nextUserId);
            if (nextUser != null) {
                nextUser.addCurrentBorrow(itemId);
                nextUser.addToHistory(itemId);
            }
            result.append(" It has been automatically reserved for user ").append(nextUserId).append(".");
        }
        return result.toString();
    }
}
