package org.helmo.reseau.servers;

import org.helmo.reseau.repositories.IServerRepositories;
import org.helmo.reseau.clients.ClientRunnable;
import org.helmo.reseau.clients.RelayRunnable;
import org.helmo.reseau.domains.Server;
import org.helmo.reseau.domains.User;
import org.helmo.reseau.grammar.Protocol;
import org.helmo.reseau.tasks.TaskExecutor;
import org.helmo.reseau.tasks.TaskManager;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ServerManager {
    private IServerRepositories repositories;
    private TLSSocketFactory tlsSocketFactory;
    private TaskManager taskManager;
    private List<ClientRunnable> clientList;
    private Server server;

    public ServerManager(IServerRepositories repositories, TLSSocketFactory tlsSocketFactory, TaskManager taskManager){
        this.repositories = repositories;
        this.server = repositories.getServer();
        this.tlsSocketFactory = tlsSocketFactory;
        this.taskManager = taskManager;
        this.clientList = Collections.synchronizedList(new ArrayList<>());
    }

    public void startServer() {
        SSLServerSocketFactory sslServerSocketFactory = tlsSocketFactory.getServerSocketFactory();
        Protocol protocol = new Protocol();

        try(SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(server.getUnicastPort(), 100, InetAddress.getByName(server.getDomain()))) {
            System.out.println("[*] Server started at " + server.getDomain() + ":" + server.getUnicastPort());
            new Thread(new TaskExecutor(taskManager, this, protocol)).start();
            //TODO: Voir avec Ahmed si c'est bien ça ?
            //RelayRunnable relayRunnable = new RelayRunnable(server.getDomain(),server.getMulticastPort(),server.getMulticastAddress(),server.getRelayPort());
            //Thread networkSelectorThread = new Thread(relayRunnable);
            //networkSelectorThread.start();
            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                System.out.println("[+] New client connected");

                ClientRunnable client = new ClientRunnable(clientSocket, this, protocol);
                clientList.add(client);
                (new Thread(client)).start();
            }

        } catch (IOException e){
            System.out.println("[!] Error 1ServerManager.startServer: " + e.getMessage());
        } catch (Exception e){
            System.out.println("[!] Error 2ServerManager.startServer: " + Arrays.toString(e.getStackTrace()));
        }
    }
    public void closeClient(ClientRunnable client){
        client.close();
        clientList.remove(client);
    }
    public boolean registerUser(User user) {
        if (server.hasUser(user)) {
            return false;
        } else {
            server.addUser(user);
            repositories.writeServer(server);
            return true;
        }
    }
    public void saveServer() {
        repositories.writeServer(server);
    }
    public User getUser(String name) {
        return server.getUser(name);
    }

    public String getServerDomain() {
        return server.getDomain();
    }

    public void createTask(ClientRunnable clientRunnable, String[] message) {
        System.out.println("[+] Creating task");
        taskManager.createTask(clientRunnable, message);
    }
    public List<String> getFollowers(String tag) {
        return server.getFollowers(tag);
    }

    public void addTag(String name) {
        if (!server.hasTag(name))
            server.addTag(name);
    }

    public ClientRunnable getClient(String name) {
        for (ClientRunnable client : clientList) {
            if (client.getUsername().equals(name)) {
                return client;
            }
        }
        return null;
    }

    public void addFollowedTag(String follow, String tag) {
        server.addFollowedTag(follow, tag);
    }
}
