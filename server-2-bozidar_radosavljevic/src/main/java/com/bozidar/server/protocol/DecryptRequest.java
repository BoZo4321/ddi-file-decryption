package com.bozidar.server.protocol;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DecryptRequest {

    private final List<FilePayload> files;

    public DecryptRequest(List<FilePayload> files) {
        Objects.requireNonNull(files, "files");
        this.files = Collections.unmodifiableList(files);
    }

    public List<FilePayload> getFiles() {
        return files;
    }

    public int size() {
        return files.size();
    }
}
