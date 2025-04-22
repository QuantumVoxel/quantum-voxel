//package dev.ultreon.quantum.client.input.controller.entries;
//
//import dev.ultreon.quantum.client.gui.widget.TextButton;
//import dev.ultreon.quantum.client.gui.widget.Widget;
//import dev.ultreon.quantum.client.input.controller.Config;
//import dev.ultreon.quantum.client.input.controller.ControllerAction;
//import dev.ultreon.quantum.client.input.controller.ControllerContext;
//import dev.ultreon.quantum.client.input.controller.ControllerMapping;
//import dev.ultreon.quantum.client.input.controller.gui.ConfigEntry;
//import dev.ultreon.quantum.client.input.dyn.ControllerInterDynamic;
//import dev.ultreon.quantum.text.TextObject;
//import org.jetbrains.annotations.NotNull;
//
//public class ControllerBindingEntry<T extends Enum<T> & ControllerInterDynamic<?>> extends ConfigEntry<T> {
//    private final Class<T> clazz;
//    private final ControllerMapping<T> mapping;
//
//    @SuppressWarnings("unchecked")
//    public ControllerBindingEntry(String key, ControllerMapping<T> mapping, ControllerMapping<T> value, TextObject description) {
//        super(key, value.getAction().getDefaultValue(), description);
//        this.mapping = mapping;
//
//        ControllerAction<T> action = value.getAction();
//        this.clazz = (Class<T>) action.getMapping().getClass();
//    }
//
//    @Override
//    protected T read(String text) {
//        return mapping.getAction().getMapping();
//    }
//
//    @Override
//    public Widget createButton(Config options, int x, int y, int width) {
//        final ControllerInputButton button = new ControllerInputButton(x, y, width, 20, TextObject.nullToEmpty("Value"), options.getContext(), mapping);
//        button.setAction(mapping.getAction());
//        return button;
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public void setFromWidget(Widget widget) {
//        ControllerInputButton button = (ControllerInputButton) widget;
//        ControllerAction<T> value = button.getAction();
//        this.set(value.getMapping());
//    }
//
//    public class ControllerInputButton extends TextButton {
//        private final ControllerContext context;
//        private final ControllerMapping<T> mapping;
//        private ControllerAction<T> action;
//
//        public ControllerInputButton(int x, int y, int width, int height, TextObject message, ControllerContext context, ControllerMapping<T> mapping) {
//            super(width, height);
//            this.pos.set(x, y);
//
//            this.context = context;
//            this.mapping = mapping;
//            this.action = mapping.getAction();
//        }
//
//        @Override
//        public boolean click() {
//            client.controllerInput.interceptInputOnce((evt) -> {
//                if (evt.mapping().getClass() == clazz) {
//                    this.action.setMapping(evt.mapping().as(this.action.getMapping()));
//                    this.text().set(TextObject.nullToEmpty(evt.mapping().name()));
//                }
//            });
//            return false;
//        }
//
//        public ControllerContext getContext() {
//            return context;
//        }
//
//        public ControllerMapping<T> getMapping() {
//            return mapping;
//        }
//
//        public ControllerAction<T> getAction() {
//            return this.action;
//        }
//
//        public void setAction(@NotNull ControllerAction<T> action) {
//            this.action = action;
//            this.text().set(TextObject.nullToEmpty(action.getMapping().name()));
//        }
//
//        public void actuallySetAction(@NotNull ControllerAction<T> tControllerMapping) {
//            mapping.setAction(tControllerMapping);
//        }
//
//        public void reset() {
//            this.action = mapping.getDefaultAction();
//        }
//    }
//}
