package dev.ultreon.quantum.client.input.controller;

import com.badlogic.gdx.math.Vector2;
import dev.ultreon.quantum.client.gui.widget.UIContainer;
import dev.ultreon.quantum.client.gui.widget.Widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class GuiNavigator {
    private final UIContainer<?> container; // List of all widgets in the GUI

    public GuiNavigator(UIContainer<?> container) {
        this.container = container;
    }

    public Widget moveFocus(Widget current, UIDirection direction) {
        List<Widget> candidates = new ArrayList<>();

        for (Widget widget : container.getWidgets()) {
            if (isInFrustum(current, widget, direction) &&
                hasLineOfSight(current, widget, direction)) {
                candidates.add(widget);
            }
        }

        if (!candidates.isEmpty())
            return getClosestWidget(current, candidates);

        return null; // No valid widget found
    }

    private boolean isInFrustum(Widget current, Widget candidate, UIDirection direction) {
        if (direction == UIDirection.LEFT) {
            return candidate.getCenter().x < current.getCenter().x;
        } else if (direction == UIDirection.RIGHT) {
            return candidate.getCenter().x > current.getCenter().x;
        } else if (direction == UIDirection.UP) {
            return candidate.getCenter().y > current.getCenter().y;
        } else if (direction == UIDirection.DOWN) {
            return candidate.getCenter().y < current.getCenter().y;
        }
        return false;
    }

    private boolean hasLineOfSight(Widget current, Widget candidate, UIDirection direction) {
        // Logic to ensure that the direct line between the two widgets
        // does not cross any diagonal boundaries. This could involve
        // checking if the line intersects with any of the diagonal lines.
        float angle = current.getCenter().angleDeg(candidate.getCenter());
        if (direction == UIDirection.LEFT) {
            return angle <= 45 || angle >= 135;
        } else if (direction == UIDirection.RIGHT) {
            return angle >= 45 && angle <= 135;
        } else if (direction == UIDirection.UP) {
            return angle >= 135 || angle <= 45;
        } else if (direction == UIDirection.DOWN) {
            return angle <= 225 && angle >= 135;
        }

        throw new UnsupportedOperationException("Invalid direction"); // Wait what?
    }

    private Widget getClosestWidget(Widget current, Collection<Widget> candidates) {
        // Find the closest widget by comparing distances
        return candidates.stream()
                .min(Comparator.comparingDouble(c -> distance(current.getCenter(), c.getCenter())))
                .orElse(null);
    }

    private double distance(Vector2 a, Vector2 b) {
        return Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
    }

}