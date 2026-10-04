package com.bozidar.server.database;

import com.bozidar.server.exception.ServerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteDatabaseTest {

    private Path tempDb;
    private SQLiteDatabase database;

    @BeforeEach
    void setUp() throws Exception {
        tempDb = Files.createTempFile("test_db", ".sqlite3");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + tempDb.toAbsolutePath())) {
            conn.createStatement().executeUpdate(
                    "CREATE TABLE AES_KEYS (FILE TEXT NOT NULL, KEY TEXT NOT NULL)");
        }
        database = new SQLiteDatabase(tempDb);
        database.ensureLogTableExists();
    }

    @AfterEach
    void tearDown() throws Exception {
        database.close();
        Files.deleteIfExists(tempDb);
    }

    @Test
    void findKeyForFile_noMatchingEntry_returnsEmpty() throws ServerException {
        Optional<String> key = database.findKeyForFile("unknown_file.txt");
        assertTrue(key.isEmpty());
    }

    @Test
    void findKeyForFile_nonExistentDbFile_throwsServerException() {
        assertThrows(ServerException.class, () -> new SQLiteDatabase(Path.of("does_not_exist.sqlite3")));
    }
}
