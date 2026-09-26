import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Clock class manages the shared time state and handles
 * synchronization between the updating and displaying threads.
 * 
 * @author Jeremiah Segun Jimoh
 */
class Clock {
    private String currentTime;
    private boolean isUpdated = false;

    /**
     * Calculates the current time and notifies the display thread.
     * This method handles the background updating logic.
     */
    public synchronized void updateTime() {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
            currentTime = formatter.format(new Date());
            isUpdated = true;
            notify(); // Signals the display thread that the new time is ready
        } catch (Exception e) {
            System.out.println("Error formatting time: " + e.getMessage());
        }
    }

    /**
     * Waits for the time to be updated and then prints it to the console.
     * This method handles the display logic.
     */
    public synchronized void printTime() {
        try {
            while (!isUpdated) {
                wait(); // Pauses execution until notify() is called by updateTime()
            }
            System.out.println("Current Time: " + currentTime 
                + " [Thread: " + Thread.currentThread().getName() 
                + " | Priority: " + Thread.currentThread().getPriority() + "]");
            isUpdated = false;
        } catch (InterruptedException e) {
            System.out.println("Display thread interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restores interrupted status
        }
    }
}

/**
 * Main application class to initialize the Clock and manage threads.
 */
public class ClockApplication {
    public static void main(String[] args) {
        Clock sharedClock = new Clock();

        // 1. Define the background updating task
        Runnable updateTask = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                sharedClock.updateTime();
                try {
                    Thread.sleep(1000); // Wait 1 second before the next update
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Background updater stopped.");
                }
            }
        };

        // 2. Define the foreground display task
        Runnable displayTask = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                sharedClock.printTime();
            }
        };

        // 3. Initialize Threads
        Thread updaterThread = new Thread(updateTask, "BackgroundUpdater");
        Thread displayThread = new Thread(displayTask, "ConsoleDisplay");

        // 4. Assign Priorities (Rubric Requirement)
        updaterThread.setPriority(Thread.MIN_PRIORITY); // Priority 1 (Lowest)
        displayThread.setPriority(Thread.MAX_PRIORITY); // Priority 10 (Highest)

        System.out.println("Initializing Clock Application...");
        System.out.println("Updater Priority: " + updaterThread.getPriority());
        System.out.println("Display Priority: " + displayThread.getPriority());
        System.out.println("Press Ctrl+C to terminate the process.\n");

        // 5. Start Threads
        updaterThread.start();
        displayThread.start();
    }
}