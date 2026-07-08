# Smart Library Circulation & Automation System (SLCAS)
### COS 202 Project Report

---

## 1. Description

SLCAS is a desktop Java Swing application that lets a university library manage its
catalogue of books, magazines, and journals; process borrowing and returning; queue
reservations for unavailable items; search and sort the catalogue with a choice of
algorithms; undo the last administrative action; persist data to disk; and generate
circulation reports. The system was built to demonstrate advanced object-oriented design,
hand-written data-structure and algorithm implementations, recursion, and event-driven GUI
programming, per the COS 202 project brief.

The application opens on a simple login card (`CardLayout`) and switches to a tabbed
dashboard with four panels: **View Items**, **Borrow/Return**, **Admin**, and
**Search & Sort**. A background `javax.swing.Timer` continuously checks for overdue items
and updates the status bar / raises a one-time reminder dialog.

## 2. Features

- Add new library items (Book, Magazine, Journal) through a dynamic Admin form whose
  fields change per category at runtime.
- Borrow and return items, with automatic hand-off to the next person in a reservation
  queue when a book is returned.
- Maintain a per-item reservation queue (FIFO) for items that are currently unavailable.
- Search the catalogue by title, author, or type using a selectable algorithm (Linear,
  Binary, or Recursive search).
- Sort the catalogue by title, author, or year using a selectable algorithm (Selection,
  Insertion, Merge, or Quick sort), chosen from a GUI dropdown.
- Undo the most recent admin action (add or delete) via a Stack-backed undo history.
- A fixed-size array cache tracking the "Most Frequently Accessed Items."
- Import items from a text file and export the catalogue to JSON via `JFileChooser`
  dialogs.
- Save and load all data (items + user accounts) to plain-text files on exit/demand, so
  state survives restarts.
- Generate three reports: Most Borrowed Items, Users with Overdue Items, and Category
  Distribution, plus an estimated total overdue-charge figure.
- Input validation with `JOptionPane` dialogs, tooltips, mnemonics/keyboard shortcuts
  (e.g. Alt+A to add, Ctrl+S to save), and colour-coded table rows via a custom cell
  renderer (red = overdue, amber = borrowed, white = available).

## 3. Data Structures Used

| Structure | Where | Purpose |
|---|---|---|
| `ArrayList<LibraryItem>` | `LibraryDatabase.items` | Primary store for the catalogue; supports the index-based access the sort/search algorithms need. |
| `Queue<String>` (`LinkedList`) | `LibraryDatabase.reservationQueues` | One FIFO reservation queue per item ID; patrons are served in arrival order when an item is returned. |
| `Deque<AdminAction>` used as a **Stack** | `LibraryManager.undoStack` | Push on every add/delete; pop to reverse the most recent action — classic LIFO undo. |
| Fixed-size `LibraryItem[]` array | `LibraryManager.frequentlyAccessedCache` | A small (5-slot) cache of the most-accessed items, replaced using a "lowest access count evicted first" policy — chosen to satisfy the brief's explicit array requirement without duplicating ArrayList's job. |
| `Map<String, UserAccount>` / `Map<String, Integer>` | `LibraryDatabase.users`, category counts | Fast ID-based user lookup and category tallying. |

## 4. Algorithms Chosen and Why

**Search (two implemented, a third for recursion):**
- *Linear search* — used for author lookups and as the default when the catalogue isn't
  known to be sorted. O(n), but simple and always correct regardless of ordering.
- *Binary search* — used for title lookups once the catalogue is confirmed sorted by
  title (O(log n)). The GUI is honest about this precondition: if the user picks Binary
  Search while the list isn't sorted by title, the app automatically runs a Merge Sort
  first and tells the user why, rather than silently returning wrong results.
- *Recursive search* — a straightforward recursive linear scan over title or author,
  satisfying the recursion requirement in a second, independent code path from the two
  above.

**Sorting (all four implemented, selectable from a dropdown):**
- *Selection Sort* and *Insertion Sort* — simple O(n²) algorithms, useful baselines and
  fine for the catalogue sizes a single library branch would hold.
