package dev.ultreon.quantapi.networking.api;

import net.fabricmc.api.EnvType;

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

    public EnvType getSourceEnv() {
        switch (this) {
            case SERVER:
                return EnvType.CLIENT;
            case CLIENT:
                return EnvType.SERVER;
            default:
                throw new IllegalArgumentException();
        }
    }

    public EnvType getDestinationEnv() {
        switch (this) {
            case SERVER:
                return EnvType.SERVER;
            case CLIENT:
                return EnvType.CLIENT;
            default:
                throw new IllegalArgumentException();
        }
    }
}
