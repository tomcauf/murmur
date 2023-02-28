package org.murmurServer.servers;

import org.murmurServer.clients.ClientRunnable;
import org.murmurServer.clients.Entity;
import org.murmurServer.domains.Server;
import org.murmurServer.domains.User;
import org.murmurServer.grammar.Protocol;
import org.murmurServer.repositories.IServerRepositories;
import org.murmurServer.tasks.TaskExecutor;
import org.murmurServer.tasks.TaskManager;

import javax.net.ssl.SSLSocket;
import java.util.*;

public class ServerManager {
    private final List<ClientRunnable> clientList;
    private final IServerRepositories repositories;
    private final Server server;
    private final MessageDispatch messageDispatch;
    private final TLSSocketFactory tlsSocketFactory;
    private TaskManager taskManager;
    private TaskExecutor taskExecutor;
    private Protocol protocol;
    private boolean stop = false;

    public ServerManager(IServerRepositories repositories, TLSSocketFactory tlsSocketFactory) {
        this.repositories = repositories;
        this.server = repositories.getServer();
        this.tlsSocketFactory = tlsSocketFactory;
        this.clientList = Collections.synchronizedList(new ArrayList<>());
        this.messageDispatch = new MessageDispatch();
        this.protocol = new Protocol();
        this.taskManager = new TaskManager(protocol);
        this.taskExecutor = new TaskExecutor(taskManager, repositories, this, messageDispatch, protocol);
    }

    public void startServer() {
        try {
            SocketManager socketManager = new SocketManager(server.getUnicastPort(), tlsSocketFactory);
            socketManager.start();
            new Thread(taskExecutor).start();
            while (!stop) {
                SSLSocket clientSocket = socketManager.acceptClient();
                System.out.println("[+] New client connected");
                clientSocket.startHandshake();
                ClientRunnable client = new ClientRunnable(clientSocket, this);
                clientList.add(client);
                new Thread(client).start();
            }
        } catch (Exception e) {
            System.out.println("[!] Error ServerManager.startServer: " + e.getMessage());
        }
    }

    //********** TASKS **********//
    public void createTask(String requete, Entity author){
        taskManager.createTask(requete, author);
    }
    //********** END TASKS **********//

    //TODO: Vérifier si ça doit disparaitre (toute celle qui sont en dessous du TODO)
    public void broadcastToAllClients(ClientRunnable client, String message) {
        messageDispatch.dispatchMessage(clientList, client, message);
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
        server.addUserToTag(tagName, user);
    }

    public void saveServer() {
        repositories.writeServer(server);
    }

    public Entity getClient(String entity) {
        for (ClientRunnable client : clientList) {
            if (client.getName().equals(entity)) {
                return client;
            }
        }
        return null;
    }

    public void createHelloTask(ClientRunnable clientRunnable, String randomString) {
        try {
            String helloMessage = protocol.buildHello(server.getDomain(),randomString);
            taskManager.createTask(helloMessage, clientRunnable);
        }catch (Exception e){
            System.out.println("[!] Error ServerManager.createHelloTask: " + e.getMessage());
        }
    }
}
