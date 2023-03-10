package org.helmo.reseau.clients;

import org.helmo.reseau.grammar.Protocol;
import org.helmo.reseau.utils.NetChooser;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class RelayRunnable implements Runnable {

    String domain;
    int multicastPort;
    String multicastAdress;
    int relayport;

    public RelayRunnable(String domain, int multicastPort, String multicastAdress, int relayport) {
        this.domain = domain;
        this.multicastPort = multicastPort;
        this.multicastAdress = multicastAdress;
        this.relayport = relayport;


    }

    @Override
    public void run() {
        NetChooser netChooser = new NetChooser();
        NetworkInterface selectedInterface = netChooser.getSelectedInterface();

        Protocol p = new Protocol();

        try {
            String msg = p.buildEcho("12021",domain);
            InetAddress mcastaddr = InetAddress.getByName(multicastAdress);
            InetSocketAddress group = new InetSocketAddress(mcastaddr, multicastPort);
            MulticastSocket s = new MulticastSocket(multicastPort);

            s.joinGroup(new InetSocketAddress(mcastaddr, 0), selectedInterface);
            byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket hi = new DatagramPacket(msgBytes, msgBytes.length, group);

            while(true) {
                s.send(hi);
                Thread.sleep(15000);

            }
        } catch(Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
