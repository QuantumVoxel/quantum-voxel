package dev.ultreon.quantum.client;

import dev.ultreon.ubo.types.MapType;

/**
 * Represents information about a server, including its name and address.
 * This record provides methods to load server information from a data structure
 * and save it back to a compatible format.
 */
public record ServerInfo(String name, String address) {
    /**
     * Constructs a ServerInfo record with the specified name and address.
     *
     * @param name the name of the server
     * @param address the address of the server
     */
    public ServerInfo {
    }

    public static ServerInfo load(MapType data) {
        return new ServerInfo(data.getString("name"), data.getString("address"));
    }

    public MapType save() {
        MapType data = new MapType();
        data.putString("name", this.name);
        data.putString("address", this.address);
        return data;
    }
}
