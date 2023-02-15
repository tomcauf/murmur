package org.murmurServer.clients;

import org.murmurServer.domains.User;
import org.murmurServer.servers.ServerManager;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ClientRunnable implements Runnable {
    private User user;
    private SSLSocket client;
    private ServerManager server;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected = false;
    private String randomString;

    public ClientRunnable(SSLSocket client, ServerManager server) {
        this.client = client;
        this.server = server;
        randomString = generateRandomString(22);
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
            isConnected = true;
        } catch (IOException e) {
            System.out.println("[!] Error ClientRunnable: " + e.getMessage());
        }
        sendMessage("HELLO localhost "+ randomString +"\r\n");
    }

    @Override
    public void run() {
        try {
            String line = in.readLine();
            while (isConnected && line != null) {
                System.out.printf("[ClientRunnable] Ligne reçue : %s\n", line);
                handleMessage(line);
                line = in.readLine();
            }
        } catch (Exception e) {
            System.out.println("[!] Error ClientRunnable.run: " + e.getMessage());
        }
    }
    public void sendMessage(String message) {
        out.println(message);
    }
    public void sendMessage(String message, String from) {
        sendMessage(String.format("%s %s%s%s %s \r\n", "MSGS", from,"@",server.getDomain(), message));
    }
    private void handleMessage(String message)  {
        String[] messageParts = message.split(" ");
        switch (messageParts[0]) {
            case "REGISTER" -> {
                String name = messageParts[1];
                int saltSize = Integer.parseInt(messageParts[2]);
                String bcryptHash = messageParts[3];
                String[] decryptedHash = bcryptHash.split("\\$");// 0 : "vide" | 1 : "2b" | 2 : round | 3 (salt + hash): 1*70(lettre_chiffre / symbole)
                String salt = decryptedHash[3].substring(0, saltSize);
                String hash = decryptedHash[3].substring(saltSize);
                User user = new User(name, hash, Integer.parseInt(decryptedHash[2]), salt, new ArrayList<>(), new ArrayList<>(), 0);
                if (server.doYouKnowThisUser(user)) {
                    sendMessage("-ERR User already exists\r\n");
                } else {
                    server.addUser(user);
                    server.saveServer();
                    this.user = user;
                    sendMessage("+OK Welcome " + name + "\r\n");
                }
            }
            case "CONNECT" -> {
                String name = messageParts[1];
                User user = server.getUserByName(name);
                if (user == null) {
                    sendMessage("-ERR User not found\r\n");
                } else {
                    this.user = user;
                    sendMessage("PARAM " + user.getBcryptRound() + " " + user.getBcryptSalt() + "\r\n");
                }
            }
            case "CONFIRM" ->   {
                String sha3Hex = messageParts[1];
                String sha3HexToCompare;
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA3-256");
                    byte[] hash = digest.digest((randomString + "$2b$"+ user.getBcryptRound() +"$" + user.getBcryptSalt() + user.getBcryptHash()).getBytes(StandardCharsets.UTF_8));
                    sha3HexToCompare = bytesToHex(hash);
                    if (sha3Hex.equals(sha3HexToCompare)) {
                        sendMessage("+OK Welcome " + user.getLogin() + "\r\n");
                    } else {
                        sendMessage("-ERR Wrong password\r\n");
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            case "FOLLOW" -> {//FOLLOW (nom@domaine / #tag@domaine) crlf
                String name = messageParts[1];
                if(name.startsWith("#")){
                    String tagName = name.substring(0, name.indexOf("@"));
                    String userDomain = name.substring(name.indexOf("@")+1);
                    String userName = user.getLogin();
                    if(!user.getUserTags().contains(tagName)){
                        user.addFollowedTag(name);
                        server.addTagIfNotExist(tagName);
                        server.addUserToTag(tagName, userName+"@"+userDomain);
                        server.saveServer();
                        sendMessage("+OK You are now following " + tagName + "\r\n");
                    }else {
                        sendMessage("-ERR Error while following " + tagName + "\r\n");
                    }

                }
                else{
                    String userName = name.substring(0, name.indexOf("@"));
                    String userDomain = name.substring(name.indexOf("@")+1);
                    if(userDomain.equals(server.getDomain())){
                        User userToFollow = server.getUserByName(userName);
                        if(userToFollow != null && !userToFollow.getLogin().equals(user.getLogin()) && !userToFollow.getFollowers().contains(user.getLogin()+"@"+server.getDomain())){
                            userToFollow.addFollower(user.getLogin()+"@"+server.getDomain());
                            server.saveServer();
                            sendMessage("+OK You are now following " + userName + "\r\n");
                        }else{
                            sendMessage("-ERR Error while following " + userName + "\r\n");
                        }
                    } else {
                        sendMessage("-ERR Error while following " + userName + " with domain " + userDomain + "\r\n");
                    }
                }

            }
            case "MSG" -> {
                String messageToSend = message.substring(message.indexOf(" ")+1);
                server.broadcastToAllClients(this, messageToSend);
            }
            case "DISCONNECT" -> {
                sendMessage("+OK Bye\r\n");
                isConnected = false;
            }
            default -> System.out.println("[learnAboutMessage] Unknown message");
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

    private String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#&@.?!/%*";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            result.append(characters.charAt(index));
        }
        return result.toString();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public List<String> getFollowerList() {
        return user.getFollowers();
    }

    public List<String> getTagList() {
        return user.getUserTags();
    }
    public String getUserName() {
        return user.getLogin();
    }
}
