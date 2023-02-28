package org.murmurServer.tasks;

import org.murmurServer.clients.Entity;
import org.murmurServer.domains.StatusOfTask;
import org.murmurServer.domains.Task;
import org.murmurServer.grammar.Protocol;

import java.util.*;

public class TaskManager{
    private int idCounter;
    private Protocol protocol;
    Queue<Task> taskList;
    public TaskManager(Protocol protocol){
        this.idCounter = 0;
        this.protocol = protocol;
        this.taskList = new LinkedList<>();

    }
    /**
     * REGISTER :
     * * Destinataire : l'utilisateur (je renvoie un +OK ou -ERR)
     * * Source : l'utilisateur
     *
     * CONNECT :
     * * Destinataire : l'utilisateur (je renvoie un PARAM ou -ERR)
     * * Source : l'utilisateur
     *
     * CONFIRM :
     * * Destinataire : l'utilisateur (je renvoie un +OK ou -ERR)
     * * Source : l'utilisateur
     *
     * FOLLOW :
     * * Destinataire : l'utilisateur (je renvoie un +OK ou -ERR)
     * * Source : l'utilisateur
     *
     * MSG :
     * * Destinataire : les utilisateurs qui suivent l'auteur ou si l'auteur utilise un #,
     *                  les utilisateurs qui suivent le # recevront le message
     * * Source : l'utilisateur
     *
     * DISCONNECT :
     * * Destinataire : l'utilisateur (je renvoie un +OK ou -ERR)
     * * Source : l'utilisateur
     *
     * HELLO :
     * * Destinataire : l'utilisateur
     * * Source : le serveur
     *
     *
     * Je sais directement le destinataire :
     * * REGISTER
     * * CONNECT
     * * CONFIRM
     * * FOLLOW
     * * DISCONNECT
     * * HELLO
     * * -ERR (vu que je renvoie à l'auteur, une erreur)
     *
     * Je ne sais pas le destinataire :
     * * MSG
     */
    public void createTask(String request, Entity author) {
        String[] value = protocol.verifyMessage(request);
        int id = idCounter++;
        String type = value[0];
        //System.out.println("TaskManager : createTask: " + type);
        List<String> destinataire = new ArrayList<>();
        if (value[0].equals("MSG")) {
            destinataire.addAll(author.getFollowers());
        } else {
            destinataire.add(author.getName());
        }
        Task task = new Task(id, type, author, destinataire, value, StatusOfTask.WAITING);
        taskList.add(task);
    }

    public Task getNextTask() {
        return taskList.poll();
    }
}
