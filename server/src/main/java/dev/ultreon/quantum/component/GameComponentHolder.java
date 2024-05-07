package dev.ultreon.quantum.component;

import dev.ultreon.quantum.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public interface GameComponentHolder<T extends GameComponent<?>> {
    default Collection<T> components() {
        return Collections.unmodifiableCollection(this.componentRegistry().values());
    }

    Map<Identifier, T> componentRegistry();

    <T2 extends GameComponent<?>> T2 getComponent(Identifier id, T2[] typeGetter);
}
