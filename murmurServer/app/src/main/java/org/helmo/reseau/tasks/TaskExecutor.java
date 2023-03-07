package org.helmo.reseau.tasks;

import org.helmo.reseau.clients.ClientRunnable;
import org.helmo.reseau.domains.StatusOfTask;
import org.helmo.reseau.domains.Task;
import org.helmo.reseau.domains.User;
import org.helmo.reseau.grammar.Protocol;
import org.helmo.reseau.servers.ServerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskExecutor implements Runnable{
    private TaskManager taskManager;
    private ServerManager serverManager;
    private Protocol protocol;
    public TaskExecutor(TaskManager taskManager, ServerManager serverManager, Protocol protocol){
        this.taskManager = taskManager;
        this.serverManager = serverManager;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
               System.out.println("[!] Error TaskExecutor.run: " + e.getMessage());
            }
            Task currentTask = taskManager.getNextTask();
            if(currentTask != null){
                runTask(currentTask);
            }
        }
    }

    private void runTask(Task currentTask) {
        System.out.println("[*] TaskExecutor.runTask: " + currentTask.getType());
        currentTask.setStatus(StatusOfTask.IN_PROGRESS);
        switch (currentTask.getType()) {
            case "FOLLOW" -> follow(currentTask);
            case "MSG" -> msg(currentTask);
            case "DISCONNECT" -> disconnect(currentTask);
            default -> System.out.println("[!] TaskExecutor.runTask: Unknown task type");
        }

    }
    private void follow(Task currentTask) {
        String[] command = currentTask.getCommand();
        String follow = command[1];
        String name = follow.substring(0, follow.indexOf("@"));
        String domain = follow.substring(follow.indexOf("@")+1);
        ClientRunnable source = currentTask.getSource();
        if(follow.charAt(0) == '#') {
            if (domain.equals(serverManager.getServerDomain())) {
                if(!source.isFollowed(follow)){
                    serverManager.addTag(name);
                    source.addFollowedTag(follow);
                    serverManager.saveServer();
                    source.sendMessage(protocol.buildOk("You are now following " + follow));
                }/*else{
                    source.sendMessage(protocol.buildError("You are already following " + follow));
                }*/
            } else {
                //TODO: Envoyé au RELAY la demande de FOLLOW
            }
        }else{
            if(domain.equals(serverManager.getServerDomain())) {
                User userToFollow = serverManager.getUser(name);
                String usernameDomainOfSource = source.getUsername() + "@" + serverManager.getServerDomain();
                if (userToFollow != null && !userToFollow.getLogin().equals(source.getUsername()) && !userToFollow.getFollowers().contains(usernameDomainOfSource)) {
                    userToFollow.addFollower(usernameDomainOfSource);
                    serverManager.saveServer();
                    source.sendMessage(protocol.buildOk("You are now following " + follow));
                }/*else {
                    source.sendMessage(protocol.buildError("You are already following " + follow));
                }*/
            } else {
                //TODO: Envoyé au RELAY la demande de FOLLOW
            }
        }


    }
    private void msg(Task currentTask) {
        //Récupérer la liste de destinataires et envoyer le message
        String[] command = currentTask.getCommand();
        String message = command[1];
        List<String> destinataires = currentTask.getDestination();
        ClientRunnable source = currentTask.getSource();
        String serverDomain = serverManager.getServerDomain();
        //TODO: Doit changer pour envoyer au Relay les MSG en "mieux".
        for (String destinataire : destinataires) {
            String sender = source.getUsername() + "@" + serverDomain;
            String name = destinataire.substring(0, destinataire.indexOf("@"));
            String domain = destinataire.substring(destinataire.indexOf("@")+1);
            if(domain.equals(serverDomain)) {
                ClientRunnable clientRunnable = serverManager.getClient(name);
                if (clientRunnable != null) {
                    clientRunnable.sendMessage(protocol.buildMsgs(sender, message));
                }
            }
        }

    }

    private void disconnect(Task currentTask) {
        currentTask.getSource().sendMessage(protocol.buildOk("Disconnecting"));
        serverManager.closeClient(currentTask.getSource());
    }



}
