package com.bozidar.server;

import com.bozidar.encryption.suite.Encryptor;
import com.bozidar.server.database.SQLiteDatabase;
import com.bozidar.server.decryption.FileProcessorFactory;
import com.bozidar.server.network.ClientConnectionHandler;
import com.bozidar.server.protocol.ProtocolConstants;
import com.bozidar.server.zip.ZipService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.zip.*;

import static org.junit.jupiter.api.Assertions.*;

class EndToEndTest {

    private static final String TEST_KEY = "BozidarSecKey16!";

    private Path tempDb;
    private Path outputDir;

    @BeforeEach
    void setUp() throws Exception {
        tempDb = Files.createTempFile("e2e_db", ".sqlite3");
        outputDir = Files.createTempDirectory("e2e_output");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + tempDb.toAbsolutePath())) {
            conn.createStatement().executeUpdate(
                    "CREATE TABLE AES_KEYS (FILE TEXT NOT NULL, KEY TEXT NOT NULL)");
            PreparedStatement ps = conn.prepareStatement("INSERT INTO AES_KEYS VALUES (?, ?)");
            ps.setString(1, "test.txt");
            ps.setString(2, TEST_KEY);
            ps.executeUpdate();
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (Files.exists(outputDir)) {
            try (Stream<Path> files = Files.walk(outputDir)) {
                files.sorted(Comparator.reverseOrder())
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            }
        }
        Files.deleteIfExists(tempDb);
    }

    @Test
    void fullFlow_txtFile_successResponseAndZipWithDecryptedContent() throws Exception {
        String plainText = "Hello, Bluecat DDI!";
        byte[] encryptedBytes = Encryptor.encrypt(TEST_KEY, plainText).getBytes(StandardCharsets.UTF_8);

        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int port = serverSocket.getLocalPort();

            CompletableFuture.runAsync(() -> {
                try (SQLiteDatabase db = new SQLiteDatabase(tempDb)) {
                    db.ensureLogTableExists();
                    new ClientConnectionHandler(db, FileProcessorFactory.defaultProcessors(), new ZipService(outputDir))
                            .handle(serverSocket.accept());
                } catch (Exception ignored) {}
            });

            try (Socket socket = new Socket("localhost", port)) {
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                w.write(ProtocolConstants.REQUEST_HEADER + "\n");
                w.write(ProtocolConstants.HEADER_FILE_COUNT + ProtocolConstants.KEY_VALUE_SEPARATOR + "1\n");
                w.write(ProtocolConstants.FILE_SEPARATOR + "\n");
                w.write(ProtocolConstants.HEADER_NAME + ProtocolConstants.KEY_VALUE_SEPARATOR + "test.txt\n");
                w.write(ProtocolConstants.HEADER_SIZE + ProtocolConstants.KEY_VALUE_SEPARATOR + encryptedBytes.length + "\n");
                w.write(ProtocolConstants.CONTENT_MARKER + "\n");
                w.write(Base64.getEncoder().encodeToString(encryptedBytes) + "\n");
                w.write(ProtocolConstants.END_MARKER + "\n");
                w.flush();

                BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                r.readLine(); // DDI/1.0 RESPONSE
                Map<String, String> response = new LinkedHashMap<>();
                String line;
                while ((line = r.readLine()) != null && !line.equals(ProtocolConstants.END_MARKER)) {
                    int sep = line.indexOf(ProtocolConstants.KEY_VALUE_SEPARATOR);
                    if (sep >= 0) response.put(line.substring(0, sep), line.substring(sep + ProtocolConstants.KEY_VALUE_SEPARATOR.length()));
                }

                assertEquals("SUCCESS", response.get("STATUS"));
                assertEquals("1", response.get("FILES_PROCESSED"));

                Path zipPath = Path.of(response.get("ZIP_PATH"));
                assertTrue(Files.exists(zipPath));
                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                    zis.getNextEntry();
                    assertEquals(plainText, new String(zis.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        }
    }
}
