//package dev.ultreon.quantum.skript.events;
//
//import ch.njol.skript.Skript;
//import ch.njol.skript.doc.Description;
//import ch.njol.skript.doc.Name;
//import ch.njol.skript.lang.Literal;
//import ch.njol.skript.lang.SkriptParser;
//import dev.ultreon.quantum.api.events.block.BlockEvent;
//import dev.ultreon.quantum.skript.QuantumSkriptEvent;
//import org.jetbrains.annotations.NotNull;
//
//@Name("Block Use")
//@Description("Called when a block is used.")
//public class EvtBlockUse extends QuantumSkriptEvent {
//    static {
//        Skript.registerEvent("Block broken", EvtBlockUse.class, BlockEvent.Use.class,
//                "[on] block use[d]"
//        );
//    }
//
//    @Override
//    public boolean init(Literal<?> @NotNull [] args, int matchedPattern, SkriptParser.@NotNull ParseResult parseResult) {
//        return true;
//    }
//
//    @Override
//    public boolean check(@NotNull Object event) {
//        return event instanceof BlockEvent.Use;
//    }
//
//    @Override
//    public @NotNull String toString(@NotNull Object event, boolean debug) {
//        return "on block use";
//    }
//}
