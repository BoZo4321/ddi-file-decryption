package com.bozidar.server.decryption;

import com.bozidar.encryption.suite.Encryptor;
import com.bozidar.server.exception.ServerException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

public class TxtFileProcessor implements FileProcessor {

    private static final Set<String> EXTENSIONS = Collections.singleton("txt");

    @Override
    public Set<String> supportedExtensions() {
        return EXTENSIONS;
    }

    @Override
    public byte[] decrypt(byte[] encryptedContent, String key) throws ServerException {
        String cipherText = new String(encryptedContent, StandardCharsets.UTF_8);

        String plainText;
        try {
            plainText = Encryptor.decrypt(key, cipherText);
        } catch (RuntimeException ex) {
            throw new ServerException("Decryption error for .txt payload: " + ex.getMessage(), ex);
        }
        if (plainText == null || plainText.isEmpty()) {
            throw new ServerException(
                    "Decryption produced no content for .txt payload - key may be wrong or content corrupted");
        }

        return plainText.getBytes(StandardCharsets.UTF_8);
    }
}
