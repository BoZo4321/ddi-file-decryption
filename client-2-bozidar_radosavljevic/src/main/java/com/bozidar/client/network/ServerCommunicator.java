package com.bozidar.client.network;

import com.bozidar.client.exception.ClientException;
import com.bozidar.client.protocol.DecryptResponse;
import com.bozidar.client.protocol.FilePayload;
import com.bozidar.client.protocol.ProtocolReader;
import com.bozidar.client.protocol.ProtocolWriter;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;

public class ServerCommunicator {

    private final String host;
    private final int port;

    public ServerCommunicator(String host, int port) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
    }

    public DecryptResponse exchange(List<FilePayload> files) throws ClientException {
        try (Socket socket = new Socket(host, port)) {
            ProtocolWriter writer = new ProtocolWriter(socket.getOutputStream());
            writer.writeRequest(files);

            ProtocolReader reader = new ProtocolReader(socket.getInputStream());
            return reader.readResponse();
        } catch (UnknownHostException ex) {
            throw new ClientException("Unknown server host: " + host, ex);
        } catch (IOException ex) {
            throw new ClientException("Network error talking to " + host + ":" + port + " - " + ex.getMessage(), ex);
        }
    }
}
