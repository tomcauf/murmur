package org.murmurServer.clients;

import org.murmurServer.domains.User;

import java.util.List;

public interface Entity {
    void sendMessage(String message);

    List<String> getFollowers();

    String getName();

    void setUser(User user);

    String getRandomString();

    User getUser();

    void setConnectionStatus(boolean b);

    boolean isFollowingTag(String tag);

    void addFollowTag(String tagName);
}
