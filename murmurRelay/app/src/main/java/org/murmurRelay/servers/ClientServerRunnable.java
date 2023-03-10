package org.murmurRelay.servers;

import org.murmurRelay.grammar.Protocol;
import org.murmurRelay.handler.RelayManager;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientServerRunnable implements Runnable{
    RelayManager relayManager;
    private final String senderDomain;
    private final int senderPort;
    private final InetAddress senderIpAddress;
    private final String senderDecryptKey;

    public ClientServerRunnable(RelayManager relayManager,String senderDomain, int senderPort, InetAddress senderIpAddress, String senderDecryptKey) {
        this.relayManager = relayManager;
        this.senderDomain = senderDomain;
        this.senderPort = senderPort;
        this.senderIpAddress = senderIpAddress;
        this.senderDecryptKey = senderDecryptKey;
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
        String decryptedMessage = decryptInternalMessage(messageReceived,senderDecryptKey);
        var protocol = relayManager.getProtocol();
        String[] checkMessage = protocol.verifyMessage(messageReceived);
        String encryptKey;

        if(checkMessage[0].equals("SEND") && (encryptKey = relayManager.getServerKey(protocol.extractDomain(checkMessage[3]))) != null) {
            return encryptInternalMessage(decryptedMessage,encryptKey);
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
        String messageReceived;

        try {
            socket = new Socket(senderIpAddress, senderPort);
            Thread.sleep(3000);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if(bufferedReader.ready() && (messageReceived = bufferedReader.readLine()) != null) {
                return messageReceived;
            } else {
                return null;
            }
        } catch(ConnectException e) {
            System.out.printf("The server with the domain name: %s is not available on port %d\n", senderIpAddress, senderPort);
        } catch (IOException e) {
            System.out.printf("Error while contacting the server : %s on port %d\n", senderIpAddress, senderPort);
        } catch (InterruptedException e) {
            System.out.println("Error while interrupting thread");
        } finally {
            try {
                if(socket != null && bufferedReader != null) {
                    bufferedReader.close();
                    socket.close();
                }
                relayManager.setDomainAvailable(senderDomain);
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
        String targetDomain = protocol.extractDomain(splittedMessage[3]);
        int targetPort = relayManager.getUnicastDestinationPort(targetDomain);
        InetAddress targetIpAddress = relayManager.getIpAddress(targetDomain);

        try {
            if(targetPort != -1 && targetIpAddress != null) {
                while(true) {
                    if(relayManager.checkIfServerIsAvailable(targetDomain)) {
                        relayManager.setDomainNotAvailable(targetDomain);
                        socket = new Socket(targetIpAddress,targetPort);

                        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                        out.print(messageToSend);
                        out.flush();
                        System.out.println("Message envoyé : " + messageToSend);
                        break;
                    }
                }
            }
        } catch(ConnectException e) {
            System.out.printf("The server with the domain name: %s is not available on port %d\n",targetDomain,targetPort);
        } catch (IOException e) {
            System.out.printf("Error while contacting the server : %s on port %d\n",targetDomain,targetPort);
        } finally {
            try {
                if(socket != null && out != null) {
                    out.close();
                    socket.close();
                }
                relayManager.setDomainAvailable(targetDomain);
            } catch (IOException e) {
                System.out.println("Error while closing reader and socket");
            }
        }
    }
}
