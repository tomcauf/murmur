package org.murmurRelay.handler;

import org.murmurRelay.domains.Relay;
import org.murmurRelay.grammar.Protocol;
import org.murmurRelay.repositories.IRelayRepository;
import org.murmurRelay.servers.ClientServerRunnable;
import org.murmurRelay.utils.NetChooser;

import java.net.*;

public class RelayManager {
    private final IRelayRepository repository;
    private final NetChooser netChooser;
    private final Relay relay;
    private final Protocol protocol;
    private boolean stop;

    public RelayManager(IRelayRepository repo, NetChooser netChooser,Protocol protocol) {
        this.repository = repo;
        this.netChooser = netChooser;
        this.protocol = protocol;
        relay = repository.getRelay();
        stop = false;
    }

    public void startRelay() {
        MulticastSocket multicastSocket = null;
        InetSocketAddress group = null;
        NetworkInterface selectedInterface = netChooser.getSelectedInterface();

        try {
            InetAddress mcastaddr = InetAddress.getByName(relay.getMulticastAdress());
            group = new InetSocketAddress(mcastaddr, relay.getMulticastPort());
            multicastSocket = new MulticastSocket(relay.getMulticastPort());

            multicastSocket.joinGroup(new InetSocketAddress(mcastaddr, 0), netChooser.getSelectedInterface());

            while (!stop) {
                byte[] buf = new byte[multicastSocket.getReceiveBufferSize()];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(recv);
                String messageReceived = new String(recv.getData(),recv.getOffset(),recv.getLength());
                handleMessage(messageReceived);
            }
        } catch(Exception e) {

        } finally {
            if(multicastSocket != null) {
                try {
                    multicastSocket.leaveGroup(group, selectedInterface);
                } catch(Exception e) {
                    System.out.println("Error while leaving multicast group");
                }
            }
        }
    }

    public void handleMessage(String messageReceived) {
        if(protocol.verifyMessage(messageReceived)[0].matches("ECHO")) {
            (new Thread(new ClientServerRunnable())).start();
        }
    }
}
