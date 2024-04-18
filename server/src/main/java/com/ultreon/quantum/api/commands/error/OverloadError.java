package com.ultreon.quantum.api.commands.error;

import com.ultreon.quantum.api.commands.MessageCode;

public class OverloadError extends CommandError {
    public OverloadError() {
        super(MessageCode.OVERLOAD, "No overload matching these arguments!");
        this.setOnlyOverloads(true);
    }

    public OverloadError(int index) {
        super("No overload matching these arguments!", index);
        this.setOnlyOverloads(true);
    }

    @Override
    public String getName() {
        return "NoMatch";
    }
}