package dev.ultreon.quantum.client.input.controller;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.input.controller.gui.ConfigEntry;
import dev.ultreon.quantum.client.input.dyn.ControllerInterDynamic;
import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.events.api.EventResult;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public final class ControllerMapping<T extends Enum<T> & ControllerInterDynamic<?>> {
    ControllerAction<T> action;
    private final String id;
    private final Side side;
    private final TextObject name;
    private boolean visible ;
    private final Predicate<QuantumClient> condition;
    private final Event<VisibilityEvent> event = Event.withResult();
    private final ControllerAction<T> defaultAction;

    public ControllerMapping(ControllerAction<T> action, Side side, TextObject name, boolean visible, String id,
                             Predicate<QuantumClient> condition) {
        this.action = action;
        this.defaultAction = action;
        this.side = side;
        this.name = name;
        this.visible = visible;
        this.id = id;
        this.condition = condition;
    }

    public ControllerMapping(ControllerAction<T> action, Side side, TextObject name, boolean visible, String id) {
        this(action, side, name, visible, id, (client) -> true);
    }

    public ControllerMapping(ControllerAction<T> action, Side side, TextObject name, String id, Predicate<QuantumClient> condition) {
        this(action, side, name, true, id, condition);
    }

    public ControllerMapping(ControllerAction<T> action, Side side, TextObject name, String id) {
        this(action, side, name, true, id);
    }

    public ControllerMapping<ControllerBoolean> of(ControllerBoolean action, Side side, TextObject name, boolean visible, String id,
                                Predicate<QuantumClient> condition) {
        return new ControllerMapping<>(new ControllerAction.Button(action), side, name, visible, id, condition);
    }

    public ControllerMapping<ControllerBoolean> of(ControllerBoolean action, Side side, TextObject name, boolean visible, String id) {
        return new ControllerMapping<>(new ControllerAction.Button(action), side, name, visible, id);
    }

    public ControllerMapping<ControllerBoolean> of(ControllerBoolean action, Side side, TextObject name, String id, Predicate<QuantumClient> condition) {
        return new ControllerMapping<>(new ControllerAction.Button(action), side, name, true, id, condition);
    }

    public ControllerMapping<ControllerBoolean> of(ControllerBoolean action, Side side, TextObject name, String id) {
        return new ControllerMapping<>(new ControllerAction.Button(action), side, name, true, id);
    }

    public ControllerMapping<ControllerSignedFloat> of(ControllerSignedFloat action, Side side, TextObject name, boolean visible, String id, Predicate<QuantumClient> condition) {
        return new ControllerMapping<>(new ControllerAction.Axis(action), side, name, visible, id, condition);
    }

    public ControllerMapping<ControllerSignedFloat> of(ControllerSignedFloat action, Side side, TextObject name, boolean visible, String id) {
        return new ControllerMapping<>(new ControllerAction.Axis(action), side, name, visible, id);
    }

    public ControllerMapping<ControllerSignedFloat> of(ControllerSignedFloat action, Side side, TextObject name, String id, Predicate<QuantumClient> condition) {
        return new ControllerMapping<>(new ControllerAction.Axis(action), side, name, true, id, condition);
    }

    public ControllerMapping<ControllerSignedFloat> of(ControllerSignedFloat action, Side side, TextObject name, String id) {
        return new ControllerMapping<>(new ControllerAction.Axis(action), side, name, true, id);
    }

    public ControllerMapping<ControllerVec2> of(ControllerVec2 action, Side side, TextObject name, boolean visible, String id, Predicate<QuantumClient> condition) {
        return new ControllerMapping<>(new ControllerAction.Joystick(action), side, name, visible, id, condition);
    }

    public ControllerMapping<ControllerVec2> of(ControllerVec2 action, Side side, TextObject name, boolean visible, String id) {
        return new ControllerMapping<>(new ControllerAction.Joystick(action), side, name, visible, id);
    }

    public ControllerMapping<ControllerVec2> of(ControllerVec2 action, Side side, TextObject name, String id, Predicate<QuantumClient> condition) {
        return new ControllerMapping<>(new ControllerAction.Joystick(action), side, name, true, id, condition);
    }

    public ControllerMapping<ControllerVec2> of(ControllerVec2 action, Side side, TextObject name, String id) {
        return new ControllerMapping<>(new ControllerAction.Joystick(action), side, name, true, id);
    }

    public ControllerMapping<ControllerUnsignedFloat> of(ControllerUnsignedFloat action, Side side, TextObject name, boolean visible, String id, Predicate<QuantumClient> condition) {
        return new ControllerMapping<>(new ControllerAction.Trigger(action), side, name, visible, id, condition);
    }

    public ControllerMapping<ControllerUnsignedFloat> of(ControllerUnsignedFloat action, Side side, TextObject name, boolean visible, String id) {
        return new ControllerMapping<>(new ControllerAction.Trigger(action), side, name, visible, id);
    }

    public ControllerMapping<ControllerUnsignedFloat> of(ControllerUnsignedFloat action, Side side, TextObject name, String id, Predicate<QuantumClient> condition) {
        return new ControllerMapping<>(new ControllerAction.Trigger(action), side, name, true, id, condition);
    }

    public ControllerMapping<ControllerUnsignedFloat> of(ControllerUnsignedFloat action, Side side, TextObject name, String id) {
        return new ControllerMapping<>(new ControllerAction.Trigger(action), side, name, true, id);
    }

    public boolean isVisible() {
        EventResult eventResult = this.event.factory().onVisible(visible);
        if (eventResult.isInterrupted()) return eventResult.isCanceled();
        return visible && condition.test(QuantumClient.get());
    }

    public boolean isEnabled() {
        return condition.test(QuantumClient.get());
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return "ControllerMapping[" +
                "action=" + action + ", " +
                "side=" + side + ", " +
                "name=" + name + ']';
    }

    public @NotNull ControllerAction<T> getAction() {
        if (!isEnabled()) return action.nulled();
        return action;
    }

    public Side getSide() {
        return side;
    }

    public TextObject getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ControllerMapping<?>) obj;
        return Objects.equals(this.action, that.action) &&
                Objects.equals(this.side, that.side) &&
                Objects.equals(this.name, that.name) &&
                this.visible == that.visible;
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, side, name, visible);
    }

    public String getId() {
        return id;
    }

    public ConfigEntry<?> createEntry(Config config) {
        NamespaceID id = config.getContext().id;
        return action.createEntry(config, this, id.getDomain() + ".action." + id.getPath().replace('/', '.') + "." + this.id, name);
    }

    public ControllerMapping<T> withValue(Class<T> clazz, String text) {
        this.action = ControllerAction.create(clazz, text);
        return this;
    }

    public void setAction(@NotNull ControllerAction<T> tControllerMapping) {
        this.action = tControllerMapping;
    }

    public ControllerAction<T> getDefaultAction() {
        return defaultAction;
    }

    public enum Side {
        LEFT,
        RIGHT
    }

    @FunctionalInterface
    public interface VisibilityEvent {
        EventResult onVisible(boolean visible);
    }
}
