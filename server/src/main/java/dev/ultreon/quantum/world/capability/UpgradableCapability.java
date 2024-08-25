package dev.ultreon.quantum.world.capability;

import com.badlogic.gdx.utils.Array;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public abstract class UpgradableCapability<Value> extends ModifiableCapability<Value> {
    public UpgradableCapability(CapabilityType<? extends UpgradableCapability<Value> , Array<Value>> type, Class<? extends Value> clazz) {
        super(type, clazz);
    }

    /**
     * Adds an entry to the capability.
     * When overriding this method you can return false if the entry should not be added. E.g. if the maximum capacity is reached
     *
     * @param entry the entry to add
     *              <code>entry</code> must not be null
     * @return <code>true</code> if the entry was added to the capability
     */
    public boolean add(@NotNull Value entry) {
        this.entries.add(entry);
        this.updated = true;
        return true;
    }

    /**
     * Removes an entry from the capability
     *
     * @param index the index to remove
     * @throws UnsupportedOperationException if the capability doesn't allow removal
     */
    public Value remove(int index) {
        if (!this.allowRemoval()) {
            throw new UnsupportedOperationException("The capability doesn't allow removal");
        }
        Value value = this.entries.removeIndex(index);
        this.updated = true;
        return value;
    }

    /**
     * Whether the capability allows removal
     *
     * @return <code>true</code> if the capability allows removal
     */
    public abstract boolean allowRemoval();
}
