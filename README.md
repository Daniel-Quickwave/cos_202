# Smart Library Circulation & Automation System (SLCAS)

COS 202 project — a Java Swing desktop app for managing library circulation.

## Project Structure

```
COS 202/
  src/
    model/        LibraryItem, Book, Magazine, Journal, Borrowable, UserAccount, LibraryDatabase
    controller/   LibraryManager, BorrowController, SearchEngine, SortEngine
    gui/          MainWindow, ViewItemsPanel, BorrowPanel, AdminPanel, SearchSortPanel,
                  ItemTableModel, ItemRowRenderer
    utils/        IDGenerator, FileHandler
  data/           items.txt / users.txt are created here on first save (persistence)
  docs/
    UML_Class_Diagram.md   Mermaid class diagram (render at https://mermaid.live)
    Project_Report.md      2-3 page project report (description, features, data
                            structures, algorithms, challenges)
```

## Requirements

- JDK 17 or later (the code uses `switch` expressions, pattern-matching `instanceof`,
  and `List.toList()`).
- No external libraries/dependencies — pure `javax.swing` + the JDK standard library.

## Build & Run

### Windows (PowerShell)

From the `COS 202` folder:

```powershell
# Compile everything into an "out" directory
Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName } | Set-Content sources.txt
javac -d out "@sources.txt"

# Run
java -cp out gui.MainWindow
```

If `javac`/`java` aren't recognized, either open a new PowerShell window (so it picks up
the PATH change from the JDK installer) or call the full path directly, e.g.:

```powershell
& "C:\Program Files\Java\jdk-26.0.1\bin\javac.exe" -d out "@sources.txt"
& "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" -cp out gui.MainWindow
```
(Adjust the `jdk-26.0.1` folder name to whatever version you have installed under
`C:\Program Files\Java\`.)

### Windows (cmd.exe)

```bat
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
java -cp out gui.MainWindow
```

### macOS / Linux / Git Bash

```bash
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out gui.MainWindow
```

### Using an IDE instead

Simplest cross-platform option if you have an IDE (IntelliJ IDEA / Eclipse / VS Code with
the Java Extension Pack): open the `COS 202` folder as a project, mark `src` as the
sources root, and run `gui.MainWindow`.

On first run the app seeds a handful of sample Books/Magazines/Journals if
`data/items.txt` doesn't exist yet, so the tabs aren't empty.


