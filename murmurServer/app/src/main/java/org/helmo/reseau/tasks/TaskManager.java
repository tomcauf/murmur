package org.helmo.reseau.tasks;

import org.helmo.reseau.clients.ClientRunnable;
import org.helmo.reseau.domains.Server;
import org.helmo.reseau.domains.StatusOfTask;
import org.helmo.reseau.domains.Task;
import org.helmo.reseau.servers.ServerManager;

import java.util.*;

public class TaskManager {
    private Queue<Task> tasks;
    private int idCounter;

    public TaskManager() {
        this.tasks = new LinkedList<>();
        this.idCounter = 0;
    }

    public void createTask(ClientRunnable clientRunnable, String[] message) {
        tasks.add(new Task(idCounter++, message[0], clientRunnable, message, StatusOfTask.WAITING));
    }

    public Task getNextTask() {
        return tasks.poll();
    }
}
