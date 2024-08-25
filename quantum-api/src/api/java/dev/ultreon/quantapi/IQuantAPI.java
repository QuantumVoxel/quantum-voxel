package dev.ultreon.quantapi;

import dev.ultreon.quantapi.networking.api.INetwork;
import dev.ultreon.quantapi.networking.api.INetworkFactory;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public interface IQuantAPI {
    static IQuantAPI get() {
        if (QuantAPIHolder.api != null) return QuantAPIHolder.api;
        ServiceLoader<IQuantAPI> serviceLoader = ServiceLoader.load(IQuantAPI.class);
        List<ServiceLoader.Provider<IQuantAPI>> first = serviceLoader.stream().collect(Collectors.toList());

        if (first.isEmpty())
            throw new IllegalStateException("No IQuantAPI implementation found!");

        if (first.size() > 1)
            throw new IllegalStateException("Multiple implementations of IQuantAPI found!");

        QuantAPIHolder.api = first.getFirst().get();
        return QuantAPIHolder.api;
    }

    INetwork createNetwork(INetworkFactory network, String modId, String channelName);
}
