package model;

import java.time.LocalDate;

public class Magazine extends LibraryItem implements Borrowable {
    private String issueNumber;

    public Magazine(String id, String title, String author, int year, String issueNumber) {
        super(id, title, author, year);
        this.issueNumber = issueNumber;
    }

    @Override
    public String getCategory() { return "Magazine"; }

    @Override
    public int getMaxBorrowDays() { return 7; }

    @Override
    public boolean borrowItem(String userId, int days) {
        if (!available) return false;
        available = false;
        borrowerId = userId;
        dueDate = LocalDate.now().plusDays(days > 0 ? days : getMaxBorrowDays());
        timesBorrowed++;
        return true;
    }

    @Override
    public boolean returnItem() {
        if (available) return false;
        available = true;
        borrowerId = null;
        dueDate = null;
        return true;
    }

    @Override
    public boolean isAvailable() { return available; }

    public String getIssueNumber() { return issueNumber; }
}
