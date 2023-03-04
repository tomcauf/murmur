package org.helmo.reseau;

import org.helmo.reseau.grammar.Protocol;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class RelayRunnable implements Runnable {
    @Override
    public void run() {
        try {
            // Les infos du ServerSocket ne sont pas à encoder en dur, elles doivent venir du fichier de config
            ServerSocket serverRelay = new ServerSocket(12021,50, InetAddress.getByName("server1.godswila.guru"));
            while(true) {
                Socket client = serverRelay.accept();
                PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(),Charset.forName("UTF-8")),true);
                // Attention tu ne dois pas envoyer le même message SEND en boucle. Mais bien une fois chaque message devant passer par le relay et au fur et à mesure
                // Je pense donc que la connexion unicast doit tourner en permanence sur ce port, mais tu dois trouver un moyen de récupérer les messages SEND tout au long de l'exécution
                out.print(new Protocol().buildSend("1@server1.godswila.guru","maxime123@server1.godswila.guru","maxime345@server2.godswila.guru","COUCOU JE SUIS LE MESSAGE du serveur 1")); // Ceci doit être remplacé par un vrai message SEND (lorsqu'un utilisateur du client Python est connecté sur un serveur (imaginons le server1) et envoie un message et que ce message est destiné à un ou plusieurs utilisateurs se trouvant sur un autre serveur (exemple server2), un message SEND doit être envoyé au relay)
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
