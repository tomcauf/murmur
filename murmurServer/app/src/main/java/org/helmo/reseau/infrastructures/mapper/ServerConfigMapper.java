package org.helmo.reseau.infrastructures.mapper;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.helmo.reseau.domains.Server;
import org.helmo.reseau.domains.Tag;
import org.helmo.reseau.domains.User;
import org.helmo.reseau.infrastructures.dto.ServerDto;
import org.helmo.reseau.infrastructures.dto.TagDto;
import org.helmo.reseau.infrastructures.dto.UserDto;
import org.helmo.reseau.repositories.IServerRepositories;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ServerConfigMapper implements IServerRepositories {
    private final String path;
    private final String fileName;

    public ServerConfigMapper(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;
    }

    @Override
    public Server getServer() {
        Gson gson = new Gson();
        String configFilePath = Paths.get(path, fileName).toAbsolutePath().toString();
        try (BufferedReader reader = new BufferedReader(new FileReader(configFilePath))) {
            String file;
            StringBuilder builder = new StringBuilder();
            while ((file = reader.readLine()) != null) {
                builder.append(file);
            }
            file = builder.toString();
            ServerDto server = gson.fromJson(file, ServerDto.class);
            List<User> users = server.getUserList().stream().map(u -> new User(u.getLogin(), u.getBCryptHash(), u.getBCryptRound(), u.getBCryptSalt(), u.getFollowers(), u.getUserTags(), u.getLockoutCounter())).collect(Collectors.toList());
            List<Tag> tags = server.getTagsList().stream().map(t -> new Tag(t.getName(), t.getUsers())).collect(Collectors.toList());
            return new Server(server.getDomain(), server.getSaltSizeInBytes(), server.getMulticastAddress(), server.getMulticastPort(), server.getUnicastPort(), server.getRelayPort(), server.getNetworkInterface(), server.getBase64AES(), server.isTls(), users, tags);
        } catch (IOException e) {
            System.out.println("[!] Error ServerConfigMapper.getServer: " + e.getMessage());
            return null;
        } catch (JsonSyntaxException e) {
            System.out.println("[!] Error ServerConfigMapper.getServer JSON: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void writeServer(Server server) {
        Gson gson = new Gson();
       try(BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + fileName))){
           List<UserDto> users = server.getUserList().stream().map(u -> new UserDto(u.getLogin(), u.getBcryptHash(), u.getBcryptRound(), u.getBcryptSalt(), u.getFollowers(), u.getUserTags(), u.getLockoutCounter())).collect(Collectors.toList());
           List<TagDto> tags = server.getTagsList().stream().map(t -> new TagDto(t.getName(), t.getUsers())).collect(Collectors.toList());
           ServerDto serverDto = new ServerDto(server.getDomain(), server.getSaltSizeInBytes(), server.getMulticastAddress(), server.getMulticastPort(), server.getUnicastPort(), server.getRelayPort(), server.getNetworkInterface(), server.getBase64AES(), server.isTls(), users, tags);

              writer.write(gson.toJson(serverDto));
       }catch (IOException e) {
           System.out.println("[!] Error ServerConfigMapper.writeServer: " + e.getMessage());
       } catch (JsonSyntaxException e) {
           System.out.println("[!] Error ServerConfigMapper.writeServer JSON: " + e.getMessage());
       }
    }

}
