package com.bozidar.client.protocol;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Base64;
import java.util.List;

public class ProtocolWriter {

    private final BufferedWriter writer;

    public ProtocolWriter(OutputStream output) {
        this.writer = new BufferedWriter(new OutputStreamWriter(output, ProtocolConstants.CHARSET));
    }

    public void writeRequest(List<FilePayload> files) throws IOException {
        writeLine(ProtocolConstants.REQUEST_HEADER);
        writeHeader(ProtocolConstants.HEADER_FILE_COUNT, Integer.toString(files.size()));

        for (FilePayload file : files) {
            writeLine(ProtocolConstants.FILE_SEPARATOR);
            writeHeader(ProtocolConstants.HEADER_NAME, file.getName());
            writeHeader(ProtocolConstants.HEADER_SIZE, Integer.toString(file.getSize()));
            writeLine(ProtocolConstants.CONTENT_MARKER);
            writeLine(Base64.getEncoder().encodeToString(file.getContent()));
        }

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
