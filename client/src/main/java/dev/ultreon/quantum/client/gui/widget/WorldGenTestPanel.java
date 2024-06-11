package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.input.DesktopInput;
import dev.ultreon.quantum.client.input.GameInput;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.TerrainNoise;
import dev.ultreon.quantum.world.gen.CaveNoiseGenerator;

import java.util.Random;

public class WorldGenTestPanel extends Rectangle {
    private TerrainNoise terrainNoise;
    private CaveNoiseGenerator caveNoise;
    private int terrainX = 0;
    private static final Pixmap terrain = new Pixmap(1920, 256, Pixmap.Format.RGBA8888); // Make it RGBA4444 for cool looking glitch ;-)
    private static final Texture terrainTex = new Texture(terrain);
    private double z;

    public WorldGenTestPanel() {
        super(0, 0, 0, 255);

        this.terrainNoise = new TerrainNoise(new Random(0).nextLong());
        this.caveNoise = new CaveNoiseGenerator(new Random(0).nextLong());
        revalidateImage();
    }

    @Override
    protected void renderBackground(Renderer renderer, float deltaTime) {
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        Batch batch = renderer.getBatch();
        batch.draw(terrainTex, 0, size.height - 256);
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        if (DesktopInput.isShiftDown()) {
            z += rotation;
            revalidateImage();
            return true;
        }

        if (rotation > 0) {
            terrainX += (int) (10 * rotation);
            revalidateImage();
        } else {
            terrainX += (int) (10 * rotation);
            revalidateImage();
        }

        return super.mouseWheel(mouseX, mouseY, rotation);
    }

    private void revalidateImage() {
        if (terrainNoise == null || caveNoise == null) return;
        terrain.setBlending(Pixmap.Blending.None);
        terrain.setColor(0, 0, 0, 0);
        terrain.drawRectangle(0, 0, terrain.getWidth(), terrain.getHeight());
        for (int i = 0; i < size.width; i++) {
            int height = (int) terrainNoise.evaluateNoise(i + terrainX);
//            renderer.line(pos.x + i, size.height, pos.x + i, size.height - height, RgbColor.rgb(height <= 64 ? 0x0055aa : 0x55aa55));

            for (int y = 0; y < 256; y++) {
//                boolean cave = false;
//                double v = 1.0 - ((height - 70) / (70.0));
//                double v1 = caveNoise.evaluateNoise(i + terrainX, y);
//                cave = !((v - v1) > 0.0) && v1 > 0.0;
                boolean cave;
//                double densityFx = 64.0;
//                double v = 1.0 - ((height - densityFx) / densityFx);
//                v *= ((height - (height - 7 - y))) / densityFx;
                double v1 = caveNoise.evaluateNoise(i + terrainX, y, z);
//                cave = !((v - v1) > 0.0) && v1 > 0.0;
                cave = v1 > 0.0;
                int color;
                if (height < 64 && y < 64) {
                    if (y > height - 5 && y <= height) {
                        color = 0xffdf4fff;
                    } else if (y <= height - 5) {
                        color = 0x606060ff;
                    } else {
                        color = 0x0000f080;
                    }
                    if (cave && y <= height - 7) {
                        color = 0xffffff20;
                    }
                } else {
                    if (y == height) {
                        color = 0x00a000ff;
                    } else if (y > height - 3 && y < height) {
                        color = 0x946845ff;
                    } else if (y <= height - 3) {
                        color = 0x606060ff;
                    } else {
                        color = 0x00000000;
                    }

                    if (cave && y <= height) {
                        color = 0xffffff20;
                    }
                }

                terrain.drawPixel(i, y, color);
            }
        }

        terrainTex.draw(terrain, 0, 0);
    }

    private double evaluateNoise(int x, int y) {
        return 0;
    }

    @Override
    public boolean charType(char character) {
        if (character == ' ') {
            this.terrainNoise = new TerrainNoise(new Random(0).nextLong());
            this.caveNoise = new CaveNoiseGenerator(new Random(0).nextLong());
            revalidateImage();
        }

        return super.charType(character);
    }

    public void dispose() {
        this.terrainNoise = null;
    }

    public TerrainNoise getTerrainNoise() {
        return terrainNoise;
    }

    public static WorldGenTestPanel create() {
        return new WorldGenTestPanel();
    }

    public void random() {
        this.terrainNoise = new TerrainNoise(new Random(0).nextLong());
    }
}
