package dev.ultreon.quantum.server.player;

import dev.ultreon.quantum.api.commands.perms.Permission;
import org.apache.commons.collections4.set.ListOrderedSet;

public class PermissionMap {
    protected final ListOrderedSet<Permission> allows = new ListOrderedSet<>();
    protected final ListOrderedSet<Permission> denies = new ListOrderedSet<>();

    public boolean has(Permission permission) {
        return this.allows.stream().anyMatch(p -> p.allows(permission)) && this.denies.stream().noneMatch(p -> p.allows(permission));
    }
}
