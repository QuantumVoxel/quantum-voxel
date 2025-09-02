package dev.ultreon.quantum.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.skript.QuantumSkript;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class WithIDExpression<T> extends SimplePropertyExpression<NamespaceID, T> {
    private final RegistryKey<Registry<T>> registry;
    private @NotNull Expression<ClassInfo<?>> type;

    public WithIDExpression(RegistryKey<Registry<T>> registry) {
        this.registry = registry;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public abstract String toString(@ch.njol.skript.shadow.org.jetbrains.annotations.Nullable Object event, boolean debug);

    @Override
    @Nullable
    @SuppressWarnings("NullableProblems")
    public T convert(@NotNull NamespaceID from) {
        try {
            return QuantumSkript.instance().getRegistryHandle().get(registry).get(from);
        } catch (UnsupportedOperationException e) {
            Skript.warning("Failed to get " + registry.id() + " for id " + from.toString());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        super.init(expressions, matchedPattern, isDelayed, parseResult);

        type = (Expression<ClassInfo<?>>) expressions[1];

        return true;
    }

    public RegistryKey<Registry<T>> getRegistry() {
        return registry;
    }
}
