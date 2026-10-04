package com.bozidar.server.zip;

import com.bozidar.server.exception.ServerException;
import com.bozidar.server.protocol.FilePayload;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipService {

    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");

    private final Path outputDirectory;

    public ZipService(Path outputDirectory) {
        this.outputDirectory = Objects.requireNonNull(outputDirectory, "outputDirectory");
    }

    public Path zip(List<FilePayload> files) throws ServerException {
        Path target = prepareTargetPath();

        try (OutputStream fileStream = Files.newOutputStream(target);
             ZipOutputStream zipStream = new ZipOutputStream(fileStream)) {
            for (FilePayload file : files) {
                ZipEntry entry = new ZipEntry(file.getName());
                zipStream.putNextEntry(entry);
                zipStream.write(file.getContent());
                zipStream.closeEntry();
            }
        } catch (IOException ex) {
            throw new ServerException("Failed to write zip archive at " + target, ex);
        }

        return target.toAbsolutePath();
    }

    private Path prepareTargetPath() throws ServerException {
        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException ex) {
            throw new ServerException("Could not create output directory " + outputDirectory, ex);
        }
        String name = "decrypted_" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".zip";
        return outputDirectory.resolve(name);
    }
}
