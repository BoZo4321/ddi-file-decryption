package com.bozidar.client.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class ClientConfig {

    private final String serverAddress;
    private final int port;
    private final Path inputDirectory;

    private ClientConfig(String serverAddress, int port, Path inputDirectory) {
        this.serverAddress = Objects.requireNonNull(serverAddress, "serverAddress");
        this.port = port;
        this.inputDirectory = Objects.requireNonNull(inputDirectory, "inputDirectory");
    }

    public static ClientConfig fromArgs(String[] args) {
        if (args == null || args.length != 3) {
            throw new IllegalArgumentException(
                    "Expected 3 arguments: <serverAddress> <portNumber> <pathToInputDir>");
        }

        String serverAddress = args[0];
        if (serverAddress.isBlank()) {
            throw new IllegalArgumentException("Server address must not be blank");
        }

        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Port number must be an integer, got: " + args[1], ex);
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port number out of range (1..65535): " + port);
        }

        Path inputDirectory = Paths.get(args[2]);

        return new ClientConfig(serverAddress, port, inputDirectory);
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getPort() {
        return port;
    }

    public Path getInputDirectory() {
        return inputDirectory;
    }

    @Override
    public String toString() {
        return "ClientConfig{serverAddress='" + serverAddress + '\''
                + ", port=" + port
                + ", inputDirectory=" + inputDirectory + '}';
    }
}
