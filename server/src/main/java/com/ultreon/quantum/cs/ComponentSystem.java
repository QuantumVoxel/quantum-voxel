package com.ultreon.quantum.cs;

import com.ultreon.quantum.collection.OrderedMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Map;

public class ComponentSystem {
    private final Map<String, Component> components = new OrderedMap<>();

    public void onTick() {
        for (Component component : components.values()) {
            component.onTick();
        }
    }

    public <T extends Component> T addComponent(String name, T component) {
        components.put(name, component);
        return component;
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T removeComponent(String name, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();
        Component remove = components.remove(name);
        if (remove == null) return null;
        return type.cast(remove);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <T extends Component> T getComponent(String name, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();
        Component obj = components.get(name);
        if (obj == null) throw new IllegalArgumentException("Component " + name + " does not exist!");

        try {
            T cast = type.cast(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException("Component " + name + " is not of type " + type.getName());
        }

        return (T) obj;
    }

    public <T extends Component> T getComponent(String name, Class<T> type) {
        Component obj = components.get(name);
        if (obj == null) throw new IllegalArgumentException("Component " + name + " does not exist!");
        if (!type.isAssignableFrom(obj.getClass()))
            throw new IllegalArgumentException("Component " + name + " is not of type " + type.getName());
        return type.cast(obj);
    }

    public Map<String, Component> getComponents() {
        return Collections.unmodifiableMap(components);
    }

    @ApiStatus.Internal
    public void clear() {
        components.clear();
    }
}
