package dev.ultreon.quantum.text;

import com.google.common.base.Preconditions;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.text.icon.FontIconMap;
import dev.ultreon.quantum.text.icon.IconMap;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;

public class FontIconObject extends MutableText {
    private final String id;
    private final FontIconMap fontIconMap;

    public FontIconObject(String id, FontIconMap fontIconMap) {
        Preconditions.checkNotNull(id, "Icon ID may not be null.");
        Preconditions.checkNotNull(fontIconMap, "Font Icon Map may not be null.");
        this.id = id;
        this.fontIconMap = fontIconMap;
    }

    public static FontIconObject deserialize(MapType data) {
        String id = data.getString("id");
        NamespaceID iconMapId = NamespaceID.tryParse(data.getString("iconMap"));
        FontIconMap iconMap = iconMapId == null
                ? IconMap.INSTANCE
                : Registries.FONT_ICON_MAP.get(iconMapId);

        return new FontIconObject(id, iconMap);
    }

    @Override
    public @NotNull String createString() {
        return "";
    }

    @Override
    public MapType serialize() {
        MapType data = new MapType();
        data.putString("type", "font_icon");
        data.putString("id", id);
        data.putString("iconMap", fontIconMap.getId().toString());

        return data;
    }

    @Override
    public MutableText copy() {
        return new FontIconObject(id, this.fontIconMap);
    }

    @Override
    protected @NotNull FontIconPart createPart() {
        return new FontIconPart(fontIconMap, id);
    }

    public FontIconMap getIconMap() {
        return fontIconMap;
    }

    public String getIconName() {
        return id;
    }
}
