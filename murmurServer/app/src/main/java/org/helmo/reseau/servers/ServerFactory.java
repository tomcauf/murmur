package org.helmo.reseau.servers;

import org.helmo.reseau.domains.Server;
import org.helmo.reseau.infrastructures.mapper.ServerConfigMapper;
import org.helmo.reseau.repositories.IServerRepositories;

import java.nio.file.Paths;

public class ServerFactory {

    private final String configFileName;
    private final String certificateFileName;
    private final String certificatePassword;

    public ServerFactory(String configFileName, String certificateFileName, String certificatePassword) {
        this.configFileName = configFileName;
        this.certificateFileName = certificateFileName;
        this.certificatePassword = certificatePassword;
    }

    public void createServerAndStartIt() {
        String resourceFolder;
        if(Paths.get("").toAbsolutePath().toString().contains("app")) {
            resourceFolder = Paths.get("","src","main","resources").toAbsolutePath().toString();
        } else {
            resourceFolder = Paths.get("","app","src","main","resources").toAbsolutePath().toString();
        }
        String configPath = Paths.get(resourceFolder, "config").toString();
        String certificatePath = Paths.get(resourceFolder, "certificate").toString();
        IServerRepositories mapper = new ServerConfigMapper(configPath, configFileName);
        Server server = mapper.getServer();
        if(server== null) {
            System.out.println("[!] Error main: server is null");
            return;
        }
        TLSSocketFactory tlsSocketFactory = new TLSSocketFactory(certificatePath, certificateFileName, certificatePassword);
        ServerManager serverRunning = new ServerManager(mapper, tlsSocketFactory);
        serverRunning.startServer();
    }
}