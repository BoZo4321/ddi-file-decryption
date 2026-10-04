package com.bozidar.server.decryption;

import com.bozidar.server.exception.ServerException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TxtFileProcessorTest {

    private final TxtFileProcessor processor = new TxtFileProcessor();

    @Test
    void decrypt_emptyContent_throwsServerException() {
        // "hello" base64-encoded = 5 bytes, not a multiple of 16 → guaranteed IllegalBlockSizeException
        assertThrows(ServerException.class, () -> processor.decrypt("aGVsbG8=".getBytes(), "BozidarSecKey16!"));
    }
}
