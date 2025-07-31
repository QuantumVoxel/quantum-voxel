package dev.ultreon.quantapi.networking.api;

import dev.ultreon.xeox.api.Environment;

public enum PacketDestination {
    SERVER, CLIENT;

    public PacketDestination opposite() {
        return switch (this) {
            case SERVER -> PacketDestination.CLIENT;
            case CLIENT -> PacketDestination.SERVER;
        };
    }

    public Environment getSourceEnv() {
        return switch (this) {
            case SERVER -> Environment.CLIENT;
            case CLIENT -> Environment.SERVER;
        };
    }

    public Environment getDestinationEnv() {
        return switch (this) {
            case SERVER -> Environment.SERVER;
            case CLIENT -> Environment.CLIENT;
        };
    }
}
