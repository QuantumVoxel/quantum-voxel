package dev.ultreon.quantum.server;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.api.commands.Command;
import dev.ultreon.quantum.command.*;
import dev.ultreon.quantum.events.LoadingEvent;
import dev.ultreon.quantum.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;

public final class GameCommands {
    public static void register() {
        CommandRegistry.register(new TeleportCommand());
        CommandRegistry.register(new KillCommand());
        CommandRegistry.register(new FlyCommand());
        CommandRegistry.register(new InvincibleCommand());
        CommandRegistry.register(new WhereAmICommand());
        CommandRegistry.register(new GamemodeCommand());
        CommandRegistry.register(new TimeCommand());
        CommandRegistry.register(new PlayerCommand());
        CommandRegistry.register(new ChunkCommand());
        CommandRegistry.register(new GiveCommand());
        CommandRegistry.register(new SummonCommand());
        CommandRegistry.register(new SummonItemCommand());
        CommandRegistry.register(new SetVarCommand());
        CommandRegistry.register(new EntityCommand());
        CommandRegistry.register(new ItemCommand());

        if (FabricLoader.getInstance().isDevelopmentEnvironment())
            CommandRegistry.register(new JSCommand());

        LoadingEvent.REGISTER_COMMANDS.factory().onRegisterCommands();

        Command.runCommandLoaders();
    }
}
