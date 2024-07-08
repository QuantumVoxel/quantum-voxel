package dev.ultreon.quantum.client.gui;

import dev.ultreon.quantum.util.Copyable;

import java.util.Objects;

/**
 * A 2D position on the screen.
 * This class is mutable and can be copied.
 *
 * @author XyperCode
 * @since 0.1.0
 */
public class Position implements Copyable<Position> {
    public int x;
    public int y;

    /**
     * Creates a new position
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a new position
     *
     * @param value the x and y coordinates
     */
    public Position(int value) {
        this.x = value;
        this.y = value;
    }

    /**
     * Creates a new zero position
     */
    public Position() {
        this(0, 0);
    }

    /**
     * Checks if two positions are equal
     *
     * @param o the other position
     * @return true if the positions are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return this.x == position.x && this.y == position.y;
    }

    /**
     * Hashes the position
     *
     * @return the calculated hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    /**
     * Returns a string representation of the position
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }

    /**
     * Creates a copy of this position
     *
     * @return the copy
     */
    @Override
    public Position cpy() {
        return new Position(this.x, this.y);
    }

    /**
     * Sets the x and y coordinates
     *
     * @param pos the new position
     */
    public void set(Position pos) {
        this.x = pos.x;
        this.y = pos.y;
    }

    /**
     * Sets the x and y coordinates
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the x and y coordinates to zero
     */
    public void idt() {
        this.x = 0;
        this.y = 0;
    }
}
