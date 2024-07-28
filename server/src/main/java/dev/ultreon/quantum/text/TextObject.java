package dev.ultreon.quantum.text;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TextObject {
    private Array<TextPart> baked;

    protected TextObject() {
        super();
    }

    public static TextObject deserialize(MapType data) {
        String type = data.getString("type");
        return switch (type) {
            case "literal" -> LiteralText.deserialize(data);
            case "translation" -> TranslationText.deserialize(data);
            case "empty" -> TextObject.empty();
            case "font_icon" -> FontIconObject.deserialize(data);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
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
        return new TextObject() {
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
        };
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
        if (baked != null) return baked;

        Array<TextPart> baked = new Array<>(TextPart.class);
        this.bake(baked);
        this.baked = baked;

        return baked;
    }

    public abstract MutableText copy();

    public TextStyle getStyle() {
        return TextStyle.defaultStyle();
    }

    protected abstract void bake(Array<TextPart> bake);
}
