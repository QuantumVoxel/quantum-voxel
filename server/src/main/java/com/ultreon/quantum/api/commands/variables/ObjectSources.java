package com.ultreon.quantum.api.commands.variables;

import com.ultreon.quantum.api.commands.selector.SelectorFactories;
import com.ultreon.quantum.entity.Player;

public class ObjectSources {
    public static final SelectorObjectSource<Player> PLAYER = new SelectorObjectSource<>("player", Player.class, SelectorFactories.PLAYER);
}
