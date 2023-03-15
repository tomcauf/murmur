package org.helmo.reseau.servers;

import org.helmo.reseau.clients.ClientRunnable;
import org.helmo.reseau.domains.Server;
import org.helmo.reseau.grammar.Protocol;
import org.helmo.reseau.multicast.MulticastRunnable;
import org.helmo.reseau.repositories.IServerRepositories;
import org.helmo.reseau.relay.RelayRunnable;
import org.helmo.reseau.tasks.TaskManager;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
        idMessages = new ArrayList<>();
    }

    public boolean checkIfIdMessageExists(String idMessage) {
        return idMessages.contains(idMessage);
    }

    public void addIdMessage(String idMessage) {
        idMessages.add(idMessage);
    }



    public void createTask(String[] message){

                taskManager.createTask(null, message);
            }

     public void sendMessageToRelay(String message){
        relayRunnable.sendMessage(message);
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
                (new Thread(relayRunnable)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



