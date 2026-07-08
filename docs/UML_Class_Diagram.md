# SLCAS — UML Class Diagram

Paste the block below into the [Mermaid Live Editor](https://mermaid.live) or view it directly
in VS Code / IntelliJ with a Mermaid preview plugin to render the diagram. Export the rendered
image (PNG/SVG) for your submission.

```mermaid
classDiagram
    direction TB

    class LibraryItem {
        <<abstract>>
        -String id
        -String title
        -String author
        -int year
        -boolean available
        -String borrowerId
        -LocalDate dueDate
        -int accessCount
        -int timesBorrowed
        +getCategory() String*
        +getMaxBorrowDays() int*
        +recordAccess() void
        +getSummary() String
        +toDataLine() String
    }

    class Borrowable {
        <<interface>>
        +borrowItem(userId, days) boolean
        +returnItem() boolean
        +isAvailable() boolean
    }

    class Book {
        -String isbn
        -String genre
    }

    class Magazine {
        -String issueNumber
    }

    class Journal {
        -String volume
        -String publisher
    }

    class UserAccount {
        -String userId
        -String name
        -List~String~ borrowingHistory
        -List~String~ currentlyBorrowed
        +addToHistory(itemId) void
        +addCurrentBorrow(itemId) void
        +removeCurrentBorrow(itemId) void
    }

    class LibraryDatabase {
        -List~LibraryItem~ items
        -Map~String,UserAccount~ users
        -Map~String,Queue~String~~ reservationQueues
        +addItem(item) void
        +removeItem(id) boolean
        +findById(id) LibraryItem
        +countByCategory() Map~String,Integer~
    }

    class LibraryManager {
        -LibraryDatabase database
        -Deque~AdminAction~ undoStack
        -LibraryItem[] frequentlyAccessedCache
        +addItem(...) LibraryItem
        +deleteItem(id) boolean
        +undoLastAction() String
        +processItem(item) String
        +mostBorrowedItems(n) List~LibraryItem~
        +usersWithOverdueItems() List~UserAccount~
        +categoryDistribution() Map~String,Integer~
        +computeTotalOverdueCharges(rate) double
    }

    class BorrowController {
        -LibraryDatabase database
        +borrowItem(itemId, userId, userName) String
        +returnItem(itemId) String
    }

    class SearchEngine {
        +linearSearchByTitle(items, title) LibraryItem
        +binarySearchByTitle(sortedItems, title) LibraryItem
        +recursiveSearch(items, query, byAuthor, index) LibraryItem
    }

    class SortEngine {
        <<enumeration Algorithm>>
        +sort(items, comparator, algorithm) void
        -selectionSort(arr, c) void
        -insertionSort(arr, c) void
        -mergeSort(arr, l, r, c) void
        -quickSort(arr, lo, hi, c) void
    }

    class IDGenerator {
        +nextId(category) String$
        +registerExistingId(id) void$
    }

    class FileHandler {
        +saveItems(items, file) void$
        +loadItems(file) List~LibraryItem~$
        +saveUsers(users, file) void$
        +loadUsers(file) Map~String,UserAccount~$
        +exportItemsAsJson(items, file) void$
    }

    class MainWindow {
        -LibraryManager manager
        -CardLayout cardLayout
        +refreshAll() void
    }

    class ViewItemsPanel
    class BorrowPanel
    class AdminPanel
    class SearchSortPanel
    class ItemTableModel
    class ItemRowRenderer

    LibraryItem <|-- Book
    LibraryItem <|-- Magazine
    LibraryItem <|-- Journal
    Borrowable <|.. Book
    Borrowable <|.. Magazine

    LibraryDatabase "1" o-- "many" LibraryItem : contains
    LibraryDatabase "1" o-- "many" UserAccount : contains
    LibraryManager "1" *-- "1" LibraryDatabase : composed of
    LibraryManager ..> IDGenerator : uses
    LibraryManager ..> FileHandler : uses
    BorrowController --> LibraryDatabase : uses
    SearchSortPanel ..> SearchEngine : uses
    SearchSortPanel ..> SortEngine : uses
    BorrowPanel ..> BorrowController : uses

    MainWindow *-- ViewItemsPanel
    MainWindow *-- BorrowPanel
    MainWindow *-- AdminPanel
    MainWindow *-- SearchSortPanel
    MainWindow *-- LibraryManager
    ViewItemsPanel ..> ItemTableModel : uses
    ViewItemsPanel ..> ItemRowRenderer : uses
    SearchSortPanel ..> ItemTableModel : uses
```

## Notes on the design

- **Abstraction & polymorphism**: `LibraryItem` is abstract; `getCategory()` and
  `getMaxBorrowDays()` are overridden differently by `Book`, `Magazine`, and `Journal`.
  `LibraryManager.processItem(LibraryItem item)` operates on the abstract type and behaves
  differently at runtime depending on the concrete subclass passed in — the required
  polymorphic function.
- **Interface**: `Borrowable` is implemented only by `Book` and `Magazine`. `Journal` is
  deliberately excluded to model reference-only items — this makes `instanceof Borrowable`
  checks in `BorrowController` and `LibraryManager` meaningful rather than a rubber stamp.
- **Composition**: `LibraryDatabase` is composed of collections of `LibraryItem` and
  `UserAccount`; `LibraryManager` is composed of a `LibraryDatabase`.
- **Package layout** mirrors the required structure: `model`, `controller`, `gui`, `utils`.
