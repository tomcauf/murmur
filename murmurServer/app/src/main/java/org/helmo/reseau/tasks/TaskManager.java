package org.helmo.reseau.tasks;

import org.helmo.reseau.clients.ClientRunnable;
import org.helmo.reseau.domains.StatusOfTask;
import org.helmo.reseau.domains.Task;
import org.helmo.reseau.servers.ServerManager;

import java.util.*;

public class TaskManager {
    private Queue<Task> tasks;
    private ServerManager serverManager;
    private int idCounter;

    public TaskManager() {
        this.tasks = new LinkedList<>();
        this.idCounter = 0;
    }

    public void createTask(ClientRunnable clientRunnable, String[] message) {
        Set<String> destinataires = new HashSet<>();
        if (message[0].equals("MSG")) {
            destinataires.addAll(clientRunnable.getFollowers());
            String username = clientRunnable.getUsername();
            for (String tag : retrieveTags(message[1])) {
                List<String> followers = serverManager.getFollowers(tag);
                if (followers.contains(username)) {
                    followers.remove(username);
                    destinataires.addAll(followers);
                }
            }
        } else {
            destinataires.add(clientRunnable.getUsername());
        }
        Task task = new Task(idCounter++, message[0], clientRunnable, new ArrayList<>(destinataires), message, StatusOfTask.WAITING);
        tasks.add(task);
    }

    public Task getNextTask() {
        return tasks.poll();
    }

    private List<String> retrieveTags(String message) {
        String[] words = message.split(" ");
        List<String> tags = new ArrayList<>();
        for (String word : words) {
            if (word.startsWith("#")) {
                tags.add(word);
            }
        }
        return tags;
    }
}
