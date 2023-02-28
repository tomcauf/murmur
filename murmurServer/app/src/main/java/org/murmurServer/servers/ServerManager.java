package org.murmurServer.servers;

import org.murmurServer.clients.ClientRunnable;
import org.murmurServer.domains.Server;
import org.murmurServer.domains.User;
import org.murmurServer.repositories.IServerRepositories;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ServerManager {
    private final List<ClientRunnable> clientList;
    private final IServerRepositories repositories;
    private final Server server;
    private final MessageDispatch messageDispatch;
    private final TLSSocketFactory tlsSocketFactory;
    private boolean stop = false;
    public ServerManager(IServerRepositories repositories, TLSSocketFactory tlsSocketFactory) {
        this.repositories = repositories;
        this.server = repositories.getServer();
        this.tlsSocketFactory = tlsSocketFactory;
        this.clientList = Collections.synchronizedList(new ArrayList<>());
        this.messageDispatch = new MessageDispatch();
    }
    public void startServer() {
        try {
            SocketManager socketManager = new SocketManager(server.getUnicastPort(), tlsSocketFactory);
            socketManager.start();
            while (!stop) {
                SSLSocket clientSocket = socketManager.acceptClient();
                clientSocket.startHandshake();
                ClientRunnable client = new ClientRunnable(clientSocket, this);
                clientList.add(client);
                new Thread(client).start();
            }
        } catch (Exception e) {
            System.out.println("[!] Error ServerManager.startServer: " + e.getMessage());
        }
    }

    public void broadcastToAllClients(ClientRunnable client, String message) {
        messageDispatch.dispatchMessage(clientList,client, message);
    }
    public void addUser(User user) {
        server.addUser(user);
    }
    public boolean doYouKnowThisUser(User user) {
        return server.doYouKnowThisUser(user);
    }
    public String getDomain() {
        return server.getDomain();
    }
    public User getUserByName(String name) {
        return server.getUserByName(name);
    }
    public void addTagIfNotExist(String tagName) {
        server.addTagIfNotExist(tagName);
    }
    public void addUserToTag(String tagName, String user) {
        server.addUserToTag(tagName,user);
    }
    public void saveServer() {
        repositories.writeServer(server);
    }
}
