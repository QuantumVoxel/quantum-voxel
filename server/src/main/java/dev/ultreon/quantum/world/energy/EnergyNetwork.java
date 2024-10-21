package dev.ultreon.quantum.world.energy;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import dev.ultreon.quantum.world.Direction;

public class EnergyNetwork {
    private final Array<EnergyNode> nodes = new Array<>();
    private final Queue<EnergyNode> removals = new Queue<>();
    private final Queue<EnergyNode> revalidations = new Queue<>();

    public Array<EnergyNode> getNodes() {
        return nodes;
    }

    public void addNode(EnergyNode node, Direction connect, EnergyNode other) {
        if (node == other) throw new IllegalArgumentException("Cannot connect node to itself");
        if (nodes.contains(node, true)) throw new IllegalArgumentException("Node already exists: " + node);
        if (!nodes.contains(other, true)) throw new IllegalArgumentException("Node does not exist: " + other);
        node.set(connect, other);
        other.set(connect.getOpposite(), node);
        nodes.add(node);
    }

    public boolean removeNode(EnergyNode node) {
        boolean b = nodes.removeValue(node, true);
        if (b) {
            for (EnergyNode energyNode : node) revalidations.addLast(energyNode);
            removals.addLast(node);
        }
        return b;
    }

    public void tick() {
        if (revalidations.isEmpty()) return;

        EnergyNode node = revalidations.removeFirst();
        if (!nodes.contains(node, true)) return;

        node.onRevalidate(this);
        boolean revalidated = false;
        for (EnergyNode energyNode : node) {
            if (!nodes.contains(energyNode, true)) continue;
            revalidations.addLast(energyNode);
            revalidated = true;
        }

        if (!revalidated) {
            removals.addLast(node);
            return;
        }

        for (EnergyNode energyNode : nodes) {
            energyNode.tick();
        }
    }

    public void clear() {
        nodes.clear();
        removals.clear();
        revalidations.clear();
    }
}
