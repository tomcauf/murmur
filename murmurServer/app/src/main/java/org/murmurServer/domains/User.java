package org.murmurServer.domains;

import java.util.List;

public class User {
    private String login;
    private String bcryptHash;
    private int bcryptRound;
    private String bcryptSalt;
    private List<User> followers;
    private List<Tags> userTags;
    private int lockoutCounter;

    public User(String login, String bcryptHash, int bcryptRound, String bcryptSalt, List<User> followers, List<Tags> userTags, int lockoutCounter) {
        this.login = login;
        this.bcryptHash = bcryptHash;
        this.bcryptRound = bcryptRound;
        this.bcryptSalt = bcryptSalt;
        this.followers = followers;
        this.userTags = userTags;
        this.lockoutCounter = lockoutCounter;
    }


    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getBcryptHash() {
        return bcryptHash;
    }

    public void setBcryptHash(String bcryptHash) {
        this.bcryptHash = bcryptHash;
    }

    public int getBcryptRound() {
        return bcryptRound;
    }

    public void setBcryptRound(int bcryptRound) {
        this.bcryptRound = bcryptRound;
    }

    public String getBcryptSalt() {
        return bcryptSalt;
    }

    public void setBcryptSalt(String bcryptSalt) {
        this.bcryptSalt = bcryptSalt;
    }

    public List<User> getFollowers() {
        return followers;
    }

    public void setFollowers(List<User> followers) {
        this.followers = followers;
    }

    public List<Tags> getUserTags() {
        return userTags;
    }

    public void setUserTags(List<Tags> userTags) {
        this.userTags = userTags;
    }

    public int getLockoutCounter() {
        return lockoutCounter;
    }

    public void setLockoutCounter(int lockoutCounter) {
        this.lockoutCounter = lockoutCounter;
    }

}
