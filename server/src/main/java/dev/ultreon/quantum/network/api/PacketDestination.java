package dev.ultreon.quantum.network.api;

import dev.ultreon.quantum.util.Env;

public enum PacketDestination {
    SERVER, CLIENT;

    public PacketDestination opposite() {
        switch (this) {
            case SERVER:
                return PacketDestination.CLIENT;
            case CLIENT:
                return PacketDestination.SERVER;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Env getSourceEnv() {
        switch (this) {
            case SERVER:
                return Env.CLIENT;
            case CLIENT:
                return Env.SERVER;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Env getDestinationEnv() {
        switch (this) {
            case SERVER:
                return Env.SERVER;
            case CLIENT:
                return Env.CLIENT;
            default:
                throw new IllegalArgumentException();
        }
    }
}
