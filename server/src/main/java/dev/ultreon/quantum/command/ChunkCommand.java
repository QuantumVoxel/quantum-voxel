package dev.ultreon.quantum.command;

import dev.ultreon.quantum.api.commands.*;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.chat.Chat;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.Location;
import dev.ultreon.quantum.world.ServerChunk;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.ubo.DataIo;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ChunkCommand extends Command {
    public ChunkCommand() {
        this.requirePermission("quantum.commands.kill");
        this.setCategory(CommandCategory.TELEPORT);
        this.data().aliases("chunk", "debugChunk");
    }

    @DefineCommand("dump-data")
    public @Nullable CommandResult executeDumpData(CommandSender sender, CommandContext commandContext, String alias) {
        Location location = sender.getLocation();

        ServerWorld serverWorld = location.getServerWorld();
        QuantumServer server = serverWorld.getServer();

        server.execute(() -> {
            Chunk chunkAt = serverWorld.getChunkAt(location.getBlockVec());
            if (chunkAt instanceof ServerChunk serverChunk) {
                MapType save = serverChunk.save();

                try {
                    Files.createDirectories(Path.of("debug/chunks"));
                    DataIo.writeCompressed(save, Path.of("debug/chunks/" + location.getBlockVec() + ".ubo").toFile());
                    String uso = DataIo.toUso(save);
                    Files.writeString(Path.of("debug/chunks/" + location.getBlockVec() + ".uso"), uso, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    Chat.sendError(sender, "Failed to save chunk data: " + e.getMessage());
                }
            }

            Chat.sendSuccess(sender, "Saved chunk data at " + location.getBlockVec());
        });

        return infoMessage("Saving chunk data at " + location.getBlockVec());
    }
}
