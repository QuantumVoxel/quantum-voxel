package dev.ultreon.quantum.client.gui;

import com.github.tommyettinger.textra.Font;
import dev.ultreon.quantum.client.ClientRegistries;
import dev.ultreon.quantum.client.FontManager;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.registry.RegistryKey;

import static dev.ultreon.quantum.client.QuantumClient.id;

@SuppressWarnings("GDXJavaStaticResource")
public class Fonts {
    public static RegistryKey<Font> QUANTIUM = new RegistryKey<>(ClientRegistries.FONT.key(), id("quantum"));

    public static void register() {
        QuantumClient.get().fontManager.registerFonts(QuantumClient.get().getResourceManager());
    }
}
