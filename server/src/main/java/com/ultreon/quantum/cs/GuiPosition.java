package com.ultreon.quantum.cs;

public class GuiPosition implements Component {
    private int x;
    private int y;

    public GuiPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(GuiPosition position) {
        this.x = position.getX();
        this.y = position.getY();
    }

    public void add(int x, int y) {
        this.x += x;
        this.y += y;
    }

    public void add(GuiPosition position) {
        this.x += position.getX();
        this.y += position.getY();
    }

    public void subtract(int x, int y) {
        this.x -= x;
        this.y -= y;
    }

    public void subtract(GuiPosition position) {
        this.x -= position.getX();
        this.y -= position.getY();
    }

    public void multiply(int x, int y) {
        this.x *= x;
        this.y *= y;
    }

    public void multiply(GuiPosition position) {
        this.x *= position.getX();
        this.y *= position.getY();
    }

    public void divide(int x, int y) {
        this.x /= x;
        this.y /= y;
    }

    public void divide(GuiPosition position) {
        this.x /= position.getX();
        this.y /= position.getY();
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GuiPosition that = (GuiPosition) o;

        if (x != that.x) return false;
        return y == that.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    public GuiPosition copy() {
        return new GuiPosition(x, y);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }
}
