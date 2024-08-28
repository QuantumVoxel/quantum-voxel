package dev.ultreon.quantum.client.model.blockbench;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record BBModelOutliner(List<BBModelOutlineInfo> entries) {

    public BBModelOutlineInfo parent(UUID uuid) {
        for (BBModelOutlineInfo node : entries) {
            if (node.uuid().equals(uuid)) {
                return node;
            }
            BBModelOutlineInfo parent = parent(node.uuid());
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBModelOutliner) obj;
        return Objects.equals(this.entries, that.entries);
    }

    @Override
    public String toString() {
        return "BBModelOutliner[" +
               "entries=" + entries + ']';
    }


    //language=java
    /*
    public BBModelNode parent(UUID uuid) {
        Stack<BBModelNode> stack = new Stack<>();
        stack.addAll(entries);
        while (!stack.isEmpty()) {
            BBModelNode node = stack.pop();
            if (node.uuid().equals(uuid)) {
                return node;
            }
            stack.addAll(node.getChildren());
        }
        return null;
    }
     */
}
