package model;

/**
 * Journals are reference-only in this library: they never leave the building,
 * so unlike Book and Magazine this class does NOT implement Borrowable.
 * This asymmetry is what makes the Borrowable interface meaningful for polymorphism.
 */
public class Journal extends LibraryItem {
    private String volume;
    private String publisher;

    public Journal(String id, String title, String author, int year, String volume, String publisher) {
        super(id, title, author, year);
        this.volume = volume;
        this.publisher = publisher;
        this.available = true;
    }

    @Override
    public String getCategory() { return "Journal"; }

    @Override
    public int getMaxBorrowDays() { return 0; }

    public String getVolume() { return volume; }
    public String getPublisher() { return publisher; }
}
