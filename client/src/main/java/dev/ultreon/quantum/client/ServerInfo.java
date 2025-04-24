package dev.ultreon.quantum.client;

import dev.ultreon.quantum.ubo.types.MapType;

import java.util.Objects;

/**
 * Represents information about a server, including its name and address.
 * This record provides methods to load server information from a data structure
 * and save it back to a compatible format.
 */
public final class ServerInfo {
    private final String name;
    private final String address;
    private boolean secure;

    /**
     * Constructs a ServerInfo record with the specified name and address.
     *
     * @param name    the name of the server
     * @param address the address of the server
     * @param secure
     */
    public ServerInfo(String name, String address, boolean secure) {
        this.name = name;
        this.address = address;
        this.secure = secure;
    }

    public static ServerInfo load(MapType data) {
        return new ServerInfo(data.getString("name"), data.getString("address"), data.getBoolean("secure"));
    }

    public MapType save() {
        MapType data = new MapType();
        data.putString("name", this.name);
        data.putString("address", this.address);
        return data;
    }

    public String name() {
        return name;
    }

    public String address() {
        return address;
    }

    public boolean secure() {
        return secure;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ServerInfo) obj;
        return Objects.equals(this.name, that.name) &&
               Objects.equals(this.address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }

    @Override
    public String toString() {
        return "ServerInfo[" +
               "name=" + name + ", " +
               "address=" + address + ']';
    }

}
