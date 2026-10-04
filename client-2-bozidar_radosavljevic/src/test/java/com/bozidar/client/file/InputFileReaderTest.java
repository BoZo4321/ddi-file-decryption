package com.bozidar.client.file;

import com.bozidar.client.exception.ClientException;
import com.bozidar.client.protocol.FilePayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InputFileReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void readAll_nonExistentDirectory_throwsClientException() {
        assertThrows(ClientException.class,
                () -> new InputFileReader(tempDir.resolve("missing")).readAll());
    }

    @Test
    void readAll_emptyDirectory_throwsClientException() {
        assertThrows(ClientException.class, () -> new InputFileReader(tempDir).readAll());
    }

    @Test
    void readAll_directoryWithFiles_returnsPayloadsSortedByName() throws Exception {
        Files.writeString(tempDir.resolve("b.txt"), "world");
        Files.writeString(tempDir.resolve("a.txt"), "hello");

        List<FilePayload> payloads = new InputFileReader(tempDir).readAll();

        assertEquals(2, payloads.size());
        assertEquals("a.txt", payloads.get(0).getName());
        assertEquals("b.txt", payloads.get(1).getName());
    }
}
