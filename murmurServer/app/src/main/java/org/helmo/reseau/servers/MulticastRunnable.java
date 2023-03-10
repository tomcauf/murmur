package org.helmo.reseau.servers;

import org.helmo.reseau.domains.Server;
import org.helmo.reseau.grammar.Protocol;

import java.net.*;
import java.nio.charset.StandardCharsets;

public class MulticastRunnable implements Runnable{

    private Server server;
    private NetworkInterface si;

    public MulticastRunnable(Server server, NetworkInterface si) {
        this.server = server;
        this.si = si;
    }

    @Override
    public void run() {



        // Les deux lignes ci-dessus vont lancer un programme en console au dÃ©marrage du serveur qui va te permettre de sÃ©lectionner une interface rÃ©seau dans la liste (obligatoire pour faire du multicast). Pour Ãªtre sÃ»r de sÃ©lectionner la bonne interface, il faut que tu fasses un ipconfig avant et tu regardes l'interface rÃ©seau utilisÃ©e lorsque tu es sur ton rÃ©seau (ton wifi ou autre)

        Protocol p = new Protocol();

        try {
            // Attention le port unicast 12021 doit Ãªtre rÃ©cupÃ©rÃ© depuis le fichier de config et le domaine aussi (pas Ã©crit en dur comme ici)
            // Idem pour le port multicast 23001 et l'adresse ip multicast 224.1.1.255
            String msg = p.buildEcho(String.valueOf(server.getRelayPort()),server.getDomain());
            InetAddress mcastaddr = InetAddress.getByName(server.getMulticastAddress());
            InetSocketAddress group = new InetSocketAddress(mcastaddr, server.getMulticastPort());
            MulticastSocket s = new MulticastSocket(server.getMulticastPort());

            s.joinGroup(new InetSocketAddress(mcastaddr, 0), si);
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
