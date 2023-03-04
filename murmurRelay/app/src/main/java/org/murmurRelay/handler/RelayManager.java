package org.murmurRelay.handler;

import org.murmurRelay.domains.Relay;
import org.murmurRelay.domains.Server;
import org.murmurRelay.grammar.Protocol;
import org.murmurRelay.repositories.IRelayRepository;
import org.murmurRelay.servers.ClientServerRunnable;
import org.murmurRelay.utils.NetChooser;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelayManager {
    private final IRelayRepository repository;
    private final NetChooser netChooser;
    private final Relay relay;
    private final Protocol protocol;
    private final List<String> serversNotAvailable = new ArrayList<>();
    private final Map<String,String> unicastServers = new HashMap<>();

    public RelayManager(IRelayRepository repo, NetChooser netChooser,Protocol protocol) {
        this.repository = repo;
        this.netChooser = netChooser;
        this.protocol = protocol;
        relay = repository.getRelay();
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

            while (true) {
                byte[] buf = new byte[multicastSocket.getReceiveBufferSize()];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(recv);
                String messageReceived = new String(recv.getData(),recv.getOffset(),recv.getLength());
                System.out.println(messageReceived);
                handleMessage(messageReceived);
            }
        } catch(Exception e) {
            System.out.println("Error while executing relay");
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
        String[] checkMessage = protocol.verifyMessage(messageReceived);
        if(checkMessage[0].matches("ECHO")) {
            String serverAESKey;
            String port = checkMessage[1];
            String domain = checkMessage[2];
            if((serverAESKey = getServerKey(domain)) != null && checkIfServerIsAvailable(domain)) {
                addUnicastServer(domain,port);
                serversNotAvailable.add(domain);
                (new Thread(new ClientServerRunnable(this, Integer.parseInt(port),domain,serverAESKey))).start();
            }
        }
    }

    public int getUnicastDestinationPort(String domain) {
        if(unicastServers.containsKey(domain)) {
            return Integer.parseInt(unicastServers.get(domain));
        }
        return -1;
    }

    private void addUnicastServer(String domain,String port) {
        if(!unicastServers.containsKey(domain)) {
            unicastServers.put(domain,port);
        }
    }

    private List<Server> getRelayServerList() {
        return relay.getServerList();
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public String getServerKey(String domain) {
        for(var server : getRelayServerList()) {
            if(server.getDomain().equals(domain)) {
                return server.getBase64AES();
            }
        }
        return null;
    }

    private boolean checkIfServerIsKnown(String domain) {
        for(var server : getRelayServerList()) {
            if(server.getDomain().equals(domain)) {
                return true;
            }
        }
        return false;
    }


    private boolean checkIfServerIsAvailable(String domain) {
        return !serversNotAvailable.contains(domain);
    }

    public void setDomainAvailable(String domain) {
            serversNotAvailable.remove(domain);
    }
}
