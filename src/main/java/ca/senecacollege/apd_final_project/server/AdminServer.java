package ca.senecacollege.apd_final_project.server;

import ca.senecacollege.apd_final_project.util.Constants;
import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server that handles multiple admin client connections
 * Each admin client connects to this server through a command-line interface
 * allowing them to manage the hotel reservation system remotely
 */
public class AdminServer {
    private final int port;
    private ServerSocket serverSocket;
    private boolean running = false;
    private final ExecutorService threadPool;

    /**
     * Constructor
     *
     * @param port The port to listen on
     */
    public AdminServer(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(Constants.MAX_CLIENTS);
    }

    /**
     * Start the server
     *
     * @throws IOException If there's an error starting the server
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        LoggingManager.logSystemInfo("Admin server started on port " + port + ". Waiting for connections...");

        try {
            while (running) {
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();

                // Create a new handler for this client
                ClientHandler handler = new ClientHandler(clientSocket);

                // Submit the handler to the thread pool
                threadPool.submit(handler);

                LoggingManager.logSystemInfo("New admin client connected: " + clientSocket.getInetAddress().getHostAddress());
            }
        } catch (IOException e) {
            if (running) {
                LoggingManager.logException("Error accepting client connection", e);
                throw e;
            }
        } finally {
            stop();
        }
    }

    /**
     * Stop the server
     */
    public void stop() {
        running = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                LoggingManager.logSystemInfo("Admin server stopped");
            } catch (IOException e) {
                LoggingManager.logException("Error closing server socket", e);
            }
        }

        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
            LoggingManager.logSystemInfo("Admin server thread pool shutdown");
        }
    }
}