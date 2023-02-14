package org.murmurServer.servers;

import org.murmurServer.clients.ClientRunnable;
import org.murmurServer.domains.Server;
import org.murmurServer.domains.User;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ServerManager {
    private final List<ClientRunnable> clientList;
    private final Server server;
    private final MessageDispatch messageDispatch;
    private boolean stop = false;
    public ServerManager(Server server) {
        this.server = server;
        this.clientList = Collections.synchronizedList(new ArrayList<>());
        this.messageDispatch = new MessageDispatch();
    }
    public void startServer() {
        try {
            SocketManager socketManager = new SocketManager(server.getUnicastPort());
            socketManager.start();
            while (!stop) {
                Socket clientSocket = socketManager.acceptClient();
                ClientRunnable client = new ClientRunnable(clientSocket, this);
                clientList.add(client);
                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
}
