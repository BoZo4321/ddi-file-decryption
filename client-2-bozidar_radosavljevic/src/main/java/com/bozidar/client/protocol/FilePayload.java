package com.bozidar.client.protocol;

import java.util.Objects;

public final class FilePayload {

    private final String name;
    private final byte[] content;

    public FilePayload(String name, byte[] content) {
        this.name = Objects.requireNonNull(name, "name");
        this.content = Objects.requireNonNull(content, "content");
    }

    public String getName() {
        return name;
    }

    public byte[] getContent() {
        return content;
    }

    public int getSize() {
        return content.length;
    }
}
