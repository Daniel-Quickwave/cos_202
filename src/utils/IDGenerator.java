package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Generates unique, category-prefixed IDs (BK0001, MG0001, JN0001, ...). */
public class IDGenerator {
    private static final Map<String, AtomicInteger> counters = new HashMap<>();

    public static synchronized String nextId(String category) {
        AtomicInteger counter = counters.computeIfAbsent(category, k -> new AtomicInteger(0));
        int next = counter.incrementAndGet();
        return prefixFor(category) + String.format("%04d", next);
    }

    /** Called when loading persisted data so new IDs never collide with existing ones. */
    public static synchronized void registerExistingId(String id) {
        String prefix = id.replaceAll("[0-9]+$", "");
        String numberPart = id.replaceAll("^[A-Za-z]+", "");
        String category = categoryForPrefix(prefix);
        if (category == null || numberPart.isEmpty()) return;
        try {
            int num = Integer.parseInt(numberPart);
            AtomicInteger counter = counters.computeIfAbsent(category, k -> new AtomicInteger(0));
            if (num > counter.get()) counter.set(num);
        } catch (NumberFormatException ignored) {
            // malformed ID from an external import; simply skip re-registration
        }
    }

    private static String prefixFor(String category) {
        return switch (category) {
            case "Book" -> "BK";
            case "Magazine" -> "MG";
            case "Journal" -> "JN";
            default -> "IT";
        };
    }

    private static String categoryForPrefix(String prefix) {
        return switch (prefix) {
            case "BK" -> "Book";
            case "MG" -> "Magazine";
            case "JN" -> "Journal";
            default -> null;
        };
    }
}
