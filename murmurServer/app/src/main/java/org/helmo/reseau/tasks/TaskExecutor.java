package org.helmo.reseau.tasks;

import org.helmo.reseau.clients.ClientRunnable;
import org.helmo.reseau.domains.StatusOfTask;
import org.helmo.reseau.domains.Task;
import org.helmo.reseau.domains.User;
import org.helmo.reseau.grammar.Protocol;
import org.helmo.reseau.servers.ServerManager;


import java.util.*;

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
            case "SEND" -> send(currentTask);
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
                    if(serverManager.addFollowedTag(source.getUsername() + "@" + serverManager.getServerDomain(), name)) {
                        source.addFollowedTag(follow);
                        serverManager.saveServer();
                        source.sendMessage(protocol.buildOk("You are now following " + follow));
                    }
                }
            } else {
                String domainInfo = currentTask.getSource().getUsername() + "@" + serverManager.getServerDomain();
                String idMessage = String.format("%d@%s",taskManager.getId(), serverManager.getServerDomain());
                StringBuilder sb = new StringBuilder();
                for (String str : command)
                    sb.append(str).append(" ");

                System.out.println("[*] TaskExecutor.follow S: " + idMessage + " " + domainInfo + " " + follow + " " + sb.toString() + " ");
                serverManager.sendMessageToRelay(protocol.buildSend(idMessage,domainInfo,follow,sb.toString()));

            }
        }else{
            if(domain.equals(serverManager.getServerDomain())) {
                User userToFollow = serverManager.getUser(name);
                String usernameDomainOfSource = source.getUsername() + "@" + serverManager.getServerDomain();
                if (userToFollow != null && !userToFollow.getLogin().equals(source.getUsername()) && !userToFollow.getFollowers().contains(usernameDomainOfSource)) {
                    userToFollow.addFollower(usernameDomainOfSource);
                    serverManager.saveServer();
                    source.sendMessage(protocol.buildOk("You are now following " + follow));
                }
            } else {
                System.out.println("[*] TaskExecutor.follow: Sending to relay");
                String domainInfo = currentTask.getSource().getUsername() + "@" + serverManager.getServerDomain();
                String idMessage = String.format("%d@%s",taskManager.getId(), serverManager.getServerDomain());
                StringBuilder sb = new StringBuilder();
                for (String str : command)
                    sb.append(str).append(" ");
                System.out.println("[*] TaskExecutor.follow S: " + idMessage + " " + domainInfo + " " + follow + " " + sb.toString() + " ");
                serverManager.sendMessageToRelay(protocol.buildSend(idMessage, domainInfo, follow, sb.toString()));

            }
        }


    }
    private void send(Task currentTask) {
        try {
            String[] message = currentTask.getCommand();
            String sender = message[2];
            String destinataire = message[3];
            String messageToSend = message[4];
            System.out.println("MessageToSend: " + messageToSend);
            String[] messageProto = protocol.verifyMessage(messageToSend.trim());
            System.out.println("MessageProto: " + messageProto[0] + " " + messageProto[1]);
            if(Objects.equals(messageProto[0], "MSGS")){
                String destinataireName = destinataire.substring(0, destinataire.indexOf("@"));
                if(destinataireName.charAt(0) == '#') {
                    Set<String> destinaireList = new HashSet<>();
                    List<String> tags = retrieveTags(messageToSend);
                    for (String tag : tags) {
                        List<String> users = serverManager.getFollowers(tag);
                        if(users.contains(sender)){
                            users.remove(sender);
                            destinaireList.addAll(users);
                        }
                    }
                    for(String destinaire : destinaireList){
                        System.out.println("[X] Destinaire: " + destinaire);
                        String[] destinaireProto = destinaire.split("@");
                        if(destinaireProto[1].equals(serverManager.getServerDomain())){
                            ClientRunnable clientRunnable = serverManager.getClient(destinaireProto[0]);
                            if(clientRunnable != null){
                                clientRunnable.sendMessage(protocol.buildMsgs(messageProto[1], messageProto[2]));
                            }
                        }else{
                            String idMessage = String.format("%d@%s",taskManager.getId(), serverManager.getServerDomain());
                            serverManager.sendMessageToRelay(protocol.buildSend(idMessage, sender, destinaire, messageToSend));
                        }
                    }
                }else{
                    ClientRunnable clientRunnable = serverManager.getClient(destinataireName);
                    if(clientRunnable != null){
                        clientRunnable.sendMessage(protocol.buildMsgs(messageProto[1], messageProto[2]));
                    }
                }
            }else if(Objects.equals(messageProto[0], "FOLLOW")){
                String[] senderProto = messageProto[1].split("@");
                System.out.println("SenderProto: " + senderProto[0] + " " + senderProto[1]);
                if(senderProto[0].charAt(0) == '#') {
                    String tag = senderProto[0];
                    System.out.println("Tag: " + tag);
                    serverManager.addTag(tag);
                    if(serverManager.addFollowedTag(sender, tag)) {
                        serverManager.saveServer();
                        String ok = protocol.buildOk("You are now following " + messageProto[1]);
                        String idMessage = String.format("%d@%s", taskManager.getId(), serverManager.getServerDomain());
                        String messageToRelay = protocol.buildSend(idMessage, message[3], message[2], ok);
                        serverManager.sendMessageToRelay(messageToRelay);
                    }
                }else{
                    String destName = destinataire.substring(0, destinataire.indexOf("@"));
                    if(serverManager.addFollower(destName, sender)) {
                        serverManager.saveServer();
                        String ok = protocol.buildOk("You are now following " + destName);
                        String idMessage = String.format("%d@%s", taskManager.getId(), serverManager.getServerDomain());
                        String messageToRelay = protocol.buildSend(idMessage, message[3], message[2], ok);
                        serverManager.sendMessageToRelay(messageToRelay);
                    }
                }
            }else if(Objects.equals(messageProto[0], "+OK")){
                ClientRunnable clientRunnable = serverManager.getClient(destinataire.substring(0, destinataire.indexOf("@")));
                if(clientRunnable != null){
                    String[] messageSplit = messageToSend.split(" ");
                    for (String s : messageSplit) {
                        System.out.println("S: " + s);
                        if(s.charAt(0) == '#'){
                            clientRunnable.addFollowedTag(s);
                            serverManager.saveServer();
                            clientRunnable.sendMessage(messageToSend);
                        }
                    }
                }
            }
        }catch (Exception e){
            System.out.println("[!] TaskExecutor.send: " + e.getMessage());
        }
    }
    private void msg(Task currentTask) {
        String[] command = currentTask.getCommand();
        String message = command[1];
        List<String> destinataires = getDestination(currentTask);
        ClientRunnable source = currentTask.getSource();
        String serverDomain = serverManager.getServerDomain();
        String domainInfo = serverManager.getServerDomain();

        for (String destinataire : destinataires) {
            String sender = source.getUsername() + "@" + serverDomain;
            String name = destinataire.substring(0, destinataire.indexOf("@"));
            String domain = destinataire.substring(destinataire.indexOf("@")+1);
            if(domain.equals(serverDomain)) {
                ClientRunnable clientRunnable = serverManager.getClient(name);
                if (clientRunnable != null) {
                    clientRunnable.sendMessage(protocol.buildMsgs(sender, message));
                }
            }else{
                String idMessage = String.format("%d@%s",taskManager.getId(),domainInfo);
                String messageToSend = protocol.buildMsgs(sender, message);
                serverManager.sendMessageToRelay(protocol.buildSend(idMessage, sender, destinataire, messageToSend));

            }
        }

    }

    private List<String> getDestination(Task currentTask) {
        Set<String> destinataires = new HashSet<>();
        try {
            ClientRunnable clientRunnable = currentTask.getSource();
            destinataires.addAll(clientRunnable.getFollowers());
            String username = clientRunnable.getUsername();
            for (String tag : retrieveTags(currentTask.getCommand()[1])) {
            //Est-ce que le tag appartient à mon domaine ?
                if (serverManager.getTags().contains(tag)) {
                    System.out.println("Tag: " + tag);
                    destinataires.addAll(serverManager.getFollowers(tag));
                } else {
                    //Si non, je le relaye
                    System.out.println("Relay Tag: " + tag);
                    String messageToSend = protocol.buildMsgs(username + "@" + serverManager.getServerDomain(), currentTask.getCommand()[1]);
                    serverManager.sendMessageToRelay(protocol.buildSend(String.format("%d@%s", taskManager.getId(), serverManager.getServerDomain()), username + "@" + serverManager.getServerDomain(), tag + "@server2.godswila.guru", messageToSend));
                }
            }
            destinataires.remove(username + "@" + serverManager.getServerDomain());
        } catch (Exception e) {
            System.out.println("[!] TaskExecutor.getDestination: " + e.getMessage());
        }
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
