package org.murmurRelay.infrastructures.dtos;
import org.murmurRelay.domains.Server;

import java.util.ArrayList;
import java.util.List;

public class RelayDto {
    private final String multicastAdress;
    private final int multicastPort;
    private final List<ServerDto> configuredDomains;

    public RelayDto(String multicastAdress,int multicastPort,List<ServerDto> serverList) {
        this.multicastAdress = multicastAdress;
        this.multicastPort = multicastPort;
        this.configuredDomains = new ArrayList<ServerDto>(serverList);
    }

    public String getMulticastAdress() {
        return multicastAdress;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public List<ServerDto> getServerList() {
        return configuredDomains;
    }
}
