package model;

import java.time.LocalDate;

public class Book extends LibraryItem implements Borrowable {
    private String isbn;
    private String genre;

    public Book(String id, String title, String author, int year, String isbn, String genre) {
        super(id, title, author, year);
        this.isbn = isbn;
        this.genre = genre;
    }

    @Override
    public String getCategory() { return "Book"; }

    @Override
    public int getMaxBorrowDays() { return 14; }

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

    public String getIsbn() { return isbn; }
    public String getGenre() { return genre; }
}
