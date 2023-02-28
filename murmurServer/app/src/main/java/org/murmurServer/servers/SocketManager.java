package org.murmurServer.servers;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;

public class SocketManager {
    private SSLServerSocket serverSocket;
    private TLSSocketFactory tlsSocketFactory;
    private int port;

    public SocketManager(int port, TLSSocketFactory tlsSocketFactory){
        this.port = port;
        this.tlsSocketFactory = tlsSocketFactory;
    }

    public void start() throws Exception {
        SSLContext sslContext = tlsSocketFactory.getSSLContext();

        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        //TODO: Doit récupérer l'adresse IP du fichier config => server1.godswila.guru
        serverSocket = (SSLServerSocket) ssf.createServerSocket(port, 100, InetAddress.getByName("server1.godswila.guru"));
        System.out.printf("[*] Server started on %s and port %d\n",serverSocket.getInetAddress().getHostName(),port);
    }

    public SSLSocket acceptClient() throws IOException {
        return (SSLSocket) serverSocket.accept();
    }

    public void stop() throws IOException {
        serverSocket.close();
    }
}
