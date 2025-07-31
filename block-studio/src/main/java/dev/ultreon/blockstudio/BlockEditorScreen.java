package dev.ultreon.blockstudio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class BlockEditorScreen implements Screen {
    private Stage stage;
    private Skin skin;

    @Override
    public void show() {
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.classpath("uiskin.json")); // use default or custom skin

        BlockActor block = new BlockActor("Write ", " to the console", skin);
        block.setPosition(100, 300);
        stage.addActor(block);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(.2f, .2f, .2f, 1);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); }
}
