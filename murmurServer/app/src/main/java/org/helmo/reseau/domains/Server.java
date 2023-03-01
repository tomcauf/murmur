package org.helmo.reseau.domains;

import java.util.ArrayList;
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
    private List<Tag> tagsList;

    public Server(String domain, int saltSizeInBytes, String multicastAddress, int multicastPort, int unicastPort, int relayPort, String networkInterface, String base64AES, boolean tls, List<User> userList, List<Tag> tagsList) {
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

    public void addUser(User user) {
        this.userList.add(user);
    }

    public User getUserByName(String name) {
        return userList.stream().filter(u -> u.getLogin().equals(name)).findFirst().orElse(null);
    }
    public boolean doYouKnowThisUser(User user) {
        return userList.stream().anyMatch(u -> u.getLogin().equals(user.getLogin()));
    }

    public void addTagIfNotExist(String tagName) {
        if (tagsList.stream().noneMatch(t -> t.getName().equals(tagName))) {
            tagsList.add(new Tag(tagName, new ArrayList<>()));
        }
    }

    public void addUserToTag(String tagName, String user) {
        Tag tag = tagsList.stream().filter(t -> t.getName().equals(tagName)).findFirst().orElse(null);
        if (tag != null) {
            tag.addUser(user);
        }
    }

    public int getUnicastPort() {
        return unicastPort;
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

    public List<User> getUserList() {
        return userList;
    }

    public List<Tag> getTagsList() {
        return tagsList;
    }
}
