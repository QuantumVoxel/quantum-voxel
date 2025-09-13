//package dev.ultreon.quantum.skript.events;
//
//import ch.njol.skript.Skript;
//import ch.njol.skript.doc.Description;
//import ch.njol.skript.doc.Name;
//import ch.njol.skript.lang.Literal;
//import ch.njol.skript.lang.SkriptParser;
//import dev.ultreon.quantum.api.events.block.BlockChangeEvent;
//import dev.ultreon.quantum.skript.QuantumSkriptEvent;
//import org.jetbrains.annotations.NotNull;
//
//@Name("Block Change")
//@Description("Called when a block is broken.")
//public class EvtBlockChange extends QuantumSkriptEvent {
//    static {
//        Skript.registerEvent("Block broken", EvtBlockChange.class, BlockChangeEvent.class,
//                "[on] block (broken|break)",
//                "[on] block place[d]",
//                "[on] block set",
//                "[on] attempted block (broken|break)",
//                "[on] attempted block place[d]"
//        );
//    }
//
//    private int matchedPattern;
//
//    @Override
//    public boolean init(Literal<?> @NotNull [] args, int matchedPattern, SkriptParser.@NotNull ParseResult parseResult) {
//        this.matchedPattern = matchedPattern;
//        return true;
//    }
//
//    @Override
//    public boolean check(@NotNull Object event) {
//        switch (matchedPattern) {
//            case 0:
//                return event instanceof BlockChangeEvent.Broken;
//            case 1:
//                return event instanceof BlockChangeEvent.Place;
//            case 2:
//                return event instanceof BlockChangeEvent.Set;
//            case 3:
//                return event instanceof BlockChangeEvent.AttemptBreak;
//            case 4:
//                return event instanceof BlockChangeEvent.AttemptPlace;
//            default:
//                return false;
//        }
//    }
//
//    @Override
//    public @NotNull String toString(@NotNull Object event, boolean debug) {
//        switch (matchedPattern) {
//            case 0:
//                return "on block broken";
//            case 1:
//                return "on block placed";
//            case 2:
//                return "on block set";
//            case 3:
//                return "on attempted block break";
//            case 4:
//                return "on attempted block place";
//        }
//
//        throw new IllegalStateException("Unknown type");
//    }
//}
