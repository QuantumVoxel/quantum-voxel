package dev.ultreon.quantum.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.ExpressionType;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.registry.RegistryKeys;
import org.jetbrains.annotations.NotNull;

@Name("Block with ID")
@Description("The block with a given ID")
public class BlockWithIDExpr extends WithIDExpression<Block> {
    static {
        Skript.registerExpression(BlockWithIDExpr.class, Block.class, ExpressionType.PROPERTY, "[the] block with id %namespace%");
    }

    public BlockWithIDExpr() {
        super(RegistryKeys.BLOCK);
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "block";
    }

    @Override
    public @NotNull Class<? extends Block> getReturnType() {
        return Block.class;
    }

    @Override
    public @NotNull String toString(Object event, boolean debug) {
        return "block with id " + getExpr().getSingle(event);
    }
}
