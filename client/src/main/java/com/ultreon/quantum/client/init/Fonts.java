package com.ultreon.quantum.client.init;

import com.ultreon.quantum.CommonConstants;
import com.ultreon.quantum.client.ClientRegistries;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.font.Font;
import com.ultreon.quantum.util.Identifier;

@SuppressWarnings("GDXJavaStaticResource")
public class Fonts {
    public static final Font DEFAULT = Fonts.register("default", QuantumClient.get().font);

    private static Font register(String name, Font font) {
        ClientRegistries.FONT.register(new Identifier(CommonConstants.NAMESPACE, name), font);
        return font;
    }

    public static void nopInit() {
        // Empty for class initialization
    }
}
