package org.helmo.reseau.domains;

import org.helmo.reseau.clients.Entity;
import java.util.List;

public class Task {
    private int id;
    private String type;
    private Entity source;
    private List<String> destination;
    private String[] command;
    private StatusOfTask status;
    public Task(int id, String type, Entity source, List<String> destination, String[] command, StatusOfTask status) {
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

    public Entity getSource() {
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
