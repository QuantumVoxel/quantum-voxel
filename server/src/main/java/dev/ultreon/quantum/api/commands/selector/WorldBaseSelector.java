package dev.ultreon.quantum.api.commands.selector;

import dev.ultreon.quantum.api.commands.Command;
import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.api.commands.TabCompleting;
import dev.ultreon.quantum.api.commands.error.*;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.WorldAccess;

import java.util.ArrayList;

public class WorldBaseSelector extends BaseSelector<World> {
    private final CommandSender sender;

    public WorldBaseSelector(CommandSender sender, Parsed parsed) {
        super(parsed);
        this.sender = sender;
        this.result = this.calculateData();
    }

    public WorldBaseSelector(CommandSender sender, String text) {
        super(text);
        this.sender = sender;
        this.result = this.calculateData();
    }

    @Override
    public Result<World> calculateData() {
        Player player = null;
        if (this.sender instanceof Player) {
            player = (Player) this.sender;
        }
        if (this.getError() != null) {
            return new Result<>(null, this.getError());
        }
        World target = null;
        if (this.getKey() == SelectorKey.TAG) {
            if ("here".equals(this.getStringValue())) {
                if (player == null) {
                    return new Result<>(null, new NeedPlayerError());
                } else {
                    WorldAccess worldAccess = player.getWorld();
                    if (!(worldAccess instanceof World world)) {
                        return new Result<>(null, new NotFoundError("world"));
                    }
                    return new Result<>(world, null);
                }
            } else {
                return new Result<>(null, new OverloadError());
            }
        } else if (this.getKey() == SelectorKey.NAME) {
            String name = this.getStringValue();
            NamespaceID namespaceID = NamespaceID.tryParse(name);
            if (namespaceID == null) {
                return new Result<>(null, new InvalidKeyError(name));
            }
            target = QuantumServer.get().getWorld(namespaceID);
            if (target == null) {
                return new Result<>(null, new NotFoundError("world " + name));
            }
        }
        if (target == null) {
            return new Result<>(null, new ImpossibleError("Got error that couldn't be caught."));
        } else {
            return new Result<>(target, null);
        }
    }

    public static ArrayList<String> tabComplete(CommandSender sender, Command command, String arg) {
        ArrayList<String> output = new ArrayList<>();
        if (sender instanceof Player) {
            TabCompleting.selectors(output, SelectorKey.TAG, arg, "here");
        }
        TabCompleting.selectors(output, SelectorKey.NAME, arg, TabCompleting.worlds(new ArrayList<>(), ""));
        TabCompleting.selectors(output, SelectorKey.UUID, arg, TabCompleting.worldIds(new ArrayList<>(), ""));
        return output;
    }
}