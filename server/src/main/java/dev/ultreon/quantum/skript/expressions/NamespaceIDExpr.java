//package dev.ultreon.quantum.skript.expressions;
//
//import ch.njol.skript.Skript;
//import ch.njol.skript.doc.Description;
//import ch.njol.skript.doc.Examples;
//import ch.njol.skript.doc.Name;
//import ch.njol.skript.lang.Expression;
//import ch.njol.skript.lang.ExpressionType;
//import ch.njol.skript.lang.SkriptParser;
//import ch.njol.skript.lang.util.SimpleExpression;
//import ch.njol.util.Kleenean;
//import dev.ultreon.quantum.util.NamespaceID;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//@Name("Namespace ID")
//@Description("The ID of a block, item, entity, or anything that is registered in the game")
//@Examples({
//        "set {_id} to namespace id \"quantum:stone\""
//})
//public class NamespaceIDExpr extends SimpleExpression<NamespaceID> {
//    static {
//        Skript.registerExpression(NamespaceIDExpr.class, NamespaceID.class, ExpressionType.SIMPLE, "[<.+>:]<.+>");
//    }
//
//    private @Nullable NamespaceID[] id;
//
//    @Override
//    public boolean isSingle() {
//        return true;
//    }
//
//    @Override
//    public @NotNull Class<? extends NamespaceID> getReturnType() {
//        return NamespaceID.class;
//    }
//
//    @Override
//    public @NotNull String toString(@NotNull Object event, boolean debug) {
//        return id[0] != null ? id[0].toString() : "<undefined>";
//    }
//
//    @Override
//    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
//        String expr = parseResult.expr;
//        NamespaceID namespaceID = NamespaceID.tryParse(expr);
//        if (namespaceID == null) {
//            return false;
//        }
//        this.id = new NamespaceID[]{namespaceID};
//        return true;
//    }
//
//    @Override
//    protected NamespaceID @NotNull [] get(@NotNull Object event) {
//        return id;
//    }
//}
