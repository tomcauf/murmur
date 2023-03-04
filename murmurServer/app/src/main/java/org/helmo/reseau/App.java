package org.helmo.reseau;

public class App {
    private static final String CONFIG_FILE_NAME = "server1.json";
    private static final String CERTIFICATE_FILE_NAME = "star.godswila.guru.p12";
    private static final String CERTIFICATE_PASSWORD = "labo2023";

    public static void main(String[] args) {
        (new Thread(new MulticastRunnable())).start();
        (new Thread(new Multicast2Runnable())).start();
        (new Thread(new RelayRunnable())).start();
        (new Thread(new Server2Runnable())).start();

/*        System.setProperty("javax.net.debug", "all");
        System.out.println("[*] Program started");
        ServerFactory serverFactory = new ServerFactory(CONFIG_FILE_NAME, CERTIFICATE_FILE_NAME, CERTIFICATE_PASSWORD);
        serverFactory.createServerAndStartIt();*/

    }
}
