package org.murmurServer.infrastructures.mapper;

import org.murmurServer.domains.Server;
import org.murmurServer.domains.Tag;
import org.murmurServer.domains.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ServerConfigMapper {
    private final String path;
    private final String fileName;

    public ServerConfigMapper(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;
    }

    public Server getServer() {
        try (BufferedReader reader = new BufferedReader(new FileReader(path + "/" + fileName))) {
            String line = reader.readLine();
            String currentDomain = "";
            int saltSizeInBytes = 0;
            String multicastAddress = "";
            int multicastPort = 0;
            int unicastPort = 0;
            int relayPort = 0;
            String networkInterface = "";
            String base64AES = "";
            boolean tls = false;
            List<User> userList = new ArrayList<>();
            List<Tag> tagsList = new ArrayList<>();
            while (line != null) {
                if (line.contains("currentDomain")) {
                    currentDomain = line.split(":")[1].replace("\"", "").replace(",", "").trim();
                } else if (line.contains("saltSizeInBytes")) {
                    saltSizeInBytes = Integer.parseInt(line.split(":")[1].replace("\"", "").replace(",", "").trim());
                } else if (line.contains("multicastAddress")) {
                    multicastAddress = line.split(":")[1].replace("\"", "").replace(",", "").trim();
                } else if (line.contains("multicastPort")) {
                    multicastPort = Integer.parseInt(line.split(":")[1].replace("\"", "").replace(",", "").trim());
                } else if (line.contains("unicastPort")) {
                    unicastPort = Integer.parseInt(line.split(":")[1].replace("\"", "").replace(",", "").trim());
                } else if (line.contains("relayPort")) {
                    relayPort = Integer.parseInt(line.split(":")[1].replace("\"", "").replace(",", "").trim());
                } else if (line.contains("networkInterface")) {
                    networkInterface = line.split(":")[1].replace("\"", "").replace(",", "").trim();
                } else if (line.contains("base64AES")) {
                    base64AES = line.split(":")[1].replace("\"", "").replace(",", "").trim();
                } else if (line.contains("tls")) {
                    tls = Boolean.parseBoolean(line.split(":")[1].replace("\"", "").replace(",", "").trim());
                } else if (line.contains("users")) {
                    String login = "";
                    String bcryptHash = "";
                    int bcryptRound = 0;
                    String bcryptSalt = "";
                    List<String> followers = new ArrayList<>();
                    List<String> userTags = new ArrayList<>();
                    int lockoutCounter = 0;
                    line = reader.readLine();
                    while (!line.contains("]")) {
                        if (line.contains("login")) {
                            login = line.split(":")[1].replace("\"", "").replace(",", "").trim();
                        } else if (line.contains("bcryptHash")) {
                            bcryptHash = line.split(":")[1].replace("\"", "").replace(",", "").trim();
                        } else if (line.contains("bcryptRound")) {
                            bcryptRound = Integer.parseInt(line.split(":")[1].replace("\"", "").replace(",", "").trim());
                        } else if (line.contains("bcryptSalt")) {
                            bcryptSalt = line.split(":")[1].replace("\"", "").replace(",", "").trim();
                        } else if (line.contains("followers")) {
                            line = reader.readLine();
                            while (!line.contains("]")) {
                                if (line.contains("login")) {
                                    //followers.add(new User(line.split(":")[1].replace("\"","").replace(",","").trim()));
                                }
                                line = reader.readLine();
                            }
                        } else if (line.contains("userTags")) {
                            line = reader.readLine();
                            while (!line.contains("]")) {
                                if (line.contains("name")) {
                                    //userTags.add(line.split(":")[1].replace("\"","").replace(",","").trim());
                                }
                                line = reader.readLine();
                            }
                        } else if (line.contains("lockoutCounter")) {
                            lockoutCounter = Integer.parseInt(line.split(":")[1].replace("\"", "").replace(",", "").trim());
                        }
                        line = reader.readLine();
                    }
                    userList.add(new User(login, bcryptHash, bcryptRound, bcryptSalt, followers, userTags, lockoutCounter));
                } else if (line.contains("tags")) {
                    while (!line.contains("]")) {
                        if (line.contains("name")) {
                            tagsList.add(new Tag(line.split(":")[1].replace("\"", "").replace(",", "").trim()));
                        }
                        line = reader.readLine();
                    }
                }
                line = reader.readLine();
            }
            return new Server(currentDomain, saltSizeInBytes, multicastAddress, multicastPort, unicastPort, relayPort, networkInterface, base64AES, tls, userList, tagsList);
        } catch (IOException e) {
            System.out.println("[!] Error ServerConfigMapper.getServer: " + e.getMessage());
        }
        return null;
    }

    public void writeServer(Server server) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + fileName))) {
        } catch (IOException e) {
            System.out.println("[!] Error ServerConfigMapper.writeServer: " + e.getMessage());
        }
    }

}
