package dev.ultreon.quantum.api.commands.error;

import dev.ultreon.quantum.api.commands.MessageCode;

public class NeedPlayerError extends CommandError {

    public NeedPlayerError() {
        super(MessageCode.NEED_PLAYER, "You need to be a player to use this command!");
        this.setGeneric();
    }

    public NeedPlayerError(int index) {
        super(MessageCode.NEED_PLAYER, "You need to be a player to use this command!", index);
    }

    @Override
    public String getName() {
        return "NotPlayer";
    }
}