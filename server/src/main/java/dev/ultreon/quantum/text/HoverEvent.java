package dev.ultreon.quantum.text;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.world.Location;

import java.io.Serializable;
import java.util.Objects;

public final class HoverEvent<T> implements Serializable {
    private final Action action;
    private final T value;

    public HoverEvent(Action action, T value) {
        this.action = action;
        this.value = value;
    }

    public static HoverEvent<TextObject> text(String text) {
        return new HoverEvent<>(Action.TEXT, TextObject.literal(text));
    }

    public static HoverEvent<TextObject> text(TextObject text) {
        return new HoverEvent<>(Action.TEXT, text);
    }

    public static HoverEvent<ItemStack> item(ItemStack stack) {
        return new HoverEvent<>(Action.TEXT, stack);
    }

    public static HoverEvent<Location> location(Location stack) {
        return new HoverEvent<>(Action.TEXT, stack);
    }

    public static HoverEvent<Player> location(Player stack) {
        return new HoverEvent<>(Action.TEXT, stack);
    }

    public static HoverEvent<Entity> location(Entity stack) {
        return new HoverEvent<>(Action.TEXT, stack);
    }

    public Action action() {
        return action;
    }

    public T value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (HoverEvent) obj;
        return Objects.equals(this.action, that.action) &&
               Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, value);
    }

    @Override
    public String toString() {
        return "HoverEvent[" +
               "action=" + action + ", " +
               "value=" + value + ']';
    }


    public enum Action {
        TEXT,
        ITEM,
        LOCATION,
        PLAYER,
        ENTITY,
    }
}
