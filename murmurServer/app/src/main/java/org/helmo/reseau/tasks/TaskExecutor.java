package org.helmo.reseau.tasks;

import org.helmo.reseau.clients.Entity;
import org.helmo.reseau.domains.Task;
import org.helmo.reseau.domains.User;
import org.helmo.reseau.grammar.Protocol;
import org.helmo.reseau.repositories.IServerRepositories;
import org.helmo.reseau.servers.MessageDispatch;
import org.helmo.reseau.servers.ServerManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
//TODO: AMELIORATION :
// => Dans la currentTask, il faudrait juste des méthodes pour récupérer les info de l'utilisateur au lieu de récup l'entity à chaque fois, ...
public class TaskExecutor implements Runnable{
    private TaskManager taskManager;
    private IServerRepositories serverRepositories;
    private ServerManager server;
    private MessageDispatch messageDispatch;
    private Protocol protocol;
    public TaskExecutor(TaskManager taskManager, IServerRepositories serverRepositories, ServerManager serverManager,MessageDispatch messageDispatch, Protocol protocol) {
        this.taskManager = taskManager;
        this.serverRepositories = serverRepositories;
        this.server = serverManager;
        this.messageDispatch = messageDispatch;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        System.out.println("TaskExecutor is running");
        while(true){
            try {
                Thread.sleep(1000);
                //System.out.println("[*] TaskExecutor is sleeping");
            } catch (InterruptedException e) {
                System.out.println("[*] TaskExecutor has been interrupted");
            }
            //Si il y a des tâches à effectuer
            Task currentTask = taskManager.getNextTask();
            if(currentTask != null){
                runTask(currentTask);
            }
        }
    }
    /**
     * REGISTER :
     * * Destinataire : l'utilisateur (je renvoie un +OK ou -ERR)
     * * Source : l'utilisateur
     *
     * CONNECT :
     * * Destinataire : l'utilisateur (je renvoie un PARAM ou -ERR)
     * * Source : l'utilisateur
     *
     * CONFIRM :
     * * Destinataire : l'utilisateur (je renvoie un +OK ou -ERR)
     * * Source : l'utilisateur
     *
     * FOLLOW :
     * * Destinataire : l'utilisateur (je renvoie un +OK ou -ERR)
     * * Source : l'utilisateur
     *
     * MSG :
     * * Destinataire : les utilisateurs qui suivent l'auteur ou si l'auteur utilise un #,
     *                  les utilisateurs qui suivent le # recevront le message
     * * Source : l'utilisateur
     *
     * DISCONNECT :
     * * Destinataire : l'utilisateur (je renvoie un +OK ou -ERR)
     * * Source : l'utilisateur
     *
     * HELLO :
     * * Destinataire : l'utilisateur
     * * Source : le serveur
     *
     *
     * Je sais directement le destinataire :
     * * REGISTER
     * * CONNECT
     * * CONFIRM
     * * FOLLOW
     * * DISCONNECT
     * * HELLO
     * * -ERR (vu que je renvoie à l'auteur, une erreur)
     *
     * Je ne sais pas le destinataire :
     * * MSG
     */
    private void runTask(Task currentTask) {
        switch (currentTask.getType()) {
            case "-ERR":
                System.out.println("TaskExecutor : -ERR");
                messageDispatch.dispatchMessageToUser(currentTask.getSource(), protocol.buildError(currentTask.getCommand()[1]));
                try{
                    currentTask.getSource().close();
                }catch (IOException e){
                    System.out.println("TaskExecutor : -ERR : IOException");
                }
                break;
            case "+OK":
                System.out.println("TaskExecutor : +OK");
                messageDispatch.dispatchMessageToUser(currentTask.getSource(), protocol.buildOk(currentTask.getCommand()[1]));
                break;
            case "HELLO":
                System.out.println("TaskExecutor : HELLO");
                helloTask(currentTask);
                break;
            case "REGISTER":
                System.out.println("TaskExecutor : REGISTER");
                registerTask(currentTask);
                break;
            case "CONNECT":
                System.out.println("TaskExecutor : CONNECT");
                connectTask(currentTask);
                break;
            case "CONFIRM":
                System.out.println("TaskExecutor : CONFIRM");
                confirmTask(currentTask);
                break;
            case "FOLLOW":
                System.out.println("TaskExecutor : FOLLOW");
                followTask(currentTask);
                break;
            case "MSG":
                System.out.println("TaskExecutor : MSG");
                msgTask(currentTask);
                break;
            case "DISCONNECT":
                System.out.println("TaskExecutor : DISCONNECT");
                disconnectTask(currentTask);
                break;
            default:
                System.out.println("[*] TaskExecutor : Not task found");
                break;
        }
    }

