package dev.ultreon.blockstudio;

import com.badlogic.gdx.Game;

public class BlockStudio extends Game {
    private static BlockStudio instance;

    public static BlockStudio getInstance() {
        return instance;
    }

    @Override
    public void create() {
        instance = this;

        System.out.println("Hello Block Studio!");

        setScreen(new BlockEditorScreen());
    }
}