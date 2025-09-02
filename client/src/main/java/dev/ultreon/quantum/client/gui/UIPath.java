/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.ultreon.quantum.client.gui;

import dev.ultreon.quantum.client.gui.widget.Widget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *  * Represents a path through the UI.
 *
 * @author xypercode
 */
public class UIPath {
    private final List<Widget> components;

    /**
     * Constructs a new UIPath with no components.
     */
    public UIPath() {
        this.components = new ArrayList<>();
    }

    /**
     * Constructs a new UIPath with a single component.
     *
     * @param component the component.
     */
    public UIPath(Widget component) {
        this.components = new ArrayList<>(List.of(component));
    }

    /**
     * Constructs a new UIPath with the given components.
     *
     * @param components the components.
     */
    public UIPath(Collection<Widget> components) {
        this.components = new ArrayList<>(components);
    }

    /**
     * Constructs a new UIPath with the given path.
     *
     * @param path the path.
     */
    public UIPath(UIPath path) {
        this.components = new ArrayList<>(path.components);
    }

    /**
     * Appends a component to the path.
     *
     * @param component the component.
     * @return the path.
     */
    public UIPath append(Widget component) {
        this.components.add(component);
        return this;
    }

    /**
     * Appends a path to the path.
     *
     * @param path the path.
     * @return the path.
     */
    public UIPath append(UIPath path) {
        this.components.addAll(path.components);
        return this;
    }

    /**
     * Returns the path as a string.
     *
     * @return the path.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Widget component : this.components) {
            builder.append(component.getName());
            builder.append("::");
        }

        builder.delete(builder.length() - 2, builder.length());
        return builder.toString();
    }
}
