package dev.ultreon.quantum;

public interface Margins {
    int getLeft();
    int getRight();
    int getTop();
    int getBottom();

    void setMargins(int left, int top, int right, int bottom);
}
