import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Custom exception to handle scenarios where an item cannot be found.
 */
class ItemNotFoundException extends Exception {
    public ItemNotFoundException(String message) {
        super(message);
    }
}

/**
 * A generic class representing a library item.
 * The generic type parameter <T> allows the itemID to be of any type 
 * (e.g., Integer, String, or UUID).
 */
class LibraryItem<T> {
    private String title;
    private String author;
    private T itemID;

    public LibraryItem(String title, String author, T itemID) {
        this.title = title;
        this.author = author;
        this.itemID = itemID;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public T getItemID() {
        return itemID;
    }

    @Override
    public String toString() {
        return "ID: " + itemID + " | Title: " + title + " | Author: " + author;
    }
}

/**
 * Subclass representing a Book. 
 */
class Book<T> extends LibraryItem<T> {
    public Book(String title, String author, T itemID) {
        super(title, author, itemID);
    }

    @Override
    public String toString() {
        return "[Book] " + super.toString();
    }
}

/**
 * Subclass representing a DVD.
 */
class DVD<T> extends LibraryItem<T> {
    public DVD(String title, String author, T itemID) {
        super(title, author, itemID);
    }

    @Override
    public String toString() {
        return "[DVD] " + super.toString();
    }
}

/**
 * A generic catalog that only accepts LibraryItems or its subclasses.
 * The bounded type parameter <T extends LibraryItem<?>> ensures type safety.
 */
class Catalog<T extends LibraryItem<?>> {
    private List<T> items;

    public Catalog() {
        this.items = new ArrayList<>();
    }

    /**
     * Adds a new item to the catalog.
     */
    public void addItem(T item) {
        items.add(item);
        System.out.println("Success: Added '" + item.getTitle() + "' to the catalog.");
    }

    /**
     * Removes an item from the catalog by matching its generic ID.
     * Throws a custom exception if the item does not exist.
     */
    public void removeItemById(Object id) throws ItemNotFoundException {
        T targetItem = null;
        for (T item : items) {
            if (item.getItemID().equals(id)) {
                targetItem = item;
                break;
            }
        }

        if (targetItem == null) {
            throw new ItemNotFoundException("Error: Item with ID '" + id + "' does not exist in the catalog.");
        }

        items.remove(targetItem);
        System.out.println("Success: Removed '" + targetItem.getTitle() + "' from the catalog.");
    }

    /**
     * Displays all current items in the catalog.
     */
    public void viewCatalog() {
        System.out.println("\n--- Current Library Catalog ---");
        if (items.isEmpty()) {
            System.out.println("The catalog is currently empty.");
        } else {
            for (T item : items) {
                System.out.println(item.toString());
            }
        }
        System.out.println("-------------------------------\n");
    }
}

/**
 * Main application class containing the interactive Command Line Interface.
 */
public class LibraryApplication {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // Initialize a generic catalog specifying that the item ID will be a String
        Catalog<LibraryItem<String>> catalog = new Catalog<>();
        boolean isRunning = true;

        System.out.println("Welcome to the Generic Library Catalog System");

        while (isRunning) {
            System.out.println("\nPlease select an operation:");
            System.out.println("1. Add a Book");
            System.out.println("2. Add a DVD");
            System.out.println("3. Remove an Item");
            System.out.println("4. View Catalog");
            System.out.println("5. Exit");
            System.out.print("Choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter Book ID: ");
                    String bookId = scanner.nextLine();
                    System.out.print("Enter Book Title: ");
                    String bookTitle = scanner.nextLine();
                    System.out.print("Enter Book Author: ");
                    String bookAuthor = scanner.nextLine();
                    catalog.addItem(new Book<>(bookTitle, bookAuthor, bookId));
                    break;

                case "2":
                    System.out.print("Enter DVD ID: ");
                    String dvdId = scanner.nextLine();
                    System.out.print("Enter DVD Title: ");
                    String dvdTitle = scanner.nextLine();
                    System.out.print("Enter DVD Director: ");
                    String dvdDirector = scanner.nextLine();
                    catalog.addItem(new DVD<>(dvdTitle, dvdDirector, dvdId));
                    break;

                case "3":
                    System.out.print("Enter the ID of the item to remove: ");
                    String removeId = scanner.nextLine();
                    try {
                        catalog.removeItemById(removeId);
                    } catch (ItemNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;

                case "4":
                    catalog.viewCatalog();
                    break;

                case "5":
                    System.out.println("Exiting system. Goodbye!");
                    isRunning = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please select a number from 1 to 5.");
            }
        }
        scanner.close();
    }
}