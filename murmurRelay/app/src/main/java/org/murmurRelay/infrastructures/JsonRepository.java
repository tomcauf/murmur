package org.murmurRelay.infrastructures;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.murmurRelay.domains.Relay;
import org.murmurRelay.infrastructures.dtos.RelayDto;
import org.murmurRelay.infrastructures.mapper.Mapping;
import org.murmurRelay.repositories.IRelayRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class JsonRepository implements IRelayRepository {
    private final Path configFilePath;


    public JsonRepository() {
        String configFolderPath;

        if(Paths.get("").toAbsolutePath().toString().contains("app")) {
            configFolderPath = Paths.get("","src","main","resources","config").toAbsolutePath().toString();
        } else {
            configFolderPath = Paths.get("","app","src","main","resources","config").toAbsolutePath().toString();
        }

        configFilePath = Paths.get(configFolderPath,"relay.json").toAbsolutePath();
    }

    @Override
    public Relay getRelay() {
        Mapping map = new Mapping();
        Gson gson = new Gson();

        if(Files.exists(configFilePath)) {
            try(BufferedReader bufferedReader = Files.newBufferedReader(configFilePath, StandardCharsets.UTF_8)) {
                RelayDto relayDto = gson.fromJson(bufferedReader,RelayDto.class);
                return map.getRelay(relayDto);
            }  catch(JsonSyntaxException e) {
                throw new RuntimeException("Error while reading JSON file. Please check the syntax of the file elements");
            } catch (JsonIOException | IOException e) {
                throw new RuntimeException("Error while reading JSON file");
            }
        } else {
            throw new RuntimeException("Error : config filepath does not exist");
        }
    }

    @Override
    public void writeRelay(Relay relay) {
        Mapping map = new Mapping();
        Gson gson = new Gson();
        RelayDto relayDto = map.getRelayDto(relay);

        try(BufferedWriter bufferedWriter = Files.newBufferedWriter(configFilePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(relayDto,bufferedWriter);
        } catch (JsonIOException | IOException e) {
            throw new RuntimeException("Error while writing in config filepath");
        }
    }
}
