package com.bozidar.client.protocol;

import com.bozidar.client.exception.ClientException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProtocolReader {

    private final BufferedReader reader;

    public ProtocolReader(InputStream input) {
        this.reader = new BufferedReader(new InputStreamReader(input, ProtocolConstants.CHARSET));
    }

    public DecryptResponse readResponse() throws ClientException, IOException {
        String header = readLineOrFail("response header");
        if (!ProtocolConstants.RESPONSE_HEADER.equals(header)) {
            throw new ClientException("Unexpected response header: '" + header + "'");
        }

        String status = parseStringHeader(ProtocolConstants.HEADER_STATUS);
        int filesProcessed = parseIntHeader(ProtocolConstants.HEADER_FILES_PROCESSED);
        String zipPath = parseStringHeader(ProtocolConstants.HEADER_ZIP_PATH);
        String message = parseStringHeader(ProtocolConstants.HEADER_MESSAGE);

        String terminator = readLineOrFail("END marker");
        if (!ProtocolConstants.END_MARKER.equals(terminator)) {
            throw new ClientException("Expected END marker, got: '" + terminator + "'");
        }

        boolean success = ProtocolConstants.STATUS_SUCCESS.equals(status);
        return new DecryptResponse(success, filesProcessed, zipPath, message);
    }

    private String parseStringHeader(String expectedKey) throws ClientException, IOException {
        String line = readLineOrFail(expectedKey + " header");
        int separatorIndex = line.indexOf(ProtocolConstants.KEY_VALUE_SEPARATOR);
        if (separatorIndex < 0) {
            throw new ClientException("Expected '" + expectedKey + ": <value>', got: '" + line + "'");
        }
        String actualKey = line.substring(0, separatorIndex);
        if (!expectedKey.equals(actualKey)) {
            throw new ClientException("Expected header '" + expectedKey + "', got: '" + actualKey + "'");
        }
        return line.substring(separatorIndex + ProtocolConstants.KEY_VALUE_SEPARATOR.length());
    }

    private int parseIntHeader(String expectedKey) throws ClientException, IOException {
        String value = parseStringHeader(expectedKey);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new ClientException(expectedKey + " is not an integer: '" + value + "'", ex);
        }
    }

    private String readLineOrFail(String what) throws ClientException, IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new ClientException("Unexpected end of stream while reading " + what);
        }
        return line;
    }
}