    private void helloTask(Task currentTask) {
        StringBuilder message = new StringBuilder();
        for (String command : currentTask.getCommand()) {
            message.append(command).append(" ");
        }
        messageDispatch.dispatchMessageToUser(currentTask.getSource(), message.toString());
    }
    private void registerTask(Task currentTask) {
        String[] command = currentTask.getCommand();//0 : REGISTER | 1 : name | 2 : saltSize | 3 : bcryptHash
        String name = command[1];
        int saltSize = Integer.parseInt(command[2]);
        String bcryptHash = command[3];
        //TODO: Protocol = Récupérer les morceaux du decryptedHash ? (voir avec Maxime)
        String[] decryptedHash = bcryptHash.split("\\$");// 0 : "vide" | 1 : "2b" | 2 : round | 3 (salt + hash): 1*70(lettre_chiffre / symbole)
        String salt = decryptedHash[3].substring(0, saltSize);
        String hash = decryptedHash[3].substring(saltSize);
        User user = new User(name, hash, Integer.parseInt(decryptedHash[2]), salt, new ArrayList<>(), new ArrayList<>(), 0);
        String message;
        if (server.doYouKnowThisUser(user)) {
            message = protocol.buildError("User already exists");
            System.out.println("TaskExecutor : User already exists");
        } else {
            server.addUser(user);
            currentTask.getSource().setUser(user);
            server.saveServer();
            message = protocol.buildOk("User registered");
        }
        System.out.println("TaskExecutor : " + message);
        messageDispatch.dispatchMessageToUser(currentTask.getSource(), message);
    }
    private void connectTask(Task currentTask) {
        String[] command = currentTask.getCommand();
        String name = command[1];
        User user = server.getUserByName(name);
        String message;
        if (user == null) {
            message = protocol.buildError("User doesn't exist");
        } else {
            currentTask.getSource().setUser(user);
            message = protocol.buildParam(String.valueOf(user.getBcryptRound()), user.getBcryptSalt());
        }
        System.out.println("TaskExecutor : " + message);
        messageDispatch.dispatchMessageToUser(currentTask.getSource(), message);
    }
    private void confirmTask(Task currentTask) {
        /**
         * String sha3Hex = messageParts[1];
         *                 String sha3HexToCompare;
         *                 try {
         *                     MessageDigest digest = MessageDigest.getInstance("SHA3-256");
         *                     byte[] hash = digest.digest((randomString + "$2b$"+ user.getBcryptRound() +"$" + user.getBcryptSalt() + user.getBcryptHash()).getBytes(StandardCharsets.UTF_8));
         *                     sha3HexToCompare = bytesToHex(hash);
         *                     if (sha3Hex.equals(sha3HexToCompare)) {
         *                         sendMessage("+OK Welcome " + user.getLogin() + "\r\n");
         *                     } else {
         *                         sendMessage("-ERR Wrong password\r\n");
         *                     }
         *                 } catch (NoSuchAlgorithmException e) {
         *                     e.printStackTrace();
         *                 }
         */
        String[] command = currentTask.getCommand();
        String sha3Hex = command[1];
        String sha3HexToCompare;
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            User user = currentTask.getSource().getUser();
            byte[] hash = digest.digest((currentTask.getSource().getRandomString() + "$2b$"+ user.getBcryptRound() +"$" + user.getBcryptSalt() + user.getBcryptHash()).getBytes(StandardCharsets.UTF_8));
            sha3HexToCompare = bytesToHex(hash);
            if (sha3Hex.equals(sha3HexToCompare)) {
                messageDispatch.dispatchMessageToUser(currentTask.getSource(), protocol.buildOk("Welcome " + user.getLogin()));
            } else {
                messageDispatch.dispatchMessageToUser(currentTask.getSource(), protocol.buildError("Wrong password"));
            }
        }catch (NoSuchAlgorithmException e) {
            messageDispatch.dispatchMessageToUser(currentTask.getSource(), protocol.buildError("Error while trying to confirm"));
        }

    }
    private void followTask(Task currentTask) {
        String[] command = currentTask.getCommand();
        String follow = command[1];

        if(follow.charAt(0) == '#') {
            String tagName = follow.substring(0, follow.indexOf("@"));
            String userDomain = follow.substring(follow.indexOf("@")+1);
            if(!currentTask.getSource().isFollowingTag(follow) && userDomain.equals(server.getDomain())){
                System.out.println("ok");
                currentTask.getSource().addFollowTag(tagName);
                server.addTagIfNotExist(tagName);
                server.addUserToTag(tagName, currentTask.getSource().getName());
                server.saveServer();
                messageDispatch.dispatchMessageToUser(currentTask.getSource(), protocol.buildOk("You are now following " + tagName));
            }else{
                messageDispatch.dispatchMessageToUser(currentTask.getSource(), protocol.buildError("You are already following " + tagName));
            }
        } else {
            String userName = follow.substring(0, follow.indexOf("@"));
            String userDomain = follow.substring(follow.indexOf("@")+1);
            User userToFollow = server.getUserByName(userName);
            if(userToFollow != null && !userToFollow.getLogin().equals(currentTask.getSource().getName()) && !userToFollow.getFollowers().contains(currentTask.getSource().getName()+"@"+server.getDomain())){
                userToFollow.addFollower(currentTask.getSource().getName()+"@"+server.getDomain());
                server.saveServer();
                messageDispatch.dispatchMessageToUser(currentTask.getSource(), protocol.buildOk("You are now following " + follow));
            }else{
                messageDispatch.dispatchMessageToUser(currentTask.getSource(), protocol.buildError("You are already following " + follow));
            }
        }
    }

    private void msgTask(Task currentTask) {
    }

    private void disconnectTask(Task currentTask)  {
        try{
            currentTask.getSource().setConnectionStatus(false);
            messageDispatch.dispatchMessageToUser(currentTask.getSource(), protocol.buildOk("Bye"));
            currentTask.getSource().close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
