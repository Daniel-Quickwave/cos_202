package model;

/**
 * Contract for items that may leave the library building.
 * Reference-only items (see {@link Journal}) intentionally do not implement this.
 */
public interface Borrowable {
    boolean borrowItem(String userId, int days);
    boolean returnItem();
    boolean isAvailable();
}
