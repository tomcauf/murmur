package org.murmurRelay.servers;

import org.murmurRelay.grammar.Protocol;
import org.murmurRelay.handler.RelayManager;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.charset.Charset;

public class ClientServerRunnable implements Runnable{
    RelayManager relayManager;
    private final int port;
    private final String domain;
    private final String decryptKey;

    public ClientServerRunnable(RelayManager relayManager, int port, String domain, String decryptKey) {
        this.relayManager = relayManager;
        this.port = port;
        this.domain = domain;
        this.decryptKey = decryptKey;
    }
    @Override
    public void run() {
        String messageReceived = getServerMessage();
        String messageToSend = decryptAndEncryptMessage(messageReceived);

        if(messageToSend != null) {
            sendServerMessage(messageToSend);
        }
    }

    private String decryptAndEncryptMessage(String messageReceived) {
        var protocol = relayManager.getProtocol();
        String[] checkMessage = protocol.verifyMessage(messageReceived);
        String encryptKey;

        if(checkMessage[0].equals("SEND") && (encryptKey = relayManager.getServerKey(protocol.extractDomain(checkMessage[3]))) != null) {
            // Déchiffrement à faire avec decryptKey
            String decryptedMessage = decryptInternalMessage(checkMessage[4],decryptKey);
            String encryptedMessage = encryptInternalMessage(decryptedMessage,encryptKey);

            return protocol.buildSend(checkMessage[1],checkMessage[2],checkMessage[3],encryptedMessage);
        }
        return null;
    }

    private String encryptInternalMessage(String messageToEncrypt, String encryptKey) {
        return messageToEncrypt;
    }

    private String decryptInternalMessage(String internalMessage,String decryptKey) {
        return internalMessage;
    }


    private String getServerMessage() {
        Socket socket = null;
        BufferedReader bufferedReader = null;

        try {
            socket = new Socket(domain, port);

            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            return bufferedReader.readLine();
        } catch(ConnectException e) {
            System.out.printf("The server with the domain name: %s is not available on port %d\n",domain,port);
        } catch (IOException e) {
            System.out.printf("Error while contacting the server : %s on port %d\n",domain,port);
        } finally {
            try {
                if(socket != null && bufferedReader != null) {
                    bufferedReader.close();
                    socket.close();
                }
                relayManager.setDomainAvailable(domain);
            } catch (IOException e) {
                System.out.println("Error while closing reader and socket");
            }
        }
        return null;
    }

    private void sendServerMessage(String messageToSend) {
        Socket socket = null;
        PrintWriter out = null;
        Protocol protocol = relayManager.getProtocol();
        String[] splittedMessage = relayManager.getProtocol().verifyMessage(messageToSend);
        String domain = protocol.extractDomain(splittedMessage[3]);
        int port = relayManager.getUnicastDestinationPort(domain);

        try {
            if(port != -1) {
                socket = new Socket(domain, relayManager.getUnicastDestinationPort(domain));

                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")), true);

                out.print(messageToSend);
                out.flush();
                System.out.println("Message envoyé : " + messageToSend);
            }
        } catch(ConnectException e) {
            System.out.printf("The server with the domain name: %s is not available on port %d\n",domain,port);
        } catch (IOException e) {
            System.out.printf("Error while contacting the server : %s on port %d\n",domain,port);
        } finally {
            try {
                if(socket != null && out != null) {
                    out.close();
                    socket.close();
                }
                relayManager.setDomainAvailable(domain);
            } catch (IOException e) {
                System.out.println("Error while closing reader and socket");
            }
        }
    }
}
