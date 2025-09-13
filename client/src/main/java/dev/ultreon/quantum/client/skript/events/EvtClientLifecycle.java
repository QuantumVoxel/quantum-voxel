//package dev.ultreon.quantum.client.skript.events;
//
//import ch.njol.skript.Skript;
//import ch.njol.skript.doc.Name;
//import ch.njol.skript.lang.Literal;
//import ch.njol.skript.lang.SkriptParser;
//import ch.njol.skript.shadow.org.jetbrains.annotations.Nullable;
//import dev.ultreon.quantum.client.api.events.ClientLifecycleEvent;
//import dev.ultreon.quantum.skript.QuantumSkriptEvent;
//import org.jetbrains.annotations.NotNull;
//
//@Name("Client Lifecycle Event")
//public class EvtClientLifecycle extends QuantumSkriptEvent {
//    static {
//        Skript.registerEvent("Client Started", EvtClientLifecycle.class, ClientLifecycleEvent.class,
//                "[on] [quantum [voxel]] client (started|loaded)",
//                "[on] [quantum [voxel]] client (stopped|disposed)");
//    }
//
//    private LifecycleType type;
//
//    private enum LifecycleType {
//        STARTED,
//        STOPPED;
//    }
//
//    @Override
//    public boolean init(Literal<?> @NotNull [] literals, int matchedPattern, SkriptParser.@NotNull ParseResult parseResult) {
//        switch (matchedPattern) {
//            case 0:
//                this.type = LifecycleType.STARTED;
//                return true;
//            case 1:
//                this.type = LifecycleType.STOPPED;
//                return true;
//            default:
//                return false;
//        }
//    }
//
//    @Override
//    public boolean check(@NotNull Object o) {
//        if (type == LifecycleType.STARTED) {
//            return o instanceof ClientLifecycleEvent.ClientLoaded;
//        } else {
//            return o instanceof ClientLifecycleEvent.ClientDisposed;
//        }
//    }
//
//    @Override
//    public @NotNull String toString(@Nullable @NotNull Object event, boolean debug) {
//        switch (type) {
//            case STARTED:
//                return "on quantum client started";
//            case STOPPED:
//                return "on quantum client stopped";
//        }
//        throw new IllegalStateException("Unknown type");
//    }
//}
