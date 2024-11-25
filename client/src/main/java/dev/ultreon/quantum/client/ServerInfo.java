package dev.ultreon.quantum.client;

import dev.ultreon.ubo.types.MapType;

public record ServerInfo(String name, String address) {
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
