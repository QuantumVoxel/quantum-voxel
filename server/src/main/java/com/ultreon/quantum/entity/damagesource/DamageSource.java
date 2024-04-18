package com.ultreon.quantum.entity.damagesource;

import com.ultreon.quantum.entity.Entity;
import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.text.Formatter;
import com.ultreon.quantum.text.TextObject;
import com.ultreon.quantum.util.Identifier;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.Nullable;

public class DamageSource {
    public static final DamageSource NOTHING = DamageSource.register(new Identifier("none"), new DamageSource());
    public static final DamageSource FALLING = DamageSource.register(new Identifier("falling"), new DamageSource());
    public static final DamageSource VOID = DamageSource.register(new Identifier("void"), new DamageSource().byPassInvincibility(true));
    public static final DamageSource KILL = DamageSource.register(new Identifier("kill"), new DamageSource().byPassInvincibility(true));

    private boolean byPassInvincibility;

    private static <T extends DamageSource> T register(Identifier id, T damageSource) {
        Registries.DAMAGE_SOURCE.register(id, damageSource);
        return damageSource;
    }

    public @Nullable Identifier getType() {
        return Registries.DAMAGE_SOURCE.getId(this);
    }

    public TextObject getDescription(@Nullable Entity entity) {
        Identifier type = this.getType();
        if (type == null) return Formatter.format("<red>NULL</>");
        if (entity == null) return TextObject.translation(type.namespace() + ".damageSource." + type.path().replaceAll("/", "."), Formatter.format("<red>NULL</>"));
        TextObject displayName = entity.getDisplayName();
        return TextObject.translation(type.namespace() + ".damageSource." + type.path().replaceAll("/", "."), displayName);
    }

    public boolean byPassInvincibility() {
        return this.byPassInvincibility;
    }

    public @This DamageSource byPassInvincibility(boolean byPassInvincibility) {
        this.byPassInvincibility = byPassInvincibility;
        return this;
    }
}
