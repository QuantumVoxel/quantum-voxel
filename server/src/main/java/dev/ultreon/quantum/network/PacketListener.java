package dev.ultreon.quantum.network;

import dev.ultreon.quantum.network.packets.Packet;

import java.util.function.Supplier;

public interface PacketListener {
    static PacketListener onFailure(Supplier<Packet<?>> supplier) {
        return new PacketListener() {
            @Override
            public Packet<?> onFailure() {
                return supplier.get();
            }
        };
    }

    static PacketListener onSuccess(Runnable func) {
        return new PacketListener() {
            @Override
            public void onSuccess() {
                func.run();
            }
        };
    }

    static PacketListener onEither(Runnable func) {
        return new PacketListener() {
            @Override
            public void onSuccess() {
                func.run();
            }

            @Override
            public Packet<?> onFailure() {
                func.run();
                return PacketListener.super.onFailure();
            }
        };
    }

    default void onSuccess() {

    }

    default Packet<?> onFailure() {
        return null;
    }
}
