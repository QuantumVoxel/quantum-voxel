package dev.ultreon.quantum.world.capability;

import java.util.function.Consumer;

public class CapabilityType<Instance extends Capability<Holder>, Holder> {
    private final CapabilityFactory<Instance, Holder> factory;

    public CapabilityType(CapabilityFactory<Instance, Holder> factory) {
        this.factory = factory;
    }

    public Instance create(Holder holder) {
        return this.factory.create();
    }

    public Instance with(Holder holder, Consumer<Instance> consumer) {
        Instance instance = this.create(holder);
        consumer.accept(instance);
        return instance;
    }

    @FunctionalInterface
    public interface CapabilityFactory<T extends Capability<Holder>, Holder> {
        T create();
    }
}
