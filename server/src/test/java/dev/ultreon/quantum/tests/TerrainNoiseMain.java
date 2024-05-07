package dev.ultreon.quantum.tests;

import com.ultreon.libs.commons.v0.Mth;
import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import dev.ultreon.quantum.world.TerrainNoise;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class TerrainNoiseMain {

    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
        NoiseSource terrainNoise = new TerrainNoise(new Random().nextLong());

        for (int x = 0; x < 1024; x++) {
            for (int y = 0; y < 1024; y++) {
                int val = (int) (Mth.clamp(terrainNoise.evaluateNoise(x, y), 0, 255));
                image.setRGB(x, y, val << 16 | val << 8 | val);
            }
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.add(new JLabel(new ImageIcon(image)));
            frame.pack();
            frame.setVisible(true);
        });
    }
}
