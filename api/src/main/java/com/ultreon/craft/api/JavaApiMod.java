package com.ultreon.quantum.api;

import net.fabricmc.api.ModInitializer;

public class JavaApiMod implements ModInitializer {
    public static final String MOD_ID = "quantum_api";

    @Override
    public void onInitialize() {
        System.out.printf("Hello from Java! Mod ID: %s%n", MOD_ID);
    }
}
