package dev.ultreon.quantum.client.input.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

public final class ControllerMappings {
    private final List<ControllerMapping<ControllerBoolean>> buttonMappings = new ArrayList<>();
    private final List<ControllerMapping<ControllerSignedFloat>> axisMappings = new ArrayList<>();
    private final List<ControllerMapping<ControllerVec2>> joystickMappings = new ArrayList<>();
    private final List<ControllerMapping<ControllerUnsignedFloat>> triggerMappings = new ArrayList<>();

    public List<ControllerMapping<ControllerBoolean>> getButtonMappings() {
        return buttonMappings;
    }

    public List<ControllerMapping<ControllerSignedFloat>> getAxisMappings() {
        return axisMappings;
    }

    public List<ControllerMapping<ControllerVec2>> getJoystickMappings() {
        return joystickMappings;
    }

    public List<ControllerMapping<ControllerUnsignedFloat>> getTriggerMappings() {
        return triggerMappings;
    }

    public List<ControllerMapping<?>> getLeftSideMappings() {
        List<ControllerMapping<?>> mappings = new ArrayList<>();
        mappings.addAll(this.getButtonMappings());
        mappings.addAll(this.getAxisMappings());
        mappings.addAll(this.getTriggerMappings());
        mappings.addAll(this.getJoystickMappings());
        return mappings.stream().filter(mapping -> mapping.getSide() == ControllerMapping.Side.LEFT).toList();
    }

    public List<ControllerMapping<?>> getRightSideMappings() {
        List<ControllerMapping<?>> mappings = new ArrayList<>();
        mappings.addAll(this.getButtonMappings());
        mappings.addAll(this.getAxisMappings());
        mappings.addAll(this.getTriggerMappings());
        mappings.addAll(this.getJoystickMappings());
        return mappings.stream().filter(mapping -> mapping.getSide() == ControllerMapping.Side.RIGHT).toList();
    }

    @SuppressWarnings("unchecked")
    public <T extends ControllerMapping<?>> T register(T mapping) {
        Preconditions.checkNotNull(mapping, "mapping cannot be null");

        if (mapping.action instanceof ControllerAction.Button) {
            this.buttonMappings.add((ControllerMapping<ControllerBoolean>) mapping);
        } else if (mapping.action instanceof ControllerAction.Axis) {
            this.axisMappings.add((ControllerMapping<ControllerSignedFloat>) mapping);
        } else if (mapping.action instanceof ControllerAction.Joystick) {
            this.joystickMappings.add((ControllerMapping<ControllerVec2>) mapping);
        } else if (mapping.action instanceof ControllerAction.Trigger) {
            this.triggerMappings.add((ControllerMapping<ControllerUnsignedFloat>) mapping);
        } else {
            throw new IllegalArgumentException("Unsupported controller action: " + mapping.action.getClass().getName());
        }

        return mapping;
    }

    public Iterable<ControllerMapping<?>> getAllMappings() {
        return Iterables.concat(this.getButtonMappings(), this.getAxisMappings(), this.getTriggerMappings(), this.getJoystickMappings());
    }
}
