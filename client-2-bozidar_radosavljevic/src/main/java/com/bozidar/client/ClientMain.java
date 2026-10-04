package com.bozidar.client;

import com.bozidar.client.config.ClientConfig;
import com.bozidar.client.exception.ClientException;
import com.bozidar.client.file.InputFileReader;
import com.bozidar.client.network.ServerCommunicator;
import com.bozidar.client.protocol.DecryptResponse;
import com.bozidar.client.protocol.FilePayload;

import java.util.List;

public class ClientMain {

    public static void main(String[] args) {
        ClientConfig config;
        try {
            config = ClientConfig.fromArgs(args);
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid arguments: " + ex.getMessage());
            System.err.println("Usage: java -jar client_2.jar <serverAddress> <portNumber> <pathToInputDir>");
            System.exit(1);
            return;
        }

        System.out.println("Client starting with " + config);

        try {
            List<FilePayload> files = new InputFileReader(config.getInputDirectory()).readAll();
            System.out.println("Read " + files.size() + " file(s) from " + config.getInputDirectory());

            DecryptResponse response = new ServerCommunicator(config.getServerAddress(), config.getPort())
                    .exchange(files);
            printOutcome(response);

            System.exit(response.isSuccess() ? 0 : 4);
        } catch (ClientException ex) {
            System.err.println("FAILURE: " + ex.getMessage());
            System.exit(2);
        }
    }

    private static void printOutcome(DecryptResponse response) {
        if (response.isSuccess()) {
            System.out.println("SUCCESS");
            System.out.println("  Files processed : " + response.getFilesProcessed());
            System.out.println("  Zip stored at   : " + response.getZipPath());
            System.out.println("  Message         : " + response.getMessage());
        } else {
            System.out.println("FAILURE");
            System.out.println("  Message: " + response.getMessage());
        }
    }
}
