package org.helmo.reseau.servers;

import org.helmo.reseau.domains.Server;
import org.helmo.reseau.infrastructures.ServerRepositories;
import org.helmo.reseau.infrastructures.mapper.ServerMapper;
import org.helmo.reseau.repositories.IServerRepositories;
import org.helmo.reseau.tasks.TaskManager;

import java.nio.file.Paths;

public class ServerFactory {
    private ServerManager serverManager;
    public ServerFactory(String configFileName, String certificateFileName, String certificatePassword) {
        String resourceFolder;
        if(Paths.get("").toAbsolutePath().toString().contains("app")) {
            resourceFolder = Paths.get("","src","main","resources").toAbsolutePath().toString();
        } else {
            resourceFolder = Paths.get("","app","src","main","resources").toAbsolutePath().toString();
        }

        String configPath = Paths.get(resourceFolder, "config").toString();
        IServerRepositories repositories = new ServerRepositories(configPath, configFileName, new ServerMapper());

        Server server = repositories.getServer();
        if(server== null) {
            System.out.println("[!] Error: server is null");
            return;
        }

        String certificatePath = Paths.get(resourceFolder, "certificate").toString();
        TLSSocketFactory tlsSocketFactory = new TLSSocketFactory(certificatePath, certificateFileName, certificatePassword);

        TaskManager taskManager = new TaskManager();
        serverManager = new ServerManager(repositories, tlsSocketFactory, taskManager);
    }

    public void start() {
        if(serverManager != null) {
            serverManager.startServer();
        }else{
            System.out.println("[!] Error: serverManager is null");
        }
    }
}