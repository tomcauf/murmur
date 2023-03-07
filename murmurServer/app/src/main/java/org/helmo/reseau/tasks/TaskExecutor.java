package org.helmo.reseau.tasks;

import org.helmo.reseau.clients.ClientRunnable;
import org.helmo.reseau.domains.StatusOfTask;
import org.helmo.reseau.domains.Task;
import org.helmo.reseau.domains.User;
import org.helmo.reseau.grammar.Protocol;
import org.helmo.reseau.servers.ServerManager;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                    serverManager.addFollowedTag(source.getUsername() + "@" + serverManager.getServerDomain(), name);
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
        List<String> destinataires = getDestination(currentTask);
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

    private List<String> getDestination(Task currentTask) {
        Set<String> destinataires = new HashSet<>();
        ClientRunnable clientRunnable = currentTask.getSource();
        destinataires.addAll(clientRunnable.getFollowers());
        String username = clientRunnable.getUsername();
        for (String tag : retrieveTags(currentTask.getCommand()[1])) {
            List<String> followers = serverManager.getFollowers(tag);
            //TODO: Doit changer
            if (followers.contains(username + "@" + serverManager.getServerDomain())) {
                destinataires.addAll(followers);
            }
        }
        destinataires.remove(username + "@" + serverManager.getServerDomain());
        return new ArrayList<>(destinataires);
    }

    private List<String> retrieveTags(String message) {
        String[] words = message.split(" ");
        List<String> tags = new ArrayList<>();
        for (String word : words) {
            if (word.startsWith("#")) {
                tags.add(word);
            }
        }
        return tags;
    }

}
