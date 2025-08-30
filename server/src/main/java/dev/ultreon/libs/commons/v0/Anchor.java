package dev.ultreon.libs.commons.v0;

public enum Anchor {
    NE(-1, -1),
    N(0, -1),
    NW(1, -1),
    E(-1, 0),
    CENTER(0, 0),
    W(1, 0),
    SE(-1, 1),
    S(0, 1),
    SW(1, 1);

    private final int x;
    private final int y;

    Anchor(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}
