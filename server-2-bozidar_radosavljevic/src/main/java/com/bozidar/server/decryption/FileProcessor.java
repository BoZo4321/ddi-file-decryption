package com.bozidar.server.decryption;

import com.bozidar.server.exception.ServerException;

import java.util.Set;

public interface FileProcessor {

    Set<String> supportedExtensions();

    byte[] decrypt(byte[] encryptedContent, String key) throws ServerException;
}
