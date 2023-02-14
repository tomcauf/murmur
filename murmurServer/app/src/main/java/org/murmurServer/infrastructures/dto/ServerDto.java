package org.murmurServer.infrastructures.dto;

import java.util.List;

public class ServerDto {
    private String currentDomain;
    private int saltSizeInBytes;
    private String multicastAddress;
    private int multicastPort;
    private int unicastPort;
    private int relayPort;
    private String networkInterface;
    private String base64AES;
    private boolean tls;
    private List<UserDto> users;
    private List<TagDto> tags;

    public ServerDto(String domain, int saltSizeInBytes, String multicastAddress, int multicastPort, int unicastPort, int relayPort, String networkInterface, String base64AES, boolean tls, List<UserDto> users, List<TagDto> tags) {
        this.currentDomain = domain;
        this.saltSizeInBytes = saltSizeInBytes;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.unicastPort = unicastPort;
        this.relayPort = relayPort;
        this.networkInterface = networkInterface;
        this.base64AES = base64AES;
        this.tls = tls;
        this.users = users;
        this.tags = tags;
    }

    public String getDomain() {
        return currentDomain;
    }

    public int getSaltSizeInBytes() {
        return saltSizeInBytes;
    }

    public String getMulticastAddress() {
        return multicastAddress;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public int getUnicastPort() {
        return unicastPort;
    }

    public int getRelayPort() {
        return relayPort;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public String getBase64AES() {
        return base64AES;
    }

    public boolean isTls() {
        return tls;
    }

    public List<UserDto> getUserList() {
        return users;
    }

    public List<TagDto> getTagsList() {
        return tags;
    }
}
