package dev.ultreon.quantum.api.commands.variables;

import dev.ultreon.quantum.api.commands.selector.SelectorFactories;
import dev.ultreon.quantum.entity.player.Player;

public class ObjectSources {
    public static final SelectorObjectSource<Player> PLAYER = new SelectorObjectSource<>("player", Player.class, SelectorFactories.PLAYER);
}
