package dev.ultreon.quantum.client;

import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.ubo.DataIo;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class LocalData {
    public String username;
    public List<ServerInfo> servers = new ArrayList<>();

    private LocalData() {
    }

    public void save() {
        FileHandle data = QuantumClient.data("localdata.ubo");
        try (OutputStream write = data.write(false)) {

            MapType mapType = new MapType();
            mapType.putInt("version", 1);
            mapType.putString("username", this.username);

            ListType<MapType> serversData = new ListType<>();
            for (ServerInfo server : this.servers) {
                serversData.add(server.save());
            }
            mapType.put("Servers", serversData);

            DataIo.write(mapType, write);
        } catch (IOException e) {
            QuantumClient.LOGGER.error("Failed to save local data", e);
        }
    }

    public static LocalData load() {
        LocalData localData = new LocalData();
        FileHandle data = QuantumClient.data("localdata.ubo");
        if (!data.exists()) {
            QuantumClient.LOGGER.debug("No local data found, creating new one");
            return localData;
        }
        try {
            MapType mapType = DataIo.read(data.read());
            localData.username = mapType.getString("username");

            ListType<MapType> serversData = mapType.getList("Servers");
            for (MapType serverData : serversData) {
                ServerInfo server = ServerInfo.load(serverData);
                localData.servers.add(server);
            }
        } catch (IOException e) {
            QuantumClient.LOGGER.error("Failed to load local data", e);
        }
        return localData;
    }
}
