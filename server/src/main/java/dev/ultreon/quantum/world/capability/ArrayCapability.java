package dev.ultreon.quantum.world.capability;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.world.World;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Experimental
public abstract class ArrayCapability<Value> implements Capability<Array<Value>> {
    private final CapabilityType<? extends ArrayCapability<Value>, Array<Value>> type;
    private final Class<? extends Value> clazz;
    protected final Array<Value> entries = new Array<>();
    protected boolean updated = false;

    public ArrayCapability(CapabilityType<? extends ArrayCapability<Value>, Array<Value>> type, Class<? extends Value> clazz) {
        this.type = type;
        this.clazz = clazz;
    }

    public ArrayCapability(CapabilityType<? extends ModifiableCapability<Value>, Array<Value>> type, Array<Value> entries, Class<? extends Value> clazz) {
        this.clazz = clazz;
        this.type = type;
        this.entries.addAll(entries);
    }

    public ArrayCapability(CapabilityType<? extends ArrayCapability<Value>, Array<Value>> type, MapType mapType, Class<? extends Value> clazz) {
        this.clazz = clazz;
        this.type = type;

        List<MapType> value = mapType.<MapType>getList("entries").getValue();
        for (MapType entry : value) {
            this.entries.add(this.loadEntry(entry));
        }
    }

    public @Nullable Value get(int index) {
        return entries.get(index);
    }

    public int size() {
        return entries.size;
    }

    @Override
    public CapabilityType<? extends ArrayCapability<Value>, Array<Value>> getType() {
        return type;
    }

    @Override
    public boolean isUpdated() {
        return updated;
    }

    @Override
    @ApiStatus.Internal
    public Array<Value> get() {
        return entries;
    }

    @Override
    public void save(MapType data) {
        ListType<MapType> list = new ListType<>();
        for (Value entry : this.entries.toArray(clazz)) {
            list.add(this.saveEntry(entry, data));
        }
        data.put("entries", list);
    }

    @Override
    public MapType load(World world, MapType data) {
        this.entries.clear();
        List<MapType> value = data.<MapType>getList("entries").getValue();
        for (int i = 0, valueSize = value.size(); i < valueSize; i++) {
            MapType entry = value.get(i);
            this.entries.set(i, loadEntry(entry));
        }
        return data;
    }

    public abstract MapType saveEntry(Value entry, MapType data);

    public abstract Value loadEntry(MapType data);
}
