package dev.ultreon.quantum.client;

import java.util.Objects;

public final class ServerData {
    private final String name;
    private final String address;

    public ServerData(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String name() {
        return name;
    }

    public String address() {
        return address;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ServerData) obj;
        return Objects.equals(this.name, that.name) &&
               Objects.equals(this.address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }

    @Override
    public String toString() {
        return "ServerData[" +
               "name=" + name + ", " +
               "address=" + address + ']';
    }


}
