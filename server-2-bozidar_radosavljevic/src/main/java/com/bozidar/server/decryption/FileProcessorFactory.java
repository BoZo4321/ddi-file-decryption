package com.bozidar.server.decryption;

import com.bozidar.server.exception.ServerException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class FileProcessorFactory {

    private final Map<String, FileProcessor> processorsByExtension;

    public FileProcessorFactory(List<FileProcessor> processors) {
        Map<String, FileProcessor> map = new HashMap<>();
        for (FileProcessor processor : processors) {
            for (String extension : processor.supportedExtensions()) {
                map.put(extension.toLowerCase(Locale.ROOT), processor);
            }
        }
        this.processorsByExtension = Collections.unmodifiableMap(map);
    }

    public static FileProcessorFactory defaultProcessors() {
        return new FileProcessorFactory(Arrays.asList(
                new TxtFileProcessor(),
                new XlsxFileProcessor()));
    }

    public FileProcessor forFileName(String fileName) throws ServerException {
        String extension = extractExtension(fileName);
        FileProcessor processor = processorsByExtension.get(extension);
        if (processor == null) {
            throw new ServerException(
                    "Unsupported file format '" + extension + "' for file '" + fileName + "'. "
                            + "Supported extensions: " + supportedExtensions());
        }
        return processor;
    }

    public Set<String> supportedExtensions() {
        return new TreeSet<>(processorsByExtension.keySet());
    }

    private String extractExtension(String fileName) throws ServerException {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new ServerException("File '" + fileName + "' has no extension");
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
