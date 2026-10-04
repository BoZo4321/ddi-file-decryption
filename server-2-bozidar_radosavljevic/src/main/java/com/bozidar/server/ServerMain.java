package com.bozidar.server;

import com.bozidar.server.config.ServerConfig;
import com.bozidar.server.database.Database;
import com.bozidar.server.database.SQLiteDatabase;
import com.bozidar.server.decryption.FileProcessorFactory;
import com.bozidar.server.exception.ServerException;
import com.bozidar.server.network.ClientConnectionHandler;
import com.bozidar.server.zip.ZipService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    public static void main(String[] args) {
        ServerConfig config;
        try {
            config = ServerConfig.fromArgs(args);
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid arguments: " + ex.getMessage());
            System.err.println("Usage: java -jar server_2.jar <portNumber> <pathToDatabase> <pathToOutputDir>");
            System.exit(1);
            return;
        }

        System.out.println("Server starting with " + config);

        try (Database database = new SQLiteDatabase(config.getDatabasePath())) {
            database.ensureLogTableExists();
            logSafely(database, "SERVER_STARTED", "Listening on port " + config.getPort());

            ClientConnectionHandler handler = new ClientConnectionHandler(
                    database,
                    FileProcessorFactory.defaultProcessors(),
                    new ZipService(config.getOutputDirectory()));

            runAcceptLoop(config.getPort(), handler);
        } catch (ServerException ex) {
            System.err.println("Cannot start server: " + ex.getMessage());
            System.exit(2);
        }
    }

    private static void runAcceptLoop(int port, ClientConnectionHandler handler) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server ready, waiting for clients on port " + port + "...");
            while (!Thread.currentThread().isInterrupted()) {
                Socket client = serverSocket.accept();
                System.out.println("Accepted client from " + client.getRemoteSocketAddress());
                handler.handle(client);
                System.out.println("Client handled, waiting for next connection...");
            }
        } catch (IOException ex) {
            System.err.println("Server socket error: " + ex.getMessage());
            System.exit(3);
        }
    }

    private static void logSafely(Database database, String action, String message) {
        try {
            database.log(action, null, "SUCCESS", message);
        } catch (ServerException ex) {
            System.err.println("Warning: startup log entry not persisted: " + ex.getMessage());
        }
    }
}
