package com.bozidar.server.protocol;

import com.bozidar.server.exception.ServerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ProtocolReader {

    private final BufferedReader reader;

    public ProtocolReader(InputStream input) {
        this.reader = new BufferedReader(new InputStreamReader(input, ProtocolConstants.CHARSET));
    }

    public DecryptRequest readRequest() throws ServerException, IOException {
        String header = readLineOrFail("request header");
        if (!ProtocolConstants.REQUEST_HEADER.equals(header)) {
            throw new ServerException("Unexpected request header: '" + header + "'");
        }

        int fileCount = parseIntHeader(ProtocolConstants.HEADER_FILE_COUNT);

        List<FilePayload> files = new ArrayList<>(fileCount);
        for (int i = 0; i < fileCount; i++) {
            files.add(readFile(i));
        }

        String terminator = readLineOrFail("END marker");
        if (!ProtocolConstants.END_MARKER.equals(terminator)) {
            throw new ServerException("Expected END marker, got: '" + terminator + "'");
        }

        return new DecryptRequest(files);
    }

    private FilePayload readFile(int index) throws ServerException, IOException {
        String separator = readLineOrFail("file separator (index " + index + ")");
        if (!ProtocolConstants.FILE_SEPARATOR.equals(separator)) {
            throw new ServerException(
                    "Expected '" + ProtocolConstants.FILE_SEPARATOR + "' before file " + index
                            + ", got: '" + separator + "'");
        }

        String name = parseStringHeader(ProtocolConstants.HEADER_NAME);
        int declaredSize = parseIntHeader(ProtocolConstants.HEADER_SIZE);

        String contentMarker = readLineOrFail("CONTENT_BASE64 marker for file '" + name + "'");
        if (!ProtocolConstants.CONTENT_MARKER.equals(contentMarker)) {
            throw new ServerException(
                    "Expected '" + ProtocolConstants.CONTENT_MARKER + "' for file '" + name
                            + "', got: '" + contentMarker + "'");
        }

        String base64Content = readLineOrFail("base64 content for file '" + name + "'");
        byte[] content;
        try {
            content = Base64.getDecoder().decode(base64Content);
        } catch (IllegalArgumentException ex) {
            throw new ServerException("File '" + name + "' has invalid Base64 payload", ex);
        }

        if (content.length != declaredSize) {
            throw new ServerException(
                    "File '" + name + "' declared SIZE=" + declaredSize
                            + " but Base64 payload decoded to " + content.length + " bytes");
        }

        return new FilePayload(name, content);
    }

    private String parseStringHeader(String expectedKey) throws ServerException, IOException {
        String line = readLineOrFail(expectedKey + " header");
        return parseKeyValue(line, expectedKey);
    }

    private int parseIntHeader(String expectedKey) throws ServerException, IOException {
        String value = parseStringHeader(expectedKey);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new ServerException(expectedKey + " is not an integer: '" + value + "'", ex);
        }
    }

    private String parseKeyValue(String line, String expectedKey) throws ServerException {
        int separatorIndex = line.indexOf(ProtocolConstants.KEY_VALUE_SEPARATOR);
        if (separatorIndex < 0) {
            throw new ServerException("Expected '" + expectedKey + ": <value>', got: '" + line + "'");
        }
        String actualKey = line.substring(0, separatorIndex);
        if (!expectedKey.equals(actualKey)) {
            throw new ServerException("Expected header '" + expectedKey + "', got: '" + actualKey + "'");
        }
        return line.substring(separatorIndex + ProtocolConstants.KEY_VALUE_SEPARATOR.length());
    }

    private String readLineOrFail(String what) throws ServerException, IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new ServerException("Unexpected end of stream while reading " + what);
        }
        return line;
    }
}
