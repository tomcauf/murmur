package org.helmo.reseau;

import org.helmo.reseau.grammar.Protocol;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class Server2Runnable implements Runnable {
    public void run() {
        try {
            BufferedReader bufferedReader = null;
            PrintWriter out = null;
            ServerSocket serverRelay = new ServerSocket(12022,50, InetAddress.getByName("server2.godswila.guru"));
            while(true) {
                Socket client = serverRelay.accept();
                bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream(), Charset.forName("UTF-8")));
                out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(),Charset.forName("UTF-8")),true);
                out.print(new Protocol().buildSend("1@server1.godswila.guru","maxime123@server1.godswila.guru","maxime345@server2.godswila.guru","COUCOU JE SUIS LE MESSAGE du serveur 2")); // Ceci doit être remplacé par un vrai message SEND (lorsqu'un utilisateur du client Python est connecté sur un serveur (imaginons le server1) et envoie un message et que ce message est destiné à un ou plusieurs utilisateurs se trouvant sur un autre serveur (exemple server2), un message SEND doit être envoyé au relay)
                out.flush();

                String line = bufferedReader.readLine();
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
