package dev.ultreon.quantapi;

import dev.ultreon.quantapi.networking.api.INetwork;
import dev.ultreon.quantapi.networking.api.INetworkFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public interface IQuantAPI {
    static IQuantAPI get() {
        if (QuantAPIHolder.api != null) return QuantAPIHolder.api;
        ServiceLoader<IQuantAPI> serviceLoader = ServiceLoader.load(IQuantAPI.class);
        List<IQuantAPI> implementations = new ArrayList<>();
        for (IQuantAPI implementation : serviceLoader) {
            implementations.add(implementation);
        }
        if (implementations == null)
            throw new IllegalStateException("No IQuantAPI implementation found!");

        if (implementations.size() > 1)
            throw new IllegalStateException("Multiple implementations of IQuantAPI found!");

        QuantAPIHolder.api = implementations.get(0);
        return QuantAPIHolder.api;
    }

    INetwork createNetwork(INetworkFactory network, String modId, String channelName);
}
