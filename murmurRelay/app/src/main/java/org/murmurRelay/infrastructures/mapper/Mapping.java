package org.murmurRelay.infrastructures.mapper;

import org.murmurRelay.domains.Relay;
import org.murmurRelay.domains.Server;
import org.murmurRelay.infrastructures.dtos.RelayDto;
import org.murmurRelay.infrastructures.dtos.ServerDto;

import java.util.ArrayList;
import java.util.List;

public class Mapping {
    public Relay getRelay(RelayDto relayDto) {

        if(relayDto == null) {
            throw new IllegalArgumentException("Error : relayDto is null");
        } else {
            List<ServerDto> serverListDto = relayDto.getServerList();
            List<Server> serverList = new ArrayList<>();

            for(var serverDto : serverListDto) {
                serverList.add(new Server(serverDto.getDomain(),serverDto.getBase64AES()));
            }

            return new Relay(relayDto.getMulticastAdress(),relayDto.getMulticastPort(),serverList);
        }
    }

    public RelayDto getRelayDto(Relay relay) {
        if(relay == null) {
            throw new IllegalArgumentException("Error : relay is null");
        } else {
            List<Server> serverList = relay.getServerList();
            List<ServerDto> serverListDto = new ArrayList<>();

            for(var server : serverList) {
                serverListDto.add(new ServerDto(server.getDomain(),server.getBase64AES()));
            }

            return new RelayDto(relay.getMulticastAdress(),relay.getMulticastPort(),serverListDto);
        }
    }
}
