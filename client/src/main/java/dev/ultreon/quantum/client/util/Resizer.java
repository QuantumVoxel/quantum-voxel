package dev.ultreon.quantum.client.util;

import dev.ultreon.quantum.util.Vec2;

public class Resizer {
    private final float ratio;
    private final float relativeRatio;
    private final Orientation orientation;
    private float sourceWidth;
    private float sourceHeight;

    public Resizer(float srcWidth, float srcHeight) {
        this.ratio = srcWidth / srcHeight;

        if (srcWidth > srcHeight) {
            this.relativeRatio = srcWidth / srcHeight;
            this.orientation = Orientation.LANDSCAPE;
        } else if (srcWidth < srcHeight) {
            this.relativeRatio = srcHeight / srcWidth;
            this.orientation = Orientation.PORTRAIT;
        } else {
            this.relativeRatio = 1;
            this.orientation = Orientation.SQUARE;
        }

        this.sourceWidth = srcWidth;
        this.sourceHeight = srcHeight;
    }

    public Resizer() {
        this(1, 1);
    }

    public Vec2 fill(float maxWidth, float maxHeight) {
        float aspectRatio;
        float width;
        float height;

        if (this.sourceWidth < this.sourceHeight) {
            aspectRatio = (float) (this.sourceWidth / (double) this.sourceHeight);

            width = maxWidth;
            height = (int) (width / aspectRatio);

            if (height < maxHeight) {
                aspectRatio = (float) (this.sourceHeight / (double) this.sourceWidth);

                height = maxHeight;
                width = (int) (height / aspectRatio);
            }
        } else {
            aspectRatio = (float) (this.sourceHeight / (double) this.sourceWidth);

            height = maxHeight;
            width = (int) (height / aspectRatio);
            if (width < maxWidth) {
                aspectRatio = (float) (this.sourceWidth / (double) this.sourceHeight);

                width = maxWidth;
                height = (int) (width / aspectRatio);
            }
        }

        return new Vec2(width, height);
    }

    public void set(int width, int height) {
        this.sourceWidth = width;
        this.sourceHeight = height;
    }

    public Vec2 fit(float maxWidth, float maxHeight) {
        float aspectRatio = this.sourceWidth / this.sourceHeight;
        float width;
        float height;

        if (maxWidth / maxHeight > aspectRatio) {
            height = maxHeight;
            width = height * aspectRatio;
        } else {
            width = maxWidth;
            height = width / aspectRatio;
        }

        return new Vec2(width, height);
    }

    public Vec2 center(float maxWidth, float maxHeight) {
        float width = this.sourceWidth;
        float height = this.sourceHeight;

        return width < maxWidth && height < maxHeight
                ? new Vec2(width, height)
                : fit(maxWidth, maxHeight);
    }


    /**
     * Aspect ratio orientation.
     */
    public enum Orientation {
        LANDSCAPE,
        SQUARE,
        PORTRAIT
    }

    public float getRatio() {
        return this.ratio;
    }

    public float getRelativeRatio() {
        return this.relativeRatio;
    }

    public Orientation getOrientation() {
        return this.orientation;
    }

    public float getSourceWidth() {
        return this.sourceWidth;
    }

    public float getSourceHeight() {
        return this.sourceHeight;
    }
}
