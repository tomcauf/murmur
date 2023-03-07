package org.helmo.reseau.domains;

import org.helmo.reseau.clients.ClientRunnable;

import java.util.List;

public class Task {
    private int id;
    private String type;
    private ClientRunnable source;
    private List<String> destination;
    private String[] command;
    private StatusOfTask status;
    public Task(int id, String type, ClientRunnable source, List<String> destination, String[] command, StatusOfTask status) {
        this.id = id;
        this.type = type;
        this.source = source;
        this.destination = destination;
        this.command = command;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public ClientRunnable getSource() {
        return source;
    }

    public List<String> getDestination() {
        return destination;
    }

    public String[] getCommand() {
        return command;
    }

    public StatusOfTask getStatus() {
        return status;
    }

    public void setStatus(StatusOfTask status) {
        this.status = status;
    }
}
