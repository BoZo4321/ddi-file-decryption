package com.bozidar.server.protocol;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class ProtocolWriter {

    private final BufferedWriter writer;

    public ProtocolWriter(OutputStream output) {
        this.writer = new BufferedWriter(new OutputStreamWriter(output, ProtocolConstants.CHARSET));
    }

    public void writeResponse(DecryptResponse response) throws IOException {
        writeLine(ProtocolConstants.RESPONSE_HEADER);
        writeHeader(ProtocolConstants.HEADER_STATUS,
                response.isSuccess() ? ProtocolConstants.STATUS_SUCCESS : ProtocolConstants.STATUS_FAILURE);
        writeHeader(ProtocolConstants.HEADER_FILES_PROCESSED, Integer.toString(response.getFilesProcessed()));
        writeHeader(ProtocolConstants.HEADER_ZIP_PATH, response.getZipPath());
        writeHeader(ProtocolConstants.HEADER_MESSAGE, response.getMessage());
        writeLine(ProtocolConstants.END_MARKER);
        writer.flush();
    }

    private void writeHeader(String key, String value) throws IOException {
        writeLine(key + ProtocolConstants.KEY_VALUE_SEPARATOR + value);
    }

    private void writeLine(String line) throws IOException {
        writer.write(line);
        writer.write('\n');
    }
}
