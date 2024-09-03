package dev.ultreon.quantum.text;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class LiteralText extends MutableText {
    private final @NotNull String text;

    LiteralText(@NotNull String text) {
        this.text = text;
    }

    public static LiteralText deserialize(MapType data) {
        String text = data.getString("text");
        LiteralText literal = new LiteralText(text);

        literal.style = TextStyle.deserialize(data.getMap("Style"));

        ListType<MapType> extrasData = data.getList("Extras");
        for (MapType extraData : extrasData.getValue()) {
            literal.extras.add(TextObject.deserialize(extraData));
        }

        return literal;
    }

    @Override
    public @NotNull String createString() {
        String prefix = "";
        prefix += "[%%%d00]".formatted(getSize());
        if (this.isBold()) prefix += "[*]";
        if (this.isItalic()) prefix += "[/]";
        if (this.isUnderlined()) prefix += "[_]";
        if (this.isStrikethrough()) prefix += "[~]";
        prefix += "[#%06x]".formatted((this.getColor().getRed() & 0xff) << 16 | (this.getColor().getGreen() & 0xff) << 8 | this.getColor().getBlue() & 0xff);
        return prefix + this.text;
    }

    @Override
    public MapType serialize() {
        MapType data = new MapType();
        data.putString("type", "literal");
        data.putString("text", this.text);

        data.put("Style", this.style.serialize());

        ListType<MapType> extrasData = new ListType<>();
        for (TextObject extra : this.extras) {
            extrasData.add(extra.serialize());
        }
        data.put("Extras", extrasData);

        return data;
    }

    @Override
    public LiteralText style(Consumer<TextStyle> consumer) {
        return (LiteralText) super.style(consumer);
    }

    @Override
    public LiteralText copy() {
        var copy = this.extras.stream().map(TextObject::copy).toList();
        var literalText = new LiteralText(this.text);
        literalText.extras.addAll(copy);
        return literalText;
    }

    @Override
    @NotNull
    protected StylePart createPart() {
        return new StylePart(text, style.compact(), style.compactColor(), style.size());
    }
}
