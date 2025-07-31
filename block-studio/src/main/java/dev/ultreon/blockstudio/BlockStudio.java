package dev.ultreon.blockstudio;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import space.earlygrey.shapedrawer.ShapeDrawer;

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