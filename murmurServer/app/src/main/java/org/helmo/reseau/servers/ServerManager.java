package org.helmo.reseau.servers;

import org.helmo.reseau.domains.Tag;
import org.helmo.reseau.repositories.IServerRepositories;
import org.helmo.reseau.clients.ClientRunnable;
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

public class ServerManager implements Runnable{
    private IServerRepositories repositories;
    private TLSSocketFactory tlsSocketFactory;
    private TaskManager taskManager;
    private List<ClientRunnable> clientList;
    private Server server;
    private RelayManager relayManager;

    public ServerManager(IServerRepositories repositories, TLSSocketFactory tlsSocketFactory, TaskManager taskManager, RelayManager relayManager){
        this.repositories = repositories;
        this.server = repositories.getServer();
        this.tlsSocketFactory = tlsSocketFactory;
        this.taskManager = taskManager;
        this.clientList = Collections.synchronizedList(new ArrayList<>());
        this.relayManager = relayManager;
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

    public boolean addFollowedTag(String follow, String tag) {
        return server.addFollowedTag(follow, tag);
    }
    public boolean addFollower(String destName, String sender) {
        return server.addFollower(destName, sender);
    }

    public void sendMessageToRelay(String s){
        relayManager.sendMessageToRelay(s);
    }

    @Override
    public void run() {
        SSLServerSocketFactory sslServerSocketFactory = tlsSocketFactory.getServerSocketFactory();
        Protocol protocol = new Protocol();

        try(SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(server.getUnicastPort())){
            System.out.println("[*] Server started at " + serverSocket.getInetAddress().getHostAddress() + ":" + server.getUnicastPort());
            new Thread(new TaskExecutor(taskManager, this, protocol)).start();
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

    public List<String> getTags() {
        return server.getTags();
    }
}
