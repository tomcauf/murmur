package org.helmo.reseau.relay;

import org.helmo.reseau.grammar.Protocol;
import org.helmo.reseau.tasks.TaskManager;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RelayRunnable implements Runnable{
    private  BufferedReader in = null;
    private PrintWriter out = null;
    private  TaskManager taskManager;
    private Protocol protocol;
    private Socket client;

    private boolean isConnected;

    private RelayManager relayManager;

    public RelayRunnable(TaskManager taskManager, Protocol protocol, Socket relaySocket, RelayManager relayManager) {
        this.taskManager = taskManager;
        this.protocol = protocol;
        try {
            client = relaySocket;
            in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
            isConnected = true;
            this.relayManager = relayManager;

        } catch (IOException e) {
            System.out.println("[!] Error RelayRunnable: " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void run() {

        try {
            String msg;
            while(isConnected && (msg = in.readLine()) != null) {

                System.out.println("[*] R | Message: " + msg);
                //decrypt

                String[] message = protocol.verifyMessage(msg);
                if (message[0].equals("SEND") ) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : message) {
                        sb.append(s).append(" ");
                    }
                    System.out.println("[*] R | Message: " + sb.toString());
                    if(!relayManager.checkIfIdMessageExists(message[1])) {
                        System.out.println("[*] R | Message: " + sb.toString());
                        relayManager.addIdMessage(message[1]);
                        System.out.println("[*] R | Message: " + sb.toString());
                        taskManager.createTask(null, message);
                        System.out.println("[*] R | Message: " + sb.toString());
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                out.close();
                client.close();
                isConnected = false;
            } catch (IOException e) {
                throw new RuntimeException("Error while closing resources");
            }
        }

    }

    public void sendMessage(String message){
        //encrypt here.
        out.println(message);
        out.flush();
    }

}
