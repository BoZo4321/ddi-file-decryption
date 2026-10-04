package com.bozidar.server.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class ServerConfig {

    private final int port;
    private final Path databasePath;
    private final Path outputDirectory;

    private ServerConfig(int port, Path databasePath, Path outputDirectory) {
        this.port = port;
        this.databasePath = Objects.requireNonNull(databasePath, "databasePath");
        this.outputDirectory = Objects.requireNonNull(outputDirectory, "outputDirectory");
    }

    public static ServerConfig fromArgs(String[] args) {
        if (args == null || args.length != 3) {
            throw new IllegalArgumentException(
                    "Expected 3 arguments: <portNumber> <pathToDatabase> <pathToOutputDir>");
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Port number must be an integer, not " + args[0], ex);
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port number out of range (1-65535): " + port);
        }

        Path databasePath = Paths.get(args[1]);
        Path outputDirectory = Paths.get(args[2]);

        return new ServerConfig(port, databasePath, outputDirectory);
    }

    public int getPort() {
        return port;
    }

    public Path getDatabasePath() {
        return databasePath;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public String toString() {
        return "ServerConfig{port=" + port
                + ", databasePath=" + databasePath
                + ", outputDirectory=" + outputDirectory + '}';
    }
}
