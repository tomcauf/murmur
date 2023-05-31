package org.helmo.reseau.tasks;

import org.helmo.reseau.clients.ClientRunnable;
import org.helmo.reseau.domains.StatusOfTask;
import org.helmo.reseau.domains.Task;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskManager {
    private BlockingQueue<Task> tasks;
    private int idCounter;

    public TaskManager() {
        this.tasks = new LinkedBlockingQueue<>();
        this.idCounter = 0;
    }

    public void createTask(ClientRunnable clientRunnable, String[] message) {
        System.out.println("[+] Create task: " + message[0]);
        tasks.add(new Task(idCounter++, message[0], clientRunnable, message, StatusOfTask.WAITING));
    }

    public Task getNextTask() {
        try {
            return tasks.take();
        } catch (Exception e) {
            System.out.println("[!] Error while getting next task");
        }
        return null;
    }

    public int getId() {
        return idCounter;
    }
}
