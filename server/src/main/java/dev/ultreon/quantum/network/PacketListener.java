package dev.ultreon.quantum.network;

public interface PacketListener {
    static PacketListener onFailure(Runnable func) {
        return new PacketListener() {
            @Override
            public void onFailure() {
                func.run();
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
            public void onFailure() {
                func.run();
            }
        };
    }

    default void onSuccess() {

    }

    default void onFailure() {

    }
}
