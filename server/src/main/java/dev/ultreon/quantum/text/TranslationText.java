package dev.ultreon.quantum.text;

import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TranslationText extends MutableText {
    private final @NotNull String path;
    private final Object @NotNull [] args;
    private String translated = null;
    private boolean initialized = false;

    TranslationText(@NotNull String path, Object @NotNull ... args) {
        this.path = path;
        this.args = args;
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
        String translated = getTranslated();
        if (!Objects.equals(this.translated, translated)) {
            this.translated = translated;
            this.initFormat();
        }
        return this.translated;
    }

    private void initFormat() {
        if (this.initialized) this.extras.removeFirst();
        this.extras.addFirst(Formatter.format(getTranslated(), false));
        this.initialized = true;
    }

    private String getTranslated() {
        String translate = LanguageBootstrap.translate(this.path, this.args);
        if (translate == null) translate = this.path;
        return translate;
    }

    @Override
    public @NotNull String getText() {
        Object @NotNull [] objects = this.args;
        for (int i = 0, objectsLength = objects.length; i < objectsLength; i++) {
            Object arg = objects[i];
            if (arg instanceof TextObject textObject) {
                objects[i] = textObject.createString();
            }
        }
        return getTranslated();
    }

    @Override
    public MapType serialize() {
        MapType data = new MapType();
        data.putString("type", "translation");
        data.putString("path", this.path);

        data.put("Style", this.style.serialize());

        ListType<MapType> argsData = new ListType<>();
        for (Object arg : this.args) {
            if (arg instanceof TextObject textObject) {
                argsData.add(textObject.serialize());
            } else if (arg instanceof String s) {
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

    @Override
    public TranslationText style(Consumer<TextStyle> consumer) {
        return (TranslationText) super.style(consumer);
    }

    @Override
    protected Stream<TextObject> stream() {
        var builder = new ArrayList<TextObject>();
        builder.add(this);
        boolean e = false;
        for (var extra : this.extras) {
            if (!e) {
                e = true;
                continue;
            }
            builder.addAll(extra.stream().toList());
        }
        return builder.stream();
    }

    @Override
    public MutableText copy() {
        var copy = this.extras.stream().map(TextObject::copy).toList();
        var translationText = new TranslationText(this.path, this.args);
        translationText.extras.addAll(copy);
        return translationText;
    }
}
