package org.helmo.reseau.relay.multicast;

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


        System.out.println("Cocou  c est multicast runnable");



        Protocol p = new Protocol();

        try {

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
