package org.murmurServer;

import org.murmurServer.servers.ServerFactory;

public class App {
    private static final String CONFIG_FILE_NAME = "server1.json";
    private static final String CERTIFICATE_FILE_NAME = "star.godswila.guru.p12";
    private static final String CERTIFICATE_PASSWORD = "labo2023";

    public static void main(String[] args) {
        System.out.println("[*] Program started");
        ServerFactory serverFactory = new ServerFactory(CONFIG_FILE_NAME, CERTIFICATE_FILE_NAME, CERTIFICATE_PASSWORD);
        serverFactory.createServerAndStartIt();
    }
}
