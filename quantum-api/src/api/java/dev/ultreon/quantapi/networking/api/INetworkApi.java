package dev.ultreon.quantapi.networking.api;

import java.util.Optional;
import java.util.ServiceLoader;

public interface INetworkApi {
    static INetworkApi get() {
        if (NetworkApiHolder.api != null) return NetworkApiHolder.api;
        ServiceLoader<INetworkApi> serviceLoader = ServiceLoader.load(INetworkApi.class);
        Optional<INetworkApi> first = serviceLoader.findFirst();
        if (first.isEmpty())
            throw new IllegalStateException("No INetworkApi implementation found!");

        NetworkApiHolder.api = first.get();
        return NetworkApiHolder.api;
    }

    INetwork createNetwork(INetworkFactory network, String modId, String channelName);
}
