package org.helmo.reseau;

import org.helmo.reseau.grammar.Protocol;
import org.helmo.reseau.utils.NetChooser;

import java.net.*;
import java.nio.charset.StandardCharsets;

public class Multicast2Runnable implements Runnable {
    @Override
    public void run() {
        NetChooser netChooser = new NetChooser();
        NetworkInterface selectedInterface = netChooser.getSelectedInterface();

        // Les deux lignes ci-dessus vont lancer un programme en console au démarrage du serveur qui va te permettre de sélectionner une interface réseau dans la liste (obligatoire pour faire du multicast). Pour être sûr de sélectionner la bonne interface, il faut que tu fasses un ipconfig avant et tu regardes l'interface réseau utilisée lorsque tu es sur ton réseau (ton wifi ou autre)

        Protocol p = new Protocol();

        try {
            // Attention le port unicast 12021 doit être récupéré depuis le fichier de config et le domaine aussi (pas écrit en dur comme ici)
            // Idem pour le port multicast 23001 et l'adresse ip multicast 224.1.1.255
            String msg = p.buildEcho("12022","server2.godswila.guru");
            InetAddress mcastaddr = InetAddress.getByName("224.1.1.255");
            InetSocketAddress group = new InetSocketAddress(mcastaddr, 23001);
            MulticastSocket s = new MulticastSocket(23001);

            s.joinGroup(new InetSocketAddress(mcastaddr, 0), selectedInterface);
            byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket hi = new DatagramPacket(msgBytes, msgBytes.length, group);

            while(true) {
                s.send(hi);
                Thread.sleep(15000);
            }
            /*s.leaveGroup(group, selectedInterface);*/
        } catch(Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
