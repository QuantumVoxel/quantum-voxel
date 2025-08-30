package dev.ultreon.quantum.server.player;

import dev.ultreon.quantum.api.commands.perms.Permission;

import java.util.LinkedHashSet;

public class PermissionMap {
    protected final LinkedHashSet<Permission> allows = new LinkedHashSet<>();
    protected final LinkedHashSet<Permission> denies = new LinkedHashSet<>();

    public boolean has(Permission permission) {
        boolean b = true;
        for (Permission allow : this.allows) {
            if (allow.allows(permission)) {
                b = false;
                break;
            }
        }
        if (b) return false;
        for (Permission p : this.denies) {
            if (p.allows(permission)) {
                return false;
            }
        }
        return true;
    }
}
