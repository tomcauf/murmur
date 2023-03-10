package org.helmo.reseau.servers;

import org.helmo.reseau.grammar.Protocol;
import org.helmo.reseau.tasks.TaskManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RelayRunnable implements Runnable{
    private  BufferedReader in = null;
    private PrintWriter out = null;
    private  TaskManager taskManager;
    private Protocol protocol;
    private Socket client;
    public RelayRunnable(TaskManager taskManager, Protocol protocol) {
        this.taskManager = taskManager;
        this.protocol = protocol;
        try {
            ServerSocket serverRelay = new ServerSocket(12021);
            Socket client = null;


            client = serverRelay.accept();
            in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);

        } catch (IOException e) {
            System.out.println("[!] Error RelayRunnable: " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void run() {

        try {
            // Les infos du ServerSocket ne sont pas Ã  encoder en dur, elles doivent venir du fichier de config



            while(true) {

                String msg = in.readLine();

                String[] message = protocol.verifyMessage(msg);

                if (message[0].equals("SEND") ) {
                    taskManager.createTask(null, message);
                }
                // Attention tu ne dois pas envoyer le mÃªme message SEND en boucle. Mais bien une fois chaque message devant passer par le relay et au fur et Ã  mesure
                // Je pense donc que la connexion unicast doit tourner en permanence sur ce port, mais tu dois trouver un moyen de rÃ©cupÃ©rer les messages SEND tout au long de l'exÃ©cution
                out.print(new Protocol().buildSend("1@server1.godswila.guru","maxime123@server1.godswila.guru","maxime345@server2.godswila.guru","COUCOU JE SUIS LE MESSAGE du serveur 1")); // Ceci doit Ãªtre remplacÃ© par un vrai message SEND (lorsqu'un utilisateur du client Python est connectÃ© sur un serveur (imaginons le server1) et envoie un message et que ce message est destinÃ© Ã  un ou plusieurs utilisateurs se trouvant sur un autre serveur (exemple server2), un message SEND doit Ãªtre envoyÃ© au relay)
                out.flush();
                out.close();
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
