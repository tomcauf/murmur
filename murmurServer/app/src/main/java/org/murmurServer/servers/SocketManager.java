package org.murmurServer.servers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketManager {
    private ServerSocket serverSocket;
    private int port;

    public SocketManager(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.printf("[*] Server started on %s and port %d\n",serverSocket.getInetAddress().getHostAddress(),port);
    }

    public Socket acceptClient() throws IOException {
        return serverSocket.accept();
    }

    public void stop() throws IOException {
        serverSocket.close();
    }
}
