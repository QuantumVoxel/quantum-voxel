package dev.ultreon.quantum.client.input.controller;

import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Preconditions;
import dev.ultreon.quantum.client.input.controller.gui.ConfigEntry;
import dev.ultreon.quantum.client.input.dyn.ControllerInterDynamic;
import dev.ultreon.quantum.text.TextObject;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public sealed class ControllerAction<T extends Enum<T> & ControllerInterDynamic<?>> permits ControllerAction.Axis, ControllerAction.Button, ControllerAction.Joystick, ControllerAction.Nulled, ControllerAction.Trigger {
    private final T defaultValue;
    private final Type type;
    private final Vector2 tmp = new Vector2();
    private ControllerAction<T> nullAction;
    private ConfigEntry<T> entry;

    protected ControllerAction(@UnknownNullability T value, Type type) {
        if (this.getClass() != Nulled.class)
            Preconditions.checkNotNull(value, "Controller action value shouldn't be null.");

        this.type = type;
        this.defaultValue = value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum<T> & ControllerInterDynamic<?>> ControllerAction<T> create(Class<T> type, String text) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        Class clazz = type;
        if (clazz == ControllerBoolean.class)
            return (ControllerAction<T>) new Button((ControllerBoolean) EnumUtils.getEnum(clazz, text));
        else if (clazz == ControllerSignedFloat.class)
            return (ControllerAction<T>) new Axis((ControllerSignedFloat) EnumUtils.getEnum(clazz, text));
        else if (clazz == ControllerVec2.class)
            return (ControllerAction<T>) new Joystick((ControllerVec2) EnumUtils.getEnum(clazz, text));
        else if (clazz == ControllerUnsignedFloat.class)
            return (ControllerAction<T>) new Trigger((ControllerUnsignedFloat) EnumUtils.getEnum(clazz, text));
        else throw new IllegalArgumentException("Unknown controller action type: " + clazz);
    }

    /**
     * Get the mapping of the action
     *
     * @return the mapping
     */
    public T getMapping() {
        return entry == null ? getDefaultValue() : entry.get();
    }


    /**
     * Set the mapping of the action
     *
     * @param mapping the mapping
     */
    public void setMapping(T mapping) {
        if (entry == null) return;
        this.entry.set(mapping);
    }

    /**
     * Check if the action is pressed
     *
     * @return true if the action is pressed, false otherwise.
     */
    public boolean isPressed() {
        return getMapping().asBoolean().isPressed();
    }

    /**
     * Get the value of the action
     *
     * @return a value between 0..1
     */
    public float getValue() {
        return getMapping().asUnsignedFloat().getValue();
    }

    /**
     * Get the axis value of the action
     *
     * @return a value between -1..1
     */
    public float getAxisValue() {
        return getMapping().asSignedFloat().getValue();
    }

    /**
     * Get the 2D value of the action
     *
     * @return a 2D vector with values between -1..1
     */
    public Vector2 get2DValue() {
        return getMapping().asVec2().get(this.tmp);
    }

    public boolean isJustPressed() {
        return getMapping().asBoolean().isJustPressed();
    }

    public boolean isJustReleased() {
        return getMapping().asBoolean().isJustReleased();
    }


    private ConfigEntry<T> createEntry0(Config config, ControllerMapping<T> mapping, String id, TextObject description) {
        return config.add(id, mapping, description);
    }

    public ConfigEntry<T> createEntry(Config config, ControllerMapping<T> mapping, String id, TextObject description) {
        return this.entry = createEntry0(config, mapping, id, description);
    }

    public @NotNull ControllerAction<T> nulled() {
        if (this.nullAction == null) {
            this.nullAction = new Nulled();
        }

        return this.nullAction;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    protected Type getType() {
        return type;
    }

    public static final class Button extends ControllerAction<ControllerBoolean> {
        public Button(ControllerBoolean value) {
            super(value, Type.BOOLEAN);
        }
    }

    public static final class Axis extends ControllerAction<ControllerSignedFloat> {
        public Axis(ControllerSignedFloat value) {
            super(value, Type.SIGNED_FLOAT);
        }

    }
    public static final class Trigger extends ControllerAction<ControllerUnsignedFloat> {
        public Trigger(ControllerUnsignedFloat trigger) {
            super(trigger, Type.UNSIGNED_FLOAT);
        }
    }

    public static final class Joystick extends ControllerAction<ControllerVec2> {
        public Joystick(ControllerVec2 value) {
            super(value, Type.VEC2);
        }
    }

    final class Nulled extends ControllerAction<T> {
        private Nulled() {
            super(null, Type.BOOLEAN /* Placeholder */);
        }

        @Override
        public T getMapping() {
            return ControllerAction.this.getMapping();
        }

        @Override
        public void setMapping(T mapping) {
            ControllerAction.this.setMapping(mapping);
        }

        @Override
        public boolean isPressed() {
            return false;
        }

        @Override
        public boolean isJustPressed() {
            return false;
        }

        @Override
        public boolean isJustReleased() {
            return false;
        }

        @Override
        public ConfigEntry<T> createEntry(Config config, ControllerMapping<T> mapping, String id, TextObject description) {
            return ControllerAction.this.createEntry(config, mapping, id, description);
        }

        @Override
        public T getDefaultValue() {
            return ControllerAction.this.getDefaultValue();
        }

        @Override
        public float getValue() {
            return 0;
        }

        @Override
        public float getAxisValue() {
            return 0;
        }

        @Override
        public Vector2 get2DValue() {
            return new Vector2(0, 0);
        }
        @Override
        public Type getType() {
            return ControllerAction.this.getType();
        }

    }

    public enum Type {
        BOOLEAN,
        SIGNED_FLOAT,
        UNSIGNED_FLOAT,
        VEC2
    }
}
