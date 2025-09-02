//package dev.ultreon.quantum.client.input.controller;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public final class ControllerMappings {
//    private final List<ControllerMapping<ControllerBoolean>> buttonMappings = new ArrayList<>();
//    private final List<ControllerMapping<ControllerSignedFloat>> axisMappings = new ArrayList<>();
//    private final List<ControllerMapping<ControllerVec2>> joystickMappings = new ArrayList<>();
//    private final List<ControllerMapping<ControllerUnsignedFloat>> triggerMappings = new ArrayList<>();
//
//    public List<ControllerMapping<ControllerBoolean>> getButtonMappings() {
//        return buttonMappings;
//    }
//
//    public List<ControllerMapping<ControllerSignedFloat>> getAxisMappings() {
//        return axisMappings;
//    }
//
//    public List<ControllerMapping<ControllerVec2>> getJoystickMappings() {
//        return joystickMappings;
//    }
//
//    public List<ControllerMapping<ControllerUnsignedFloat>> getTriggerMappings() {
//        return triggerMappings;
//    }
//
//    public List<ControllerMapping<?>> getLeftSideMappings() {
//        List<ControllerMapping<?>> mappings = new ArrayList<>();
//        mappings.addAll(this.getButtonMappings());
//        mappings.addAll(this.getAxisMappings());
//        mappings.addAll(this.getTriggerMappings());
//        mappings.addAll(this.getJoystickMappings());
//        return mappings.stream().filter(mapping -> mapping.getSide() == ControllerMapping.Side.LEFT).collect(Collectors.toUnmodifiableList());
//    }
//
//    public List<ControllerMapping<?>> getRightSideMappings() {
//        List<ControllerMapping<?>> mappings = new ArrayList<>();
//        mappings.addAll(this.getButtonMappings());
//        mappings.addAll(this.getAxisMappings());
//        mappings.addAll(this.getTriggerMappings());
//        mappings.addAll(this.getJoystickMappings());
//        return mappings.stream().filter(mapping -> mapping.getSide() == ControllerMapping.Side.RIGHT).collect(Collectors.toUnmodifiableList());
//    }
//
//    @SuppressWarnings("unchecked")
//    public <T extends ControllerMapping<?>> T register(T mapping) {
//        if (mapping.action instanceof ControllerAction.Button) {
//            this.buttonMappings.add((ControllerMapping<ControllerBoolean>) mapping);
//        } else if (mapping.action instanceof ControllerAction.Axis) {
//            this.axisMappings.add((ControllerMapping<ControllerSignedFloat>) mapping);
//        } else if (mapping.action instanceof ControllerAction.Joystick) {
//            this.joystickMappings.add((ControllerMapping<ControllerVec2>) mapping);
//        } else if (mapping.action instanceof ControllerAction.Trigger) {
//            this.triggerMappings.add((ControllerMapping<ControllerUnsignedFloat>) mapping);
//        } else {
//            throw new IllegalArgumentException("Unsupported controller action: " + mapping.action.getClass().getName());
//        }
//
//        return mapping;
//    }
//
//    public Iterable<ControllerMapping<?>> getAllMappings() {
//        List<ControllerMapping<?>> mappings = new ArrayList<>();
//        mappings.addAll(this.getButtonMappings());
//        mappings.addAll(this.getAxisMappings());
//        mappings.addAll(this.getTriggerMappings());
//        mappings.addAll(this.getJoystickMappings());
//        return mappings;
//    }
//}
