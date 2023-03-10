package org.murmurRelay.servers;

import org.murmurRelay.handler.RelayManager;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerRunnable implements Runnable{
    RelayManager relayManager;
    private final String domain;
    private final int port;
    private final InetAddress ipAddress;
    private final String aesKey;
    private PrintWriter out;

    public ServerRunnable(RelayManager relayManager, String domain, int port, InetAddress ipAddress, String aesKey) {
        this.relayManager = relayManager;
        this.domain = domain;
        this.port = port;
        this.ipAddress = ipAddress;
        this.aesKey = aesKey;
    }
    @Override
    public void run() {
        try(Socket socket = new Socket(ipAddress,port);BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
            String messageReceived;
            this.out = out;
            while(true) {
                if((messageReceived = bufferedReader.readLine()) != null) {
                    handleMessage(messageReceived);
                }
            }
            } catch(ConnectException e) {
                System.out.printf("The server with the domain name: %s is not available on port %d\n", ipAddress, port);
            } catch (IOException e) {
                System.out.printf("Error while contacting the server : %s on port %d\n", ipAddress, port);
            } finally {
                relayManager.removeRunningServer(domain);
            }
    }

    private void handleMessage(String messageReceived) {
        String decryptedMessage = decryptMessage(messageReceived, aesKey);
        var protocol = relayManager.getProtocol();
        String[] checkMessage = protocol.verifyMessage(messageReceived);

        if(checkMessage[0].equals("SEND")) {
            relayManager.sendMessage(decryptedMessage,protocol.extractDomain(checkMessage[3]));
        }
    }

    private String encryptMessage(String messageToEncrypt, String encryptKey) {
        return messageToEncrypt;
    }

    private String decryptMessage(String internalMessage, String decryptKey) {
        return internalMessage;
    }


    public void sendMessage(String messageToSend) {
        if(out != null) {
            String encryptedMessage = encryptMessage(messageToSend, aesKey);
            out.println(encryptedMessage);
            out.flush();
            System.out.println("Messsage envoyé : " + encryptedMessage);
        }
    }
}
