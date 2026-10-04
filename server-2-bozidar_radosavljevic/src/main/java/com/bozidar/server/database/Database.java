package com.bozidar.server.database;

import com.bozidar.server.exception.ServerException;

import java.util.Optional;

public interface Database extends AutoCloseable {

    void ensureLogTableExists() throws ServerException;

    Optional<String> findKeyForFile(String fileName) throws ServerException;

    void log(String action, String fileName, String status, String message) throws ServerException;

    @Override
    void close();
}
