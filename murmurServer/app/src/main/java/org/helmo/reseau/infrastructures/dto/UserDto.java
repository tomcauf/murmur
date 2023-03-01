package org.helmo.reseau.infrastructures.dto;

import java.util.List;

public class UserDto {
    private String login;
    private String bcryptHash;
    private int bcryptRound;
    private String bcryptSalt;
    private List<String> followers;
    private List<String> userTags;
    private int lockoutCounter;

    public UserDto(String login, String bcryptHash, int bcryptRound, String bcryptSalt, List<String> followers, List<String> userTags, int lockoutCounter) {
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

    public String getBCryptHash() {
        return bcryptHash;
    }

    public int getBCryptRound() {
        return bcryptRound;
    }

    public String getBCryptSalt() {
        return bcryptSalt;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public List<String> getUserTags() {
        return userTags;
    }

    public int getLockoutCounter() {
        return lockoutCounter;
    }
}
