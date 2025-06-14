package dev.ultreon.quantum.text;

import dev.ultreon.quantum.ubo.types.ListType;
import dev.ultreon.quantum.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TranslationText extends MutableText {
    private final @NotNull String path;
    private final Object @NotNull [] args;
    private boolean initialized = false;

    TranslationText(@NotNull String path, Object @NotNull ... args) {
        this.path = path;
        this.args = args;

        this.revalidate();
    }

    public static TranslationText deserialize(MapType data) {
        String path = data.getString("path");

        ListType<MapType> argsData = data.getList("Args");
        List<TextObject> args = new ArrayList<>();
        for (MapType argData : argsData) {
            args.add(TextObject.deserialize(argData));
        }
        TranslationText translation = new TranslationText(path, args.toArray(new Object[]{}));

        translation.style = TextStyle.deserialize(data.getMap("Style"));

        ListType<MapType> extrasData = data.getList("Extras");
        for (MapType extraData : extrasData.getValue()) {
            translation.extras.add(TextObject.deserialize(extraData));
        }

        return translation;
    }

    @Override
    public @NotNull String createString() {
        return this.getTranslated();
    }

    private void initFormat() {
        if (this.initialized) this.extras.remove(0);
        this.extras.add(0, Formatter.format(getTranslated(), false));
        this.initialized = true;
    }

    private String getTranslated() {
        return LanguageBootstrap.translate(this.path, this.args);
    }

    @Override
    public @NotNull String getText() {
        return this.getTranslated() + this.extras.stream().map(TextObject::getText).reduce("", (a, b) -> a + b);
    }

    @Override
    public MapType serialize() {
        MapType data = new MapType();
        data.putString("type", "translation");
        data.putString("path", this.path);

        data.put("Style", this.style.serialize());

        ListType<MapType> argsData = new ListType<>();
        for (Object arg : this.args) {
            if (arg instanceof TextObject) {
                TextObject textObject = (TextObject) arg;
                argsData.add(textObject.serialize());
            } else if (arg instanceof String) {
                String s = (String) arg;
                argsData.add(TextObject.literal(s).serialize());
            } else argsData.add(TextObject.literal(String.valueOf(arg)).serialize());
        }
        data.put("Args", argsData);

        ListType<MapType> extrasData = new ListType<>();
        for (TextObject extra : this.extras) {
            extrasData.add(extra.serialize());
        }
        data.put("Extras", extrasData);

        return data;
    }

    private void revalidate() {

    }

    @Override
    protected @NotNull StylePart createPart() {
        return new StylePart(this.getTranslated(), this.style.compact(), this.style.compactColor(), this.style.size());
    }

    @Override
    public TranslationText style(Consumer<TextStyle> consumer) {
        return (TranslationText) super.style(consumer);
    }

    @Override
    public MutableText copy() {
        var copy = this.extras.stream().map(TextObject::copy).collect(Collectors.toList());
        var translationText = new TranslationText(this.path, this.args);
        translationText.extras.addAll(copy);
        return translationText;
    }
}
