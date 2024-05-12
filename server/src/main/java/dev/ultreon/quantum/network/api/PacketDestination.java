package dev.ultreon.quantum.network.api;

import dev.ultreon.quantum.util.Env;

public enum PacketDestination {
    SERVER, CLIENT;

    public PacketDestination opposite() {
        return switch (this) {
            case SERVER -> PacketDestination.CLIENT;
            case CLIENT -> PacketDestination.SERVER;
        };
    }

    public Env getSourceEnv() {
        return switch (this) {
            case SERVER -> Env.CLIENT;
            case CLIENT -> Env.SERVER;
        };
    }

    public Env getDestinationEnv() {
        return switch (this) {
            case SERVER -> Env.SERVER;
            case CLIENT -> Env.CLIENT;
        };
    }
}
