package dev.ultreon.quantum.server;

import dev.ultreon.quantum.events.LoadingEvent;

@Deprecated
public final class GameCommands {
    @Deprecated
    public static void register() {
//        CommandRegistry.register(new TeleportCommand());
//        CommandRegistry.register(new TpDimCommand());
//        CommandRegistry.register(new KillCommand());
//        CommandRegistry.register(new FlyCommand());
//        CommandRegistry.register(new InvincibleCommand());
//        CommandRegistry.register(new WhereAmICommand());
//        CommandRegistry.register(new HeightmapCommand());
//        CommandRegistry.register(new GameModeCommand());
//        CommandRegistry.register(new SpecificGameModeCommand(GameMode.SURVIVAL));
//        CommandRegistry.register(new SpecificGameModeCommand(GameMode.SPECTATOR));
//        CommandRegistry.register(new SpecificGameModeCommand(GameMode.BUILDER));
//        CommandRegistry.register(new SpecificGameModeCommand(GameMode.BUILDER_PLUS));
//        CommandRegistry.register(new SpecificGameModeCommand(GameMode.ADVENTUROUS));
//        CommandRegistry.register(new TimeCommand());
//        CommandRegistry.register(new PlayerCommand());
//        CommandRegistry.register(new ChunkCommand());
//        CommandRegistry.register(new GiveCommand());
//        CommandRegistry.register(new SummonCommand());
//        CommandRegistry.register(new SummonItemCommand());
//        CommandRegistry.register(new SetVarCommand());
//        CommandRegistry.register(new EntityCommand());
//        CommandRegistry.register(new ItemCommand());
//        CommandRegistry.register(new DeleteChunkCommand());
//        CommandRegistry.register(new FillCommand());
//        CommandRegistry.register(new FillSphereCommand());
//        CommandRegistry.register(new PositionCommand());
//        CommandRegistry.register(new ReplaceCommand());
//
//        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
//            CommandRegistry.register(new JSCommand());
//            CommandRegistry.register(new DebugCommand());
//        }

        LoadingEvent.REGISTER_COMMANDS.factory().onRegisterCommands();

//        Command.runCommandLoaders();
    }
}
