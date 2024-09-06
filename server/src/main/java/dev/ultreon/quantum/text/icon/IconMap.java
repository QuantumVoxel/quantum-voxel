package dev.ultreon.quantum.text.icon;

import dev.ultreon.quantum.CommonConstants;

public class IconMap extends FontIconMap {
    public static final IconMap INSTANCE = new IconMap();

    private IconMap() {
        super(CommonConstants.id("icon"));
    }

    public static void register() {
        IconMap.INSTANCE.set("icon_success");
        IconMap.INSTANCE.set("icon_info");
        IconMap.INSTANCE.set("icon_warning");
        IconMap.INSTANCE.set("icon_error");
        IconMap.INSTANCE.set("icon_denied");
        IconMap.INSTANCE.set("icon_fatal");
        IconMap.INSTANCE.set("icon_debug");
        IconMap.INSTANCE.set("tag_owner");
        IconMap.INSTANCE.set("tag_admin");
        IconMap.INSTANCE.set("tag_server");
        IconMap.INSTANCE.set("tag_console");
        IconMap.INSTANCE.set("tag_death");
        IconMap.INSTANCE.set("tag_broadcast");
        IconMap.INSTANCE.set("tag_join");
        IconMap.INSTANCE.set("tag_leave");
    }
}