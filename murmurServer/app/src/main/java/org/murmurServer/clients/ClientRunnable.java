package org.murmurServer.clients;

import org.murmurServer.domains.User;
import org.murmurServer.servers.ServerRunning;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class ClientRunnable implements Runnable {
    private User user;
    private Socket client;
    private ServerRunning server;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected = false;
    private String randomString;

    public ClientRunnable(Socket client, ServerRunning server) {
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
        } catch (IOException e) {
            System.out.println("[!] Error ClientRunnable.run: " + e.getMessage());
        }
    }
    public void sendMessage(String message) {
        out.println(message);
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
                String sha3HexToCompare = "";
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
            case "FOLLOW" -> System.out.println("[learnAboutMessage] FOLLOW");
            case "MSG" -> {
                String messageToSend = messageParts[1];
                server.broadcastToAllClientsExceptMe(this, messageToSend);
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

}
