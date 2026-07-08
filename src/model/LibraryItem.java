package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Abstract base class for every item the library catalogues.
 * Concrete behaviour (category, borrowing rules) is supplied by subclasses.
 */
public abstract class LibraryItem implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String title;
    protected String author;
    protected int year;
    protected boolean available;
    protected String borrowerId;
    protected LocalDate dueDate;
    protected int accessCount;
    protected int timesBorrowed;

    protected LibraryItem(String id, String title, String author, int year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.available = true;
        this.accessCount = 0;
        this.timesBorrowed = 0;
    }

    public abstract String getCategory();

    public abstract int getMaxBorrowDays();

    public void recordAccess() {
        accessCount++;
    }

    public String getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getBorrowerId() { return borrowerId; }
    public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public int getAccessCount() { return accessCount; }
    public void setAccessCount(int accessCount) { this.accessCount = accessCount; }

    public int getTimesBorrowed() { return timesBorrowed; }
    public void setTimesBorrowed(int timesBorrowed) { this.timesBorrowed = timesBorrowed; }

    /** Human readable one-line summary, used by the polymorphic processing function. */
    public String getSummary() {
        return String.format("[%s] \"%s\" by %s (%d) - %s",
                getCategory(), title, author, year,
                available ? "Available" : ("Borrowed by " + borrowerId));
    }

    /** Pipe-delimited line used for plain-text persistence. */
    public String toDataLine() {
        return String.join("|",
                getCategory(),
                id,
                escape(title),
                escape(author),
                String.valueOf(year),
                String.valueOf(available),
                borrowerId == null ? "" : borrowerId,
                dueDate == null ? "" : dueDate.toString(),
                String.valueOf(accessCount),
                String.valueOf(timesBorrowed));
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("|", "/");
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
