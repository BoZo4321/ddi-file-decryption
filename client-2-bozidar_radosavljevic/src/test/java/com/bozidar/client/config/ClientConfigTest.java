package com.bozidar.client.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientConfigTest {

    @Test
    void fromArgs_validArgs_parsesAllFields() {
        ClientConfig config = ClientConfig.fromArgs(new String[]{"localhost", "9090", "./input"});
        assertEquals("localhost", config.getServerAddress());
        assertEquals(9090, config.getPort());
        assertEquals("input", config.getInputDirectory().getFileName().toString());
    }

    @Test
    void fromArgs_invalidArgs_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ClientConfig.fromArgs(null));
        assertThrows(IllegalArgumentException.class,
                () -> ClientConfig.fromArgs(new String[]{"   ", "9090", "./input"}));
        assertThrows(IllegalArgumentException.class,
                () -> ClientConfig.fromArgs(new String[]{"localhost", "abc", "./input"}));
    }
}