- *Merge Sort* — the recommended, stable O(n log n) algorithm; used internally as the
  "make it safe for binary search" step, since a stable sort keeps ties (e.g. same title)
  in a predictable order.
- *Quick Sort* — a second O(n log n) option, included so the dropdown offers a genuine
  choice and to demonstrate an in-place, divide-and-conquer alternative to Merge Sort.

Both Merge Sort and Quick Sort are naturally recursive, and are implemented from scratch
(no `Collections.sort`/`Arrays.sort`) against a `Comparator<LibraryItem>` so the same code
sorts by title, author, or year.

**Recursion (beyond the sorting algorithms):**
1. `LibraryDatabase.countByCategoryRecursive` — recursively tallies items per category.
2. `SearchEngine.recursiveSearch` — recursive linear search by title or author.
3. `LibraryManager.computeChargesRecursive` — recursively sums overdue charges across the
   catalogue (used by the Admin Reports dialog).

## 5. Object-Oriented Design

- `LibraryItem` is an **abstract class** with `Book`, `Magazine`, and `Journal` as
  concrete subclasses (`/model`).
- `Borrowable` is an **interface**, implemented by `Book` and `Magazine` only — `Journal`
  is intentionally reference-only, so `instanceof Borrowable` checks in
  `BorrowController`/`LibraryManager` are meaningful rather than trivially true.
- **Polymorphism**: `LibraryManager.processItem(LibraryItem item)` accepts any subtype and
  produces different output depending on the runtime type's `getCategory()`,
  `getMaxBorrowDays()`, and whether it implements `Borrowable`.
- **Encapsulation & composition**: `LibraryDatabase` encapsulates the item list, the user
  map, and the reservation queues; `UserAccount` encapsulates a patron's borrowing
  history; `LibraryManager` is composed of a `LibraryDatabase` plus its own undo stack and
  cache.
- **Package organisation**: `model`, `controller`, `gui`, `utils` — matching the brief.

## 6. GUI Design

Built entirely with Java Swing. `MainWindow` uses `BorderLayout` for the overall frame
(menu bar, `CardLayout` content, status bar) and a `CardLayout` to switch between the
login card and the dashboard card. The dashboard is a `JTabbedPane` with four tabs, each
laid out with `GridBagLayout`/`BoxLayout`/`FlowLayout` as appropriate. `AdminPanel` nests a
second `CardLayout` inside its add-item form so the category-specific fields (ISBN/Genre
for Book, Issue # for Magazine, Volume/Publisher for Journal) swap dynamically as the
category combo box changes. Advanced touches include a custom `TableCellRenderer` for
colour-coded rows, `JFileChooser` import/export, a `javax.swing.Timer` for overdue
reminders, `JOptionPane` validation dialogs, and mnemonics/tooltips throughout.

## 7. Persistence

Items and user accounts are saved to pipe-delimited text files (`data/items.txt`,
`data/users.txt`) on save/exit and reloaded on startup, satisfying the persistence
requirement without an external JSON library dependency. A separate JSON export
(`FileHandler.exportItemsAsJson`) is available from the Admin tab for interoperability.

## 8. Challenges Faced

- **Deciding which search precondition to enforce**: binary search silently returning
  wrong results on an unsorted list is a common student bug. The panel now tracks whether
  the catalogue is currently sorted by title and transparently re-sorts (with a dialog
  explaining why) rather than failing silently.
- **Keeping four independent GUI panels in sync** with one shared, mutable
  `LibraryDatabase`: solved with a single `MainWindow.refreshAll()` call invoked by every
  panel after a mutation, rather than each panel polling independently.
- **Making the undo stack meaningful for both add and delete**: a delete needs to
  remember not just the item but its original list index so an undo restores its
  position rather than appending it at the end.
- **Giving the Borrowable interface a real purpose**: an early draft had all three
  subclasses implement it, which made the interface a no-op. Making `Journal`
  reference-only turned the interface check into an actual branch in the borrow/return
  and polymorphic-processing logic.
