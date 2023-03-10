package org.murmurRelay.handler;

import org.murmurRelay.domains.Relay;
import org.murmurRelay.domains.Server;
import org.murmurRelay.grammar.Protocol;
import org.murmurRelay.repositories.IRelayRepository;
import org.murmurRelay.servers.ClientServerRunnable;
import org.murmurRelay.utils.NetChooser;

import java.net.*;
import java.util.*;

public class RelayManager {
    private final IRelayRepository repository;
    private final NetChooser netChooser;
    private final Relay relay;
    private final Protocol protocol;
    private final List<String> serversNotAvailable;
    private final Map<String,Server> unicastServers;

    public RelayManager(IRelayRepository repo, NetChooser netChooser,Protocol protocol) {
        this.repository = repo;
        this.netChooser = netChooser;
        this.protocol = protocol;
        this.serversNotAvailable = Collections.synchronizedList(new ArrayList<>());
        this.unicastServers = Collections.synchronizedMap(new HashMap<>());
        relay = repository.getRelay();
    }

    public void startRelay() {
        MulticastSocket multicastSocket = null;
        InetSocketAddress group = null;
        NetworkInterface selectedInterface = netChooser.getSelectedInterface();

        try {
            InetAddress mcastaddr = InetAddress.getByName(relay.getMulticastAddress());
            group = new InetSocketAddress(mcastaddr, relay.getMulticastPort());
            multicastSocket = new MulticastSocket(relay.getMulticastPort());

            multicastSocket.joinGroup(new InetSocketAddress(mcastaddr, 0), netChooser.getSelectedInterface());

            while (true) {
                byte[] buf = new byte[multicastSocket.getReceiveBufferSize()];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(recv);
                String messageReceived = new String(recv.getData(),recv.getOffset(),recv.getLength());
                System.out.println(messageReceived);
                handleMessage(messageReceived,recv.getAddress());
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

    public void handleMessage(String messageReceived,InetAddress ipAddress) {
        String[] checkMessage = protocol.verifyMessage(messageReceived);
        if(checkMessage[0].matches("ECHO")) {
            String serverAESKey;
            String port = checkMessage[1];
            String domain = checkMessage[2];
            if((serverAESKey = checkIfServerKeyExists(domain)) != null && checkIfServerIsAvailable(domain)) {
                addUnicastServer(domain,port,serverAESKey,ipAddress);
                setDomainNotAvailable(domain);
                (new Thread(new ClientServerRunnable(this,domain,Integer.parseInt(port),ipAddress,serverAESKey))).start();
            }
        }
    }

    public int getUnicastDestinationPort(String domain) {
        if(unicastServers.containsKey(domain)) {
            return unicastServers.get(domain).getPort();
        }
        return -1;
    }

    private void addUnicastServer(String domain,String port,String serverAESKey,InetAddress ipAddress) {
        if(!unicastServers.containsKey(domain)) {
            unicastServers.put(domain,new Server(domain,serverAESKey,ipAddress,Integer.parseInt(port)));
        }
    }

    private List<Server> getRelayServerList() {
        return relay.getServerList();
    }

    public Protocol getProtocol() {
        return protocol;
    }

    private String checkIfServerKeyExists(String domain) {
        for(var server : getRelayServerList()) {
            if(server.getDomain().equals(domain)) {
                return server.getBase64AES();
            }
        }
        return null;
    }

    public String getServerKey(String domain) {
        if(unicastServers.containsKey(domain)) {
            return unicastServers.get(domain).getBase64AES();
        }
        return null;
    }

    public InetAddress getIpAddress(String domain) {
        if(unicastServers.containsKey(domain)) {
            return unicastServers.get(domain).getIpAddress();
        }
        return null;
    }

    public boolean checkIfServerIsAvailable(String domain) {
        return !serversNotAvailable.contains(domain);
    }

    public void setDomainAvailable(String domain) {
        serversNotAvailable.remove(domain);
    }

    public void setDomainNotAvailable(String domain) {
        if(!serversNotAvailable.contains(domain)) {
            serversNotAvailable.add(domain);
        }
    }
}
