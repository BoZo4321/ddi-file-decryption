package com.bozidar.server.database;

import com.bozidar.server.exception.ServerException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SQLiteDatabase implements Database {

    private static final String SELECT_KEY_SQL =
            "SELECT KEY FROM AES_KEYS WHERE FILE = ? LIMIT 1";

    private static final String CREATE_LOG_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS APP_LOGS ("
                    + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "TIMESTAMP TEXT NOT NULL,"
                    + "ACTION TEXT NOT NULL,"
                    + "FILE_NAME TEXT,"
                    + "STATUS TEXT NOT NULL,"
                    + "MESSAGE TEXT"
                    + ")";

    private static final String INSERT_LOG_SQL =
            "INSERT INTO APP_LOGS (TIMESTAMP, ACTION, FILE_NAME, STATUS, MESSAGE) VALUES (?, ?, ?, ?, ?)";

    private final Connection connection;

    public SQLiteDatabase(Path databasePath) throws ServerException {
        if (!Files.exists(databasePath)) {
            throw new ServerException("SQLite database file not found: " + databasePath.toAbsolutePath());
        }
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath.toAbsolutePath());
        } catch (SQLException ex) {
            throw new ServerException("Could not open SQLite connection to " + databasePath, ex);
        }
    }

    @Override
    public void ensureLogTableExists() throws ServerException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_LOG_TABLE_SQL);
        } catch (SQLException ex) {
            throw new ServerException("Failed to create APP_LOGS table", ex);
        }
    }

    @Override
    public Optional<String> findKeyForFile(String fileName) throws ServerException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_KEY_SQL)) {
            statement.setString(1, fileName);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.ofNullable(rs.getString("KEY")) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new ServerException("Failed to look up key for file '" + fileName + "'", ex);
        }
    }

    @Override
    public void log(String action, String fileName, String status, String message) throws ServerException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_LOG_SQL)) {
            statement.setString(1, DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
            statement.setString(2, action);
            if (fileName == null) {
                statement.setNull(3, Types.VARCHAR);
            } else {
                statement.setString(3, fileName);
            }
            statement.setString(4, status);
            statement.setString(5, message == null ? "" : message);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new ServerException("Failed to insert APP_LOGS row for action " + action, ex);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            System.err.println("Warning: failed to close SQLite connection cleanly: " + ex.getMessage());
        }
    }
}
