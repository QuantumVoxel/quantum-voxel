package dev.ultreon.quantum.entity.damagesource;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.text.Formatter;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.Nullable;

public class DamageSource {
    public static final DamageSource NOTHING = DamageSource.register(new NamespaceID("none"), new DamageSource());
    public static final DamageSource FALLING = DamageSource.register(new NamespaceID("falling"), new DamageSource());
    public static final DamageSource VOID = DamageSource.register(new NamespaceID("void"), new DamageSource().byPassInvincibility(true));
    public static final DamageSource KILL = DamageSource.register(new NamespaceID("kill"), new DamageSource().byPassInvincibility(true));
    public static final DamageSource HUNGER = DamageSource.register(new NamespaceID("hunger"), new DamageSource());
    public static final DamageSource PLAYER = DamageSource.register(new NamespaceID("player"), new DamageSource());

    private boolean byPassInvincibility;

    private static <T extends DamageSource> T register(NamespaceID id, T damageSource) {
        Registries.DAMAGE_SOURCE.register(id, damageSource);
        return damageSource;
    }

    public @Nullable NamespaceID getType() {
        return Registries.DAMAGE_SOURCE.getId(this);
    }

    public TextObject getDescription(@Nullable Entity entity) {
        NamespaceID type = this.getType();
        if (type == null) return Formatter.format("[light red]NULL</>");
        if (entity == null) return TextObject.translation(type.getDomain() + ".damageSource." + type.getPath().replaceAll("/", "."), Formatter.format("[light red]NULL</>"));
        TextObject displayName = entity.getDisplayName();
        return TextObject.translation(type.getDomain() + ".damageSource." + type.getPath().replaceAll("/", "."), displayName);
    }

    public boolean byPassInvincibility() {
        return this.byPassInvincibility;
    }

    public @This DamageSource byPassInvincibility(boolean byPassInvincibility) {
        this.byPassInvincibility = byPassInvincibility;
        return this;
    }
}
