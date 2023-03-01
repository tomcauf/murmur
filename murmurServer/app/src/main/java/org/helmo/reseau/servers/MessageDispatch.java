package org.helmo.reseau.servers;

import org.helmo.reseau.clients.ClientRunnable;
import org.helmo.reseau.clients.Entity;

import java.util.*;
import java.util.stream.Collectors;

public class MessageDispatch {
    public MessageDispatch(){

    }
    public void dispatchMessage(List<ClientRunnable> clients, ClientRunnable author, String message){
        Set<ClientRunnable> clientsToDispatch = new HashSet<>();
        clientsToDispatch.addAll(getFollowers(clients, author));
        clientsToDispatch.addAll(getTagged(clients,getTags(message)));
        for (ClientRunnable client : clientsToDispatch) {
            if (client.isConnected() && client != author){
                client.sendMessage(message, author.getUserName());
            }
        }
    }
    public void dispatchMessageToUser(Entity client, String message){
        client.sendMessage(message);
    }
    //Doit être dans la class de task
    private List<ClientRunnable> getFollowers(List<ClientRunnable> clients, ClientRunnable author){
        List<String> followerList = author.getFollowerList()
                .stream().map(follower -> follower.split("@")[0]).collect(Collectors.toList());
        List<ClientRunnable> followers = new ArrayList<>();
        for (ClientRunnable client : clients) {
            if (followerList.contains(client.getUserName())){
                followers.add(client);
            }
        }
        return followers;
    }
    //Doit être dans la class de task
    private List<ClientRunnable> getTagged(List<ClientRunnable> clients, List<String> tags){
        List<ClientRunnable> tagged = new ArrayList<>();
        for (ClientRunnable client : clients) {
            for (String tag : tags) {
                List<String> tagList = client.getTagList()
                        .stream().map(tagElement -> tagElement.split("#")[1]).collect(Collectors.toList());
                if (tagList.contains(tag)){
                    tagged.add(client);
                }
            }
        }
        return tagged;
    }
    //Doit être dans la class de task
    private List<String> getTags(String message){
        List<String> tags = new ArrayList<>();
        String[] messageParts = message.split(" ");
        for (String messagePart : messageParts) {
            if (messagePart.startsWith("#")) {
                tags.add(messagePart);
            }
        }
        return tags;
    }
}

