package com.bozidar.server.protocol;

import java.util.Objects;

public final class DecryptResponse {

    private final boolean success;
    private final int filesProcessed;
    private final String zipPath;
    private final String message;

    private DecryptResponse(boolean success, int filesProcessed, String zipPath, String message) {
        this.success = success;
        this.filesProcessed = filesProcessed;
        this.zipPath = zipPath == null ? "" : zipPath;
        this.message = Objects.requireNonNull(message, "message");
    }

    public static DecryptResponse success(int filesProcessed, String zipPath, String message) {
        return new DecryptResponse(true, filesProcessed, zipPath, message);
    }

    public static DecryptResponse failure(String message) {
        return new DecryptResponse(false, 0, "", message);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getFilesProcessed() {
        return filesProcessed;
    }

    public String getZipPath() {
        return zipPath;
    }

    public String getMessage() {
        return message;
    }
}
