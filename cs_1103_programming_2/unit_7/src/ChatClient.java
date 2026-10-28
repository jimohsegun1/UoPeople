import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * The ChatClient connects to the ChatServer and handles two-way communication.
 */
public class ChatClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            System.out.println("Successfully connected to the chat server.");

            // Start a separate thread strictly for listening to incoming messages
            new Thread(new MessageReceiver(socket)).start();

            // Use the main thread to read user input from the console and send it
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner consoleScanner = new Scanner(System.in);

            while (consoleScanner.hasNextLine()) {
                String message = consoleScanner.nextLine();
                out.println(message);
                
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Disconnecting from server...");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to connect to the server: " + e.getMessage());
        }
    }
}

/**
 * A background task that continuously reads messages sent by the server.
 */
class MessageReceiver implements Runnable {
    private Socket socket;

    public MessageReceiver(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                // Display the received message in the text-based UI
                System.out.println(serverMessage);
            }
        } catch (IOException e) {
            // This exception naturally occurs when the socket is closed upon exit
            System.out.println("Connection to server closed.");
        }
    }
}