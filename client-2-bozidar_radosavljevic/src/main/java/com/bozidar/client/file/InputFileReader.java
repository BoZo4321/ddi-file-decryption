package com.bozidar.client.file;

import com.bozidar.client.exception.ClientException;
import com.bozidar.client.protocol.FilePayload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class InputFileReader {

    private final Path inputDirectory;

    public InputFileReader(Path inputDirectory) {
        this.inputDirectory = Objects.requireNonNull(inputDirectory, "inputDirectory");
    }

    public List<FilePayload> readAll() throws ClientException {
        if (!Files.exists(inputDirectory)) {
            throw new ClientException("Input directory does not exist: " + inputDirectory.toAbsolutePath());
        }
        if (!Files.isDirectory(inputDirectory)) {
            throw new ClientException("Input path is not a directory: " + inputDirectory.toAbsolutePath());
        }

        List<Path> files = new ArrayList<>();
        try (Stream<Path> stream = Files.list(inputDirectory)) {
            stream.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .forEach(files::add);
        } catch (IOException ex) {
            throw new ClientException("Failed to list input directory " + inputDirectory, ex);
        }

        if (files.isEmpty()) {
            throw new ClientException("Input directory contains no files: " + inputDirectory.toAbsolutePath());
        }

        List<FilePayload> payloads = new ArrayList<>(files.size());
        for (Path file : files) {
            payloads.add(readOne(file));
        }
        return payloads;
    }

    private FilePayload readOne(Path file) throws ClientException {
        try {
            byte[] bytes = Files.readAllBytes(file);
            return new FilePayload(file.getFileName().toString(), bytes);
        } catch (IOException ex) {
            throw new ClientException("Failed to read file " + file, ex);
        }
    }
}
