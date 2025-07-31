package dev.ultreon.quantum.api.events;

public class RegistryCreationEvent implements ModEvent {
    private final String modId;

    public RegistryCreationEvent(String modId) {
        this.modId = modId;
    }

    @Override
    public String getModId() {
        return modId;
    }
}
