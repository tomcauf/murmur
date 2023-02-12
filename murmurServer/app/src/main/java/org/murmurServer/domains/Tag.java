package org.murmurServer.domains;

import java.util.ArrayList;
import java.util.List;

public class Tag {
    String name;
    List<String> users;
    public Tag(String name) {
        this.name = name;
        this.users = new ArrayList<>();
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<String> getUsers() {
        return users;
    }
    public void setUsers(List<String> users) {
        this.users = users;
    }
    public void addUser(String user) {
        this.users.add(user);
    }
}
