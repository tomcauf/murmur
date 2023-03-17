package org.helmo.reseau.relay;

import org.helmo.reseau.domains.Server;
import org.helmo.reseau.grammar.Protocol;
import org.helmo.reseau.relay.multicast.MulticastRunnable;
import org.helmo.reseau.repositories.IServerRepositories;
import org.helmo.reseau.tasks.TaskManager;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RelayManager implements Runnable{
    private final Server server;
    private TaskManager taskManager;
    private IServerRepositories repo;
    private  NetworkInterface si;

    private RelayRunnable relayRunnable;

    private List<String> idMessages;

    public RelayManager(IServerRepositories repositories, TaskManager taskManager, NetworkInterface selectedInterface) {
        this.taskManager = taskManager;
        this.repo = repositories;
        this.server = repositories.getServer();
        this.si = selectedInterface;
        idMessages = Collections.synchronizedList(new ArrayList<>());
    }

    public boolean checkIfIdMessageExists(String idMessage) {
        return idMessages.contains(idMessage);
    }

    public void addIdMessage(String idMessage) {
        idMessages.add(idMessage);
    }

     public void sendMessageToRelay(String message){
        if (relayRunnable != null) {
            relayRunnable.sendMessage(message);
        }else{
            System.out.println("Relay not connected");
        }
     }

    @Override
    public void run() {
        Protocol protocol = new Protocol();
        MulticastRunnable multicastRunnable = new MulticastRunnable(server,si);
        try {
            ServerSocket serverSocket = new ServerSocket(server.getRelayPort());
            new Thread( multicastRunnable).start();
            while (true) {
                Socket relaySocket = serverSocket.accept();
                relayRunnable = new RelayRunnable(taskManager,protocol,relaySocket,this);
                System.out.println("[*] Relay connected");
                (new Thread(relayRunnable)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



