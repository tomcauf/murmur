package org.murmurServer.repositories;

import org.murmurServer.domains.Server;

public interface IServerRepositories {
    Server getServer();
    void writeServer(Server server);
}
