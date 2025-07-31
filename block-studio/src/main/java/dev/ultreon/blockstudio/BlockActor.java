package dev.ultreon.blockstudio;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

public class BlockActor extends Group {
    private final Label postLabel;
    private final TextField input;
    private final Label preLabel;
    private final Drawable background;
    private final Table table;

    public BlockActor(String preText, String postText, Skin skin) {
        background = skin.getDrawable("button-orange");

        table = new Table();
        table.setLayoutEnabled(true);
        table.defaults().pad(5);
        table.defaults().fillX();
        table.defaults().space(0);

        preLabel = new Label(preText, new Label.LabelStyle(skin.getFont("font"), Color.WHITE));
        preLabel.setColor(Color.WHITE);
        preLabel.setWidth(preLabel.getPrefWidth());
        table.add(preLabel);

        input = new TextField("Hello World", skin);
        input.setWidth(input.getPrefWidth());
        table.add(input);

        postText = postText.replaceAll("\\s+", " ");
        postLabel = new Label(postText, new Label.LabelStyle(skin.getFont("font"), Color.WHITE));
        postLabel.setColor(Color.WHITE);
        postLabel.setWidth(postLabel.getPrefWidth());
        table.add(postLabel);
        table.align(Align.bottomLeft);
        addActor(table);

        table.addListener(new DragListener() {
            private float startX, startY;
            private boolean doNotDrag = false;
            private final Vector2 tmpVector = new Vector2();

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                if (x > input.getX() && x < input.getX() + input.getWidth() && y > input.getY() && y < input.getY() + input.getHeight()) {
                    doNotDrag = true;
                    return;
                }

                doNotDrag = false;

                startX = x;
                startY = y;
            }

            public void drag(InputEvent event, float x, float y, int pointer) {
                if (doNotDrag) return;
                setPosition(getX() + x - startX, getY() + y - startY);
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        input.setWidth(table.getPrefWidth());
        table.layout();

        background.draw(batch, getX(), getY(), table.getPrefWidth(), table.getPrefHeight());
        super.draw(batch, parentAlpha);
    }
}
