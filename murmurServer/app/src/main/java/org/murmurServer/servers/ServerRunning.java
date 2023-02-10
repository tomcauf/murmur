package org.murmurServer.servers;

import org.murmurServer.clients.ClientRunnable;
import org.murmurServer.domains.Server;
import org.murmurServer.domains.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerRunning {
    private List<ClientRunnable> clientList;
    private Server server;
    private boolean stop = false;
    public ServerRunning(int port, Server server) {
        clientList = Collections.synchronizedList(new ArrayList<>());
        this.server = server;
        Socket client;
        if (server == null) {
            System.out.println("[!] Error with the server (null)");
            return;
        }
        try(ServerSocket serverSocket =  new ServerSocket(port)) {
            System.out.printf("[*] Server started on port %d and domain %s\n",port,serverSocket.getInetAddress().getHostAddress());

            while(!stop)
            {
                client = serverSocket.accept();
                System.out.println("[+] Client connecté");
                ClientRunnable runnable = new ClientRunnable(client,this);
                clientList.add(runnable);
                (new Thread(runnable)).start();
            }
        }
        catch(IOException ex)
        {
            System.out.println("[!] Error Server: " + ex.getMessage());
        }
    }

    public void broadcastToAllClientsExceptMe(String message, ClientRunnable client) {
        System.out.printf("[broadcastAllExcept] Message envoyé : %s\n",message);
        for(ClientRunnable c : clientList) {
            if(c != client) {
                c.sendMessage(message);
            }
        }
    }

    public void addUser(User user) {
        server.addUser(user);
    }

    public boolean doYouKnowThisUser(User user) {
        return server.doYouKnowThisUser(user);
    }

    public User getUserByName(String name) {
        return server.getUserByName(name);
    }

}
