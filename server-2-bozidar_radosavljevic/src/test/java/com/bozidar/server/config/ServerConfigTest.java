package com.bozidar.server.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerConfigTest {

    @Test
    void fromArgs_validArgs_parsesAllFields() {
        ServerConfig config = ServerConfig.fromArgs(new String[]{"9090", "./db.sqlite3", "./output"});
        assertEquals(9090, config.getPort());
        assertEquals("db.sqlite3", config.getDatabasePath().getFileName().toString());
        assertEquals("output", config.getOutputDirectory().getFileName().toString());
    }

    @Test
    void fromArgs_invalidPort_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> ServerConfig.fromArgs(new String[]{"0", "./db.sqlite3", "./out"}));
        assertThrows(IllegalArgumentException.class,
                () -> ServerConfig.fromArgs(new String[]{"abc", "./db.sqlite3", "./out"}));
    }
}
