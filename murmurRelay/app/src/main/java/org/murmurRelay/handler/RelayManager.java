package org.murmurRelay.handler;

import org.murmurRelay.domains.Relay;
import org.murmurRelay.domains.Server;
import org.murmurRelay.grammar.Protocol;
import org.murmurRelay.repositories.IRelayRepository;
import org.murmurRelay.servers.ServerRunnable;
import org.murmurRelay.utils.NetChooser;

import java.net.*;
import java.util.*;

public class RelayManager {
    private final IRelayRepository repository;
    private final NetChooser netChooser;
    private final Relay relay;
    private final Protocol protocol;
    private final Map<String,ServerRunnable> serversInUse;

    public RelayManager(IRelayRepository repo, NetChooser netChooser,Protocol protocol) {
        this.repository = repo;
        this.netChooser = netChooser;
        this.protocol = protocol;
        this.serversInUse = Collections.synchronizedMap(new HashMap<>());
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
                    multicastSocket.close();
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
            if((serverAESKey = checkIfServerKeyExists(domain)) != null && !checkIfServerIsRunning(domain)) {
                var clientServerRunnable = new ServerRunnable(this,domain,Integer.parseInt(port),ipAddress,serverAESKey);
                addRunningServer(domain,clientServerRunnable);
                (new Thread(clientServerRunnable)).start();
            }
        }
    }

    public void sendMessage(String messageToSend,String domain) {
        var server = serversInUse.get(domain);
        if(server != null) {
            server.sendMessage(messageToSend);
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

    private boolean checkIfServerIsRunning(String domain) {
        return serversInUse.containsKey(domain);
    }

    public void removeRunningServer(String domain) {
        serversInUse.remove(domain);
    }

    private void addRunningServer(String domain, ServerRunnable client) {
        if(!serversInUse.containsKey(domain)) {
            serversInUse.put(domain,client);
        }
    }
}