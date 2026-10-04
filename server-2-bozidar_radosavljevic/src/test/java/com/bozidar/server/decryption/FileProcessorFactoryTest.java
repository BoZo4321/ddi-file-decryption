package com.bozidar.server.decryption;

import com.bozidar.server.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileProcessorFactoryTest {

    private FileProcessorFactory factory;

    @BeforeEach
    void setUp() {
        factory = FileProcessorFactory.defaultProcessors();
    }

    @Test
    void forFileName_txtFile_returnsTxtProcessor() throws ServerException {
        assertTrue(factory.forFileName("file.txt") instanceof TxtFileProcessor);
    }

    @Test
    void forFileName_xlsxFile_returnsXlsxProcessor() throws ServerException {
        assertTrue(factory.forFileName("file.xlsx") instanceof XlsxFileProcessor);
    }

    @Test
    void forFileName_unsupportedExtension_throwsServerException() {
        assertThrows(ServerException.class, () -> factory.forFileName("file.pdf"));
        assertThrows(ServerException.class, () -> factory.forFileName("README"));
    }
}
