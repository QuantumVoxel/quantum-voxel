package dev.ultreon.quantum.text;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TextObject {
    protected TextObject() {
        super();
    }

    public static TextObject deserialize(MapType data) {
        String type = data.getString("type");
        switch (type) {
            case "literal":
                return LiteralText.deserialize(data);
            case "translation":
                return TranslationText.deserialize(data);
            case "empty":
                return TextObject.empty();
            case "font_icon":
                return FontIconObject.deserialize(data);
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    public static TextObject nullToEmpty(TextObject title) {
        return title == null ? TextObject.empty() : title;
    }

    public abstract @NotNull String createString();

    public String getText() {
        return this.createString();
    }

    public abstract MapType serialize();

    public static TextObject empty() {
        return new MyTextObject();
    }

    public static LiteralText literal(@Nullable String text) {
        return new LiteralText(text != null ? text : "");
    }

    public static TranslationText translation(String path, Object... args) {
        return new TranslationText(path, args);
    }

    public static TextObject nullToEmpty(@Nullable String text) {
        return text == null ? TextObject.empty() : TextObject.literal(text);
    }

    public Array<TextPart> bake() {
        Array<TextPart> baked = new Array<>(TextPart.class);
        this.bake(baked);

        return baked;
    }

    public abstract MutableText copy();

    public TextStyle getStyle() {
        return TextStyle.defaultStyle();
    }

    protected abstract void bake(Array<TextPart> bake);

    @Override
    public String toString() {
        return getText();
    }

    private static class MyTextObject extends TextObject {
        private static final StylePart BAKED = new StylePart("", 0, 0, (byte) 1);

        @Override
        public @NotNull String createString() {
            return "";
        }

        @Override
        public MapType serialize() {
            MapType data = new MapType();
            data.putString("type", "empty");
            return data;
        }

        @Override
        public MutableText copy() {
            return new LiteralText(this.createString());
        }

        @Override
        protected void bake(Array<TextPart> bake) {
            bake.add(BAKED);
        }
    }
}
