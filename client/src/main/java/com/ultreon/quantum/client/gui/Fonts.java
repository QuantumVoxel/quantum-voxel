package com.ultreon.quantum.client.gui;

import com.ultreon.quantum.client.ClientRegistries;
import com.ultreon.quantum.client.FontManager;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.font.Font;
import com.ultreon.quantum.registry.RegistryKey;

import static com.ultreon.quantum.client.QuantumClient.id;

@SuppressWarnings("GDXJavaStaticResource")
public class Fonts {
    public static RegistryKey<Font> QUANTIUM = new RegistryKey<>(ClientRegistries.FONT.key(), id("quantum"));

    public static void register() {
        FontManager.get().registerFonts(QuantumClient.get().getResourceManager());
    }
}
