package org.helmo.reseau.clients;


import org.helmo.reseau.domains.User;

import java.io.IOException;
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

    void close() throws IOException;
}
