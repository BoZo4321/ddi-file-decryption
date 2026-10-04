package com.bozidar.server.network;

import com.bozidar.server.database.Database;
import com.bozidar.server.decryption.FileProcessor;
import com.bozidar.server.decryption.FileProcessorFactory;
import com.bozidar.server.exception.ServerException;
import com.bozidar.server.protocol.DecryptRequest;
import com.bozidar.server.protocol.DecryptResponse;
import com.bozidar.server.protocol.FilePayload;
import com.bozidar.server.protocol.ProtocolReader;
import com.bozidar.server.protocol.ProtocolWriter;
import com.bozidar.server.zip.ZipService;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ClientConnectionHandler {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILURE = "FAILURE";

    private final Database database;
    private final FileProcessorFactory factory;
    private final ZipService zipService;

    public ClientConnectionHandler(Database database, FileProcessorFactory factory, ZipService zipService) {
        this.database = Objects.requireNonNull(database, "database");
        this.factory = Objects.requireNonNull(factory, "factory");
        this.zipService = Objects.requireNonNull(zipService, "zipService");
    }

    public void handle(Socket socket) {
        try (socket) {
            ProtocolReader reader = new ProtocolReader(socket.getInputStream());
            ProtocolWriter writer = new ProtocolWriter(socket.getOutputStream());

            DecryptResponse response = processRequest(reader);
            writer.writeResponse(response);
        } catch (IOException ex) {
            System.err.println("I/O error while handling client " + socket.getRemoteSocketAddress()
                    + ": " + ex.getMessage());
        }
    }

    private DecryptResponse processRequest(ProtocolReader reader) {
        DecryptRequest request;
        try {
            request = reader.readRequest();
        } catch (ServerException | IOException ex) {
            logSafely("REQUEST_RECEIVED", null, STATUS_FAILURE, "Protocol error: " + ex.getMessage());
            return DecryptResponse.failure("Protocol error: " + ex.getMessage());
        }
        logSafely("REQUEST_RECEIVED", null, STATUS_SUCCESS,
                "Files in request: " + request.size());

        List<FilePayload> decrypted;
        try {
            decrypted = decryptAll(request.getFiles());
        } catch (ServerException ex) {
            return DecryptResponse.failure("Decryption failed: " + ex.getMessage());
        }

        Path zipPath;
        try {
            zipPath = zipService.zip(decrypted);
        } catch (ServerException ex) {
            logSafely("ZIP_CREATED", null, STATUS_FAILURE, ex.getMessage());
            return DecryptResponse.failure("Could not create zip: " + ex.getMessage());
        }
        logSafely("ZIP_CREATED", null, STATUS_SUCCESS, "Created " + zipPath);
        logSafely("REQUEST_COMPLETED", null, STATUS_SUCCESS,
                "Processed " + decrypted.size() + " file(s)");

        return DecryptResponse.success(
                decrypted.size(),
                zipPath.toString(),
                "Decrypted " + decrypted.size() + " file(s) and stored zip at " + zipPath);
    }

    private List<FilePayload> decryptAll(List<FilePayload> encryptedFiles) throws ServerException {
        List<FilePayload> result = new ArrayList<>(encryptedFiles.size());
        for (FilePayload file : encryptedFiles) {
            result.add(decryptOne(file));
        }
        return result;
    }

    private FilePayload decryptOne(FilePayload file) throws ServerException {
        String key = lookupKey(file.getName());
        FileProcessor processor = factory.forFileName(file.getName());

        byte[] decrypted;
        try {
            decrypted = processor.decrypt(file.getContent(), key);
        } catch (ServerException ex) {
            logSafely("FILE_DECRYPTED", file.getName(), STATUS_FAILURE, ex.getMessage());
            throw ex;
        }
        logSafely("FILE_DECRYPTED", file.getName(), STATUS_SUCCESS,
                "Decrypted " + decrypted.length + " bytes");
        return new FilePayload(file.getName(), decrypted);
    }

    private String lookupKey(String fileName) throws ServerException {
        Optional<String> key;
        try {
            key = database.findKeyForFile(fileName);
        } catch (ServerException ex) {
            logSafely("KEY_LOOKUP", fileName, STATUS_FAILURE, "Storage error: " + ex.getMessage());
            throw new ServerException("Could not load key for '" + fileName + "'", ex);
        }
        if (key.isEmpty()) {
            logSafely("KEY_LOOKUP", fileName, STATUS_FAILURE, "No key registered for this file");
            throw new ServerException("No decryption key registered for file '" + fileName + "'");
        }
        logSafely("KEY_LOOKUP", fileName, STATUS_SUCCESS, "");
        return key.get();
    }

    private void logSafely(String action, String fileName, String status, String message) {
        try {
            database.log(action, fileName, status, message);
        } catch (ServerException ex) {
            System.err.println("Warning: log entry could not be persisted: " + ex.getMessage());
        }
    }
}
