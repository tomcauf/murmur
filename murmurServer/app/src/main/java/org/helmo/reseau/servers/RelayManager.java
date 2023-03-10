package org.helmo.reseau.servers;

import org.helmo.reseau.domains.Server;
import org.helmo.reseau.grammar.Protocol;
import org.helmo.reseau.repositories.IServerRepositories;
import org.helmo.reseau.relay.RelayRunnable;
import org.helmo.reseau.tasks.TaskManager;

import java.net.NetworkInterface;

public class RelayManager {
    private final Server server;
    private TaskManager taskManager;
    private IServerRepositories repo;
    private  NetworkInterface si;


    public RelayManager(IServerRepositories repositories, TaskManager taskManager, NetworkInterface selectedInterface) {
        this.taskManager = taskManager;
        this.repo = repositories;
        this.server = repositories.getServer();
        this.si = selectedInterface;


    }

    public void start(){
        Protocol protocol = new Protocol();
        MulticastRunnable multicastRunnable = new MulticastRunnable(server,si);
            RelayRunnable relayRunnable = new RelayRunnable(taskManager,protocol);
             new Thread( multicastRunnable).start();
             new Thread( relayRunnable).start();



    }

    public void createTask(String[] message){

                taskManager.createTask(null, message);
            }
        }



