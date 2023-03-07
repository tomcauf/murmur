package org.helmo.reseau.domains;

import java.util.List;

public class User {
    private String login;
    private String bcryptHash;
    private int bcryptRound;
    private String bcryptSalt;
    private List<String> followers;
    private List<String> userTags;
    private int lockoutCounter;

    public User(String login, String bcryptHash, int bcryptRound, String bcryptSalt, List<String> followers, List<String> userTags, int lockoutCounter) {
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

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }
    public void addFollower(String user) {
        this.followers.add(user);
    }

    public List<String> getUserTags() {
        return userTags;
    }

    public void setUserTags(List<String> userTags) {
        this.userTags = userTags;
    }
    public void addTag(String tag) {
        this.userTags.add(tag);
    }

    public int getLockoutCounter() {
        return lockoutCounter;
    }

    public void setLockoutCounter(int lockoutCounter) {
        this.lockoutCounter = lockoutCounter;
    }

    public void addFollowedTag(String tagName) {
        this.userTags.add(tagName);
    }

    public boolean haveTag(String tagName) {
        return this.userTags.contains(tagName);
    }

    public boolean isFollowed(String follow) {
        return this.followers.contains(follow);
    }
}
