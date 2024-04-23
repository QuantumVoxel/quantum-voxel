package com.ultreon.quantum.api.commands.variables;

public class PlayerVariable {
    private final PlayerVariables variables;
    private final String name;

    public PlayerVariable(PlayerVariables variables, String name) {
        this.variables = variables;
        this.name = name;
    }

    public Object getValue() {
        return variables.getVariable(name);
    }

    public void setValue(Object value) {
        variables.setVariable(name, value);
    }
}
