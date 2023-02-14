package org.murmurServer;

import org.murmurServer.domains.Server;
import org.murmurServer.infrastructures.mapper.ServerConfigMapper;
import org.murmurServer.servers.ServerManager;

import java.nio.file.Paths;

public class App {
    public static void main(String[] args) {
        System.out.println("[*] Program started");
        String configFolder;
        if(Paths.get("").toAbsolutePath().toString().contains("app")) {
            configFolder = Paths.get("","src","main","resources","config").toAbsolutePath().toString();
        } else {
            configFolder = Paths.get("","app","src","main","resources","config").toAbsolutePath().toString();
        }
        ServerConfigMapper mapper = new ServerConfigMapper(configFolder, "server1.json");
        if(mapper.getServer() == null) {
            System.out.println("[!] Error main: mapper is null");
            return;
        }
        Server server = mapper.getServer();
        ServerManager serverRunning = new ServerManager(server);
        serverRunning.startServer();
    }
}
