package dev.ultreon.quantum.api.commands.error;

import dev.ultreon.quantum.api.commands.MessageCode;
import dev.ultreon.quantum.world.World;

public class NotFoundInWorldError extends CommandError {

    public NotFoundInWorldError(String WHAT) {
        super(MessageCode.NOT_FOUND_IN_WORLD, "There's no " + WHAT + " found in this world!");
    }

    public NotFoundInWorldError(String WHAT, int index) {
        super(MessageCode.NOT_FOUND_IN_WORLD, "There's no " + WHAT + " found in this world!", index);
    }

    public NotFoundInWorldError(String WHAT, World world) {
        super(MessageCode.NOT_FOUND_IN_WORLD, "There's no " + WHAT + " found in world: " + world.getDimension().id());
    }

    public NotFoundInWorldError(String WHAT, World world, int index) {
        super(MessageCode.NOT_FOUND_IN_WORLD, "There's no " + WHAT + " found in world: " + world.getDimension().id(), index);
    }

    @Override
    public String getName() {
        return "NotFound";
    }
}