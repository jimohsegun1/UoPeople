Project: Online Chat Application
Author: Jeremiah Segun Jimoh
Course: CS 1103 (Programming 2)

Description:
This project is a multithreaded client-server chat application utilizing Java Sockets and the java.net package. The ChatServer handles incoming connections by spawning a new ClientHandler thread for each user, ensuring the server never blocks while waiting for data. It maintains a thread-safe ConcurrentHashMap to broadcast messages to all active clients. The ChatClient utilizes two threads: the main thread reads console input to send to the server, while a background MessageReceiver thread constantly listens for incoming broadcasts.

How to Run the Application:
1. Open a terminal or Windows PowerShell and navigate to the directory containing the source files.
2. Compile both files by typing: 
   javac ChatServer.java ChatClient.java
3. Start the server in the current terminal window: 
   java ChatServer
4. Open a second terminal window, navigate to the same directory, and start the first client: 
   java ChatClient
5. Open a third terminal window, navigate to the same directory, and start a second client: 
   java ChatClient
6. Type messages into either client window and press Enter. You will see the messages broadcast to the other active windows in real time. Type 'exit' to disconnect a client.



1. ChatServer.java

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



2. ChatClient.java

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


3. README.txt

Project: Online Chat Application
Author: Jeremiah Segun Jimoh
Course: CS 1103 (Programming 2)

Description:
This project is a multithreaded client-server chat application utilizing Java Sockets and the java.net package. The ChatServer handles incoming connections by spawning a new ClientHandler thread for each user, ensuring the server never blocks while waiting for data. It maintains a thread-safe ConcurrentHashMap to broadcast messages to all active clients. The ChatClient utilizes two threads: the main thread reads console input to send to the server, while a background MessageReceiver thread constantly listens for incoming broadcasts.

How to Run the Application:
1. Open a terminal or Windows PowerShell and navigate to the directory containing the source files.
2. Compile both files by typing: 
   javac ChatServer.java ChatClient.java
3. Start the server in the current terminal window: 
   java ChatServer
4. Open a second terminal window, navigate to the same directory, and start the first client: 
   java ChatClient
5. Open a third terminal window, navigate to the same directory, and start a second client: 
   java ChatClient
6. Type messages into either client window and press Enter. You will see the messages broadcast to the other active windows in real time. Type 'exit' to disconnect a client.


Screenshot:

