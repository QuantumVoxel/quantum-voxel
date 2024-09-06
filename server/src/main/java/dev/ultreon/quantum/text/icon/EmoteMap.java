package dev.ultreon.quantum.text.icon;

import dev.ultreon.quantum.CommonConstants;

public class EmoteMap extends FontIconMap {
    public static final EmoteMap INSTANCE = new EmoteMap();

    private EmoteMap() {
        super(CommonConstants.id("emote"));
    }

    public static void register() {
        EmoteMap.INSTANCE.set("smile");
        EmoteMap.INSTANCE.set("laugh");
        EmoteMap.INSTANCE.set("concern");
    }
}