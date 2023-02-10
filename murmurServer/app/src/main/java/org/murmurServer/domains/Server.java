package org.murmurServer.domains;

import java.util.List;

public class Server {
    private String domain;
    private int saltSizeInBytes;
    private String multicastAddress;
    private int multicastPort;
    private int unicastPort;
    private int relayPort;
    private String networkInterface;
    private String base64AES;
    private boolean tls;
    private List<User> userList;
    private List<Tags> tagsList;

    public Server(String domain, int saltSizeInBytes, String multicastAddress, int multicastPort, int unicastPort, int relayPort, String networkInterface, String base64AES, boolean tls, List<User> userList, List<Tags> tagsList) {
        this.domain = domain;
        this.saltSizeInBytes = saltSizeInBytes;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.unicastPort = unicastPort;
        this.relayPort = relayPort;
        this.networkInterface = networkInterface;
        this.base64AES = base64AES;
        this.tls = tls;
        this.userList = userList;
        this.tagsList = tagsList;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getSaltSizeInBytes() {
        return saltSizeInBytes;
    }

    public void setSaltSizeInBytes(int saltSizeInBytes) {
        this.saltSizeInBytes = saltSizeInBytes;
    }

    public String getMulticastAddress() {
        return multicastAddress;
    }

    public void setMulticastAddress(String multicastAddress) {
        this.multicastAddress = multicastAddress;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public void setMulticastPort(int multicastPort) {
        this.multicastPort = multicastPort;
    }

    public int getUnicastPort() {
        return unicastPort;
    }

    public void setUnicastPort(int unicastPort) {
        this.unicastPort = unicastPort;
    }

    public int getRelayPort() {
        return relayPort;
    }

    public void setRelayPort(int relayPort) {
        this.relayPort = relayPort;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
    }

    public String getBase64AES() {
        return base64AES;
    }

    public void setBase64AES(String base64AES) {
        this.base64AES = base64AES;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }
    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }
    public void addUser(User user) {
        this.userList.add(user);
    }

    public List<Tags> getTagsList() {
        return tagsList;
    }

    public void setTagsList(List<Tags> tagsList) {
        this.tagsList = tagsList;
    }

    public User getUserByName(String name) {
        return userList.stream().filter(u -> u.getLogin().equals(name)).findFirst().orElse(null);
    }
    public boolean doYouKnowThisUser(User user) {
        return userList.stream().anyMatch(u -> u.getLogin().equals(user.getLogin()));
    }

}
