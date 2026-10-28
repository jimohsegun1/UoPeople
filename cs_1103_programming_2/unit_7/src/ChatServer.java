import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The ChatServer manages client connections and broadcasts messages.
 */
public class ChatServer {
    private static final int PORT = 8080;
    // A thread-safe set to keep track of all connected client handlers
    private static Set<ClientHandler> activeClients = ConcurrentHashMap.newKeySet();
    private static int userIdCounter = 1;

    public static void main(String[] args) {
        System.out.println("Chat Server is actively listening on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                
                // Assign a unique user ID
                String assignedUserId = "User" + userIdCounter++;
                System.out.println(assignedUserId + " has connected from " + clientSocket.getInetAddress());

                // Create a new handler for this client and start it on a new thread
                ClientHandler clientHandler = new ClientHandler(clientSocket, assignedUserId);
                activeClients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server encountered an error: " + e.getMessage());
        }
    }

    /**
     * Broadcasts a message to all connected clients except the sender.
     */
    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : activeClients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Removes a client from the active list and notifies others.
     */
    public static void removeClient(ClientHandler client) {
        activeClients.remove(client);
        System.out.println(client.getUserId() + " has disconnected.");
        broadcastMessage("SERVER: " + client.getUserId() + " has left the chat.", null);
    }
}

/**
 * Handles communication for a single client in its own thread.
 */
class ClientHandler implements Runnable {
    private Socket socket;
    private String userId;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, String userId) {
        this.socket = socket;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public void run() {
        try {
            // Setup input and output streams for the socket
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Greet the new user and announce them to the chat
            out.println("SERVER: Welcome to the chat! You are " + userId + ". Type 'exit' to leave.");
            ChatServer.broadcastMessage("SERVER: " + userId + " has joined the chat.", this);

            String incomingMessage;
            // Continuously listen for messages from this client
            while ((incomingMessage = in.readLine()) != null) {
                if (incomingMessage.equalsIgnoreCase("exit")) {
                    break;
                }
                // Broadcast the formatted message to all other users
                ChatServer.broadcastMessage(userId + ": " + incomingMessage, this);
            }
        } catch (IOException e) {
            System.err.println("Connection error with " + userId + ": " + e.getMessage());
        } finally {
            // Clean up resources when the client disconnects
            ChatServer.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket for " + userId);
            }
        }
    }

    /**
     * Sends a message directly to this client.
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}