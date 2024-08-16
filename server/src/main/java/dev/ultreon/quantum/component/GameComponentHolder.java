package dev.ultreon.quantum.component;

import dev.ultreon.quantum.util.NamespaceID;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public interface GameComponentHolder<T extends GameComponent<?>> {
    default Collection<T> components() {
        return Collections.unmodifiableCollection(this.componentRegistry().values());
    }

    Map<NamespaceID, T> componentRegistry();

    <T2 extends GameComponent<?>> T2 getComponent(NamespaceID id, T2[] typeGetter);
}
