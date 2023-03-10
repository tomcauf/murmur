package org.murmurRelay.domains;

import java.util.ArrayList;
import java.util.List;

public class Relay {
    private final String multicastAdress;
    private final int multicastPort;
    private final List<Server> serverList;

    public Relay(String multicastAdress,int multicastPort,List<Server> serverList) {
        this.multicastAdress = multicastAdress;
        this.multicastPort = multicastPort;
        this.serverList = new ArrayList<>(serverList);
    }

    public String getMulticastAddress() {
        return multicastAdress;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public List<Server> getServerList() {
        return serverList;
    }
}
