package utils;

import model.Book;
import model.Journal;
import model.LibraryItem;
import model.Magazine;
import model.UserAccount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all disk persistence: pipe-delimited text files for catalogue/user save-load,
 * plus a JSON export used by the Admin panel's file-chooser import/export feature.
 */
public class FileHandler {

    public static void saveItems(List<LibraryItem> items, String filename) throws IOException {
        ensureParentDir(filename);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (LibraryItem item : items) pw.println(item.toDataLine());
        }
    }

    public static List<LibraryItem> loadItems(String filename) throws IOException {
        List<LibraryItem> items = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return items;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                LibraryItem item = parseItemLine(line);
                if (item != null) items.add(item);
            }
        }
        return items;
    }

    private static LibraryItem parseItemLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 10) return null;

        String category = parts[0];
        String id = parts[1];
        String title = parts[2];
        String author = parts[3];
        int year = Integer.parseInt(parts[4]);
        boolean available = Boolean.parseBoolean(parts[5]);
        String borrowerId = parts[6].isEmpty() ? null : parts[6];
        String dueDateStr = parts[7];
        int accessCount = Integer.parseInt(parts[8]);
        int timesBorrowed = Integer.parseInt(parts[9]);

        LibraryItem item = switch (category) {
            case "Book" -> new Book(id, title, author, year, "", "");
            case "Magazine" -> new Magazine(id, title, author, year, "");
            case "Journal" -> new Journal(id, title, author, year, "", "");
            default -> null;
        };
        if (item == null) return null;

        item.setAvailable(available);
        item.setBorrowerId(borrowerId);
        if (!dueDateStr.isEmpty()) item.setDueDate(LocalDate.parse(dueDateStr));
        item.setAccessCount(accessCount);
        item.setTimesBorrowed(timesBorrowed);
        IDGenerator.registerExistingId(id);
        return item;
    }

    public static void saveUsers(Map<String, UserAccount> users, String filename) throws IOException {
        ensureParentDir(filename);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (UserAccount user : users.values()) pw.println(user.toDataLine());
        }
    }

    public static Map<String, UserAccount> loadUsers(String filename) throws IOException {
        Map<String, UserAccount> users = new LinkedHashMap<>();
        File file = new File(filename);
        if (!file.exists()) return users;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 4) continue;
                UserAccount user = new UserAccount(parts[0], parts[1]);
                if (!parts[2].isEmpty()) {
                    for (String itemId : parts[2].split(",")) user.addCurrentBorrow(itemId);
                }
                if (!parts[3].isEmpty()) {
                    for (String itemId : parts[3].split(",")) user.addToHistory(itemId);
                }
                users.put(user.getUserId(), user);
            }
        }
        return users;
    }

    /** Exports the catalogue as formatted JSON for the Admin panel's "Export as JSON" action. */
    public static void exportItemsAsJson(List<LibraryItem> items, String filename) throws IOException {
        ensureParentDir(filename);
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < items.size(); i++) {
            LibraryItem item = items.get(i);
            sb.append("  {\n");
            sb.append("    \"category\": \"").append(item.getCategory()).append("\",\n");
            sb.append("    \"id\": \"").append(item.getId()).append("\",\n");
            sb.append("    \"title\": \"").append(escapeJson(item.getTitle())).append("\",\n");
            sb.append("    \"author\": \"").append(escapeJson(item.getAuthor())).append("\",\n");
            sb.append("    \"year\": ").append(item.getYear()).append(",\n");
            sb.append("    \"available\": ").append(item.isAvailable()).append("\n");
            sb.append("  }").append(i < items.size() - 1 ? ",\n" : "\n");
        }
        sb.append("]\n");
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.print(sb);
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void ensureParentDir(String filename) {
        File parent = new File(filename).getParentFile();
        if (parent != null) parent.mkdirs();
    }
}
